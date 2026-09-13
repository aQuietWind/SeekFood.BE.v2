package com.seek.food.meal.MQBind;

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
    //餐品文件删除队列
    @Bean
    public Queue deleteFileMealImplQueue(){
        return MQUtil.generateQuorumQueue(deadLetterExchangeConfig.getDeleteFileMealImplQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding deleteFileMealImplBinding(Queue deleteFileMealImplQueue, DirectExchange deadLetterExchange){
        return BindingBuilder.bind(deleteFileMealImplQueue).to(deadLetterExchange).with(deadLetterExchangeConfig.getDeleteFileMealImplQueue().getRoutingKey());
    }
    //批量餐品文件删除队列
    @Bean
    public Queue deleteAllFileMealImplQueue(){
        return MQUtil.generateQuorumQueue(deadLetterExchangeConfig.getDeleteAllFileMealImplQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding deleteAllFileMealImplBinding(Queue deleteAllFileMealImplQueue, DirectExchange deadLetterExchange){
        return BindingBuilder.bind(deleteAllFileMealImplQueue).to(deadLetterExchange).with(deadLetterExchangeConfig.getDeleteAllFileMealImplQueue().getRoutingKey());
    }
}
