package com.seek.food.config.NativeConfig.MQConfig;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;


//该类用于交换机无法将信息发送给队列的情况
@Configuration
//实现接口是为了使其在SpringBoot初始化后调用这一重写方法，达到注入Bean的作用
// 仅普通Servlet微服务生效，WebFlux网关不会实例化这个类
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class MQCommonConfig implements ApplicationContextAware {
    private static final Logger log = LoggerFactory.getLogger(MQCommonConfig.class);

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //获取之前注入的rabbitTemplate
        RabbitTemplate rabbitTemplate = applicationContext.getBean(RabbitTemplate.class);
        //当交换机无法将消息发送给队列时触发
        rabbitTemplate.setReturnsCallback(all->{
            log.error("消息{}从交换机{}通过路由key:{}发送给队列{}失败,code:{}",
                    all.getMessage(),
                    all.getExchange(),
                    all.getRoutingKey(),
                    all.getReplyText(),
                    all.getReplyCode());
        });
    }
}

