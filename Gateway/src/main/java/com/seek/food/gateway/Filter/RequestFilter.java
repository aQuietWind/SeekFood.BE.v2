package com.seek.food.gateway.Filter;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.google.gson.Gson;
import com.seek.food.config.NacosConfig.Common.JWTConfig;
import com.seek.food.dto.Common.Result;
import com.seek.food.util.Exception.ErrorCodeEnum;
import com.seek.food.config.NacosConfig.Gateway.GatewayBlackConfig;
import com.seek.food.config.NacosConfig.Gateway.GatewayRedisKeyConfig;
import com.seek.food.gateway.Caffeine.BlackIdCaffeine;
import com.seek.food.gateway.Caffeine.BlackIpCaffeine;
import com.seek.food.util.TimeUtil.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@Order(2)
@Component
@RefreshScope
public class RequestFilter implements GlobalFilter{

    //构造器注入
    private final StringRedisTemplate stringRedisTemplate;
    private final GatewayRedisKeyConfig gatewayRedisKeyConfig;
    private final BlackIpCaffeine blackIpCaffeine;
    private final BlackIdCaffeine blackIdCaffeine;
    private final JWTConfig jwtConfig;
    private final int blackIpTimes;
    private final int blackIpDuration;
    private final int blackIdTimes;
    private final int blackIdDuration;
    //字节码化的Result
    private final static byte[] errorBytes = new Gson().toJson(Result.error(ErrorCodeEnum.UNAUTHORIZED)).getBytes();
    private static final Logger logger = LoggerFactory.getLogger(RequestFilter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    // 构造器注入
    public RequestFilter(StringRedisTemplate stringRedisTemplate, GatewayRedisKeyConfig gatewayRedisKeyConfig, BlackIpCaffeine blackIpCaffeine
    , BlackIdCaffeine blackIdCaffeine, GatewayBlackConfig gatewayBlackConfig, JWTConfig jwtConfig) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.gatewayRedisKeyConfig = gatewayRedisKeyConfig;
        this.blackIpCaffeine = blackIpCaffeine;
        this.blackIdCaffeine = blackIdCaffeine;
        this.jwtConfig = jwtConfig;
        this.blackIpTimes= gatewayBlackConfig.getIp().getCounts();
        this.blackIdTimes= gatewayBlackConfig.getId().getCounts();
        this.blackIpDuration= gatewayBlackConfig.getIp().getDuration();
        this.blackIdDuration= gatewayBlackConfig.getId().getDuration();
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain){
        ServerHttpRequest request = exchange.getRequest();
        String tokenId = request.getHeaders().getFirst(jwtConfig.getHeaderTokenName());
        //检验是不是去公开路径的,并检查ip是否处于黑名单
        if (("".equals(tokenId)||tokenId==null)&&ipCheck(request))return chain.filter(exchange);
        else if (tokenId!=null&&idRecord(tokenId))return chain.filter(exchange);
        logger.warn("requestFilter拒绝请求");
        return reject(exchange);
    }

    //ip名单校验
    private boolean ipCheck(ServerHttpRequest request){
        //获取ip
        String ip=request.getHeaders().getFirst("X-Forwarded-For");
        if(ip==null||ip.equals("unknown")||ip.isBlank()){
            return false;
        }
        //进行ip检查
        return recordCount(
                blackIpCaffeine.getCACHE(),
                gatewayRedisKeyConfig.getIpBlack(),
                ip,
                gatewayRedisKeyConfig.getIpCheck(),
                blackIpTimes,
                blackIpDuration
                );      //检查名单
    }

    //id记录
    private boolean idRecord(String id){
        return recordCount(
                blackIdCaffeine.getCACHE(),
                gatewayRedisKeyConfig.getIdBlack(),
                id,
                gatewayRedisKeyConfig.getIdCheck(),
                blackIdTimes,
                blackIdDuration
        );
    }

    //检查流程
    private boolean recordCount(Cache<String,Long> cache,String redisBlackKey,String value,String redisRecordKey,int maxCounts,int blackDuration){
        //检查caffeine和redis中是否存在黑名单
        if (cache.get(value,key-> {
                String redisCheckResult =stringRedisTemplate.opsForValue().get(redisBlackKey+value);
                return redisCheckResult==null?null:Long.parseLong(redisCheckResult);})!=null)return false;
        //自增来访次数并检查
        long count=stringRedisTemplate.opsForValue().increment(redisRecordKey+value);
        if (count>= maxCounts){
            //持续时间戳
            long aimStamp= TimeUtil.getPlusHoursStampByNow(blackDuration);
            //封禁并且设置有效期
            stringRedisTemplate.opsForValue().set(redisBlackKey+value, ""+aimStamp,blackDuration, TimeUnit.HOURS);
            //回写jvm缓存
            cache.put(value,aimStamp);
            //拒绝放行
            return false;
        }
        //第一次来访设置1分钟有效期
        if (count==1)stringRedisTemplate.expire(redisRecordKey+value,1,TimeUnit.MINUTES);
        //同意放行
        return true;
    }

    //拒绝放行
    private Mono<Void> reject(ServerWebExchange exchange) {
        logger.warn("非法请求被RequestFilter拦截");
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
