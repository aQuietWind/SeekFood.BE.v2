package com.seek.food.config.NativeConfig.CommonConfig;

import com.seek.food.config.NativeConfig.Interceptor.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 仅普通Servlet微服务生效，WebFlux网关不会实例化这个类
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Configuration          //声明这是一个对项目进行配置的配置类
public class WebConfig implements WebMvcConfigurer {            //实现
    @Autowired          //依赖注入
    private TokenInterceptor tokenInterceptor;          //获取拦截器的对象
    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/**");        //添加拦截器，且声明该拦截器将拦截所有请求，"/*"只能匹配一级路径，不能匹配"/xx1/xx2/..."

    }
}
