package com.seek.food.config.NativeConfig.Interceptor;

import com.seek.food.config.NacosConfig.Common.JWTConfig;
import com.seek.food.util.Context.TokenIdContext;
import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

// 仅普通Servlet微服务生效，WebFlux网关不会实例化这个类
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Configuration
//用于tokenId之间的传递
public class FeignTokenInterceptor {
    //构造器注入
    private final JWTConfig jwtConfig;
    private static final Logger logger = LoggerFactory.getLogger(FeignTokenInterceptor.class);
    @Autowired
    public FeignTokenInterceptor(JWTConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @Bean
    public RequestInterceptor tokenHeaderInterceptor() {
        return (template) -> {
            //获取当前Http上下文，并检查
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes == null)return;
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            //从tokenId线程上下文中取出tokenId
            String tokenId = TokenIdContext.get();
            logger.info("tokenId:{} ,调用了feign",tokenId);
            if (tokenId != null && !tokenId.isBlank()) {
                //Feign发起远程调用时自动带上该Header
                template.header(jwtConfig.getHeaderTokenName(), tokenId);
                //用于sentinel检测
                template.header("x-origin", "service");
            }
        };
    }
}