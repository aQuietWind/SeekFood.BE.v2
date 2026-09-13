package com.seek.food.chat.MQBind;

import com.seek.food.config.NacosConfig.MQ.DeadLetterExchangeConfig;
import com.seek.food.util.MQ.MQUtil;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeadLetterExchangeBind {

    private final DeadLetterExchangeConfig deadLetterExchangeConfig;
    @Autowired
    public DeadLetterExchangeBind(DeadLetterExchangeConfig deadLetterExchangeConfig) {
        this.deadLetterExchangeConfig = deadLetterExchangeConfig;
    }

    //创建死信交换机
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(deadLetterExchangeConfig.getExchangeName());
    }

    //用于删除文件的队列
    @Bean
    public Queue deleteFileChatImplQueue(){
        return MQUtil.generateQuorumQueue(deadLetterExchangeConfig.getDeleteFileChatImplQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding deleteFileChatImplBinding(Queue deleteFileChatImplQueue, DirectExchange chatExchange){
        return BindingBuilder.bind(deleteFileChatImplQueue).to(chatExchange).with(deadLetterExchangeConfig.getDeleteFileChatImplQueue().getRoutingKey());
    }
}
