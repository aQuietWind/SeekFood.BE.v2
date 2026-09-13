package com.seek.food.gateway.Filter;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.seek.food.config.Data.JWTData;
import com.seek.food.config.NacosConfig.Common.JWTConfig;
import com.seek.food.dto.Common.Result;
import com.seek.food.util.Exception.ErrorCodeEnum;
import com.seek.food.config.NacosConfig.Common.CommonRedisKeyConfig;
import com.seek.food.config.NacosConfig.Gateway.GatewayRequestPathConfig;
import com.seek.food.util.JWT.TokenCheckResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import com.seek.food.util.JWT.JWTUtil;

import java.util.HashMap;
import java.util.List;


@Order(1)       //过滤器的顺序，越小就越先执行
@Component      //使其被扫描到
@RefreshScope
public class TokenFilter implements GlobalFilter {

    private final JWTConfig jwtConfig;
    private final GatewayRequestPathConfig gatewayRequestPathConfig;
    private final StringRedisTemplate stringRedisTemplate;
    private final CommonRedisKeyConfig commonRedisKeyConfig;
    private final HashMap<String,String> jwtHeaders=new HashMap<>();
    //字节码化的Result
    private final static byte[] errorBytes = new Gson().toJson(Result.error(ErrorCodeEnum.UNAUTHORIZED)).getBytes();
    private static final Logger logger = LoggerFactory.getLogger(TokenFilter.class);
    // 构造器注入
    @Autowired
    public TokenFilter(JWTConfig jwtConfig, GatewayRequestPathConfig gatewayRequestPathConfig, StringRedisTemplate stringRedisTemplate,
                       CommonRedisKeyConfig commonRedisKeyConfig, ObjectMapper objectMapper) {
        this.jwtConfig = jwtConfig;
        this.gatewayRequestPathConfig = gatewayRequestPathConfig;
        this.stringRedisTemplate = stringRedisTemplate;
        this.commonRedisKeyConfig = commonRedisKeyConfig;
        for (JWTData jwtData : jwtConfig.getAllJWTData()) jwtHeaders.put(jwtData.getHeaderSign(), jwtData.getSecretKey());
    }



    //token处理拦截
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain){
        //实现方法，其中exchange用于获取和设置请求头、响应头。chain用于放行
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        logger.info("请求路径:{} ,进入TokenFilter",path);
        //检查该请求路径是否需要直接放行
        if(gatewayRequestPathConfig.checkAllowPath(path))return chain.filter(exchange);
        //检查该请求路径是否为封禁路径
        if (gatewayRequestPathConfig.checkRejectPath(path))return reject(exchange);
        //尝试获取Id
        long ownId=checkToken(getToken(request));
        //token是否有效，有效则放行
        if (ownId!=JWTUtil.FailResult)return chain.filter(getNewExchange(request,exchange,ownId));
        //拒绝放行
        logger.warn("token验证失败");
        return reject(exchange);
        }


    // 通过安全的HttpOnly Cookie来获取token
    private String getToken(ServerHttpRequest request){
        //获取token
        // 1. 获取全部Cookie集合
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        // 2. 根据Cookie名称拿token（登录接口Set-Cookie里的key，比如access_token）
        List<HttpCookie> tokenCookies = cookies.get(jwtConfig.getRequestTokenName());
        String token = null;
        if (tokenCookies != null && !tokenCookies.isEmpty())token = tokenCookies.getFirst().getValue();
        return token;
    }


    //检验token是否有效
    private long checkToken(String token){
        //检测token是否为空
        if(token == null||token.isEmpty())return JWTUtil.FailResult;
        TokenCheckResult result;
        //检查token是否有效
        try {result= JWTUtil.jwtCheckByList(token, jwtConfig.getHeaderSeparator(),jwtHeaders);}
        catch (Exception e){
            logger.warn("token:{},解码失败",token);
            return JWTUtil.FailResult;
        }
        if (result.getResultId()==JWTUtil.FailResult)return JWTUtil.FailResult;
        //检验redis是否存在该token
        if(stringRedisTemplate.opsForZSet().score(commonRedisKeyConfig.getLoginToken().getRedisKey(result.getResultId()), result.getToken())==null) return JWTUtil.FailResult;
        return result.getResultId();
    }


    //构建新的上下文
    private ServerWebExchange getNewExchange(ServerHttpRequest request, ServerWebExchange exchange,long ownId){
        // 构造新请求，追加解析后的用户信息到请求头
        ServerHttpRequest newReq = request.mutate()
                .headers(headers -> {
                    // 清空客户端伪造的同名header
                    headers.remove(jwtConfig.getHeaderTokenName());
                    // 新增，此时列表只有一条
                    headers.add(jwtConfig.getHeaderTokenName(), String.valueOf(ownId));
                })
                .build();
        //放入id
        return exchange.mutate().request(newReq).build();
    }


    //拒绝放行
    private Mono<Void> reject(ServerWebExchange exchange) {
        logger.warn("非法请求被FilterFilter拦截");
        ServerHttpResponse response=exchange.getResponse();
        response.setStatusCode(ErrorCodeEnum.UNAUTHORIZED.getHttpStatus());      //设置状态码
        try {
            //使返回失败结果
            return response.writeWith(Mono.just(response.bufferFactory().wrap(errorBytes)));
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            return response.setComplete();
        }
    }


}














