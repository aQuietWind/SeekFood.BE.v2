package com.seek.food.config.NativeConfig.MQConfig;

import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 仅普通Servlet微服务生效，WebFlux网关不会实例化这个类
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Configuration
public class RabbitMQConfig {
    //序列化工具，用于对复杂的Object传递
    @Bean
    public MessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        // 1. 添加你的DTO根包（多个包用逗号分隔）
        typeMapper.setTrustedPackages("com.seek.food");
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }
}
