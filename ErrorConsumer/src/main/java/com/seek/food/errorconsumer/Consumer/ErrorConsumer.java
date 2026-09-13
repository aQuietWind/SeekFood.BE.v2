package com.seek.food.errorconsumer.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ErrorConsumer {
    private static final Logger log = LoggerFactory.getLogger(ErrorConsumer.class);
    @RabbitListener(queues = "${mq.name.bind.error-exchange.error-queue.name}")
    public void errorHandler(Message msg) {
        Map<String,Object> header=msg.getMessageProperties().getHeaders();
        log.error("""
                  交换机:{}，在投递key:{}时，发生异常:{},
                  完整异常为:{}"""
                        ,
                header.get("x-original-exchange"),      //获取交换机
                header.get("x-original-routingKey"),        //获取路由key
                header.get("x-exception-message"),           //获取异常信息
                header.get("x-exception-stacktrace")           //获取异常信息
        );

    }
}
