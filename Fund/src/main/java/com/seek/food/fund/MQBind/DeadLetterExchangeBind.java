package com.seek.food.fund.MQBind;

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
    //回滚订单有关一切的队列
    @Bean
    public Queue rollbackAllFundImplQueue(){
        return MQUtil.generateQuorumQueue(deadLetterExchangeConfig.getRollbackAllFundImplQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding rollbackAllFundImplBinding(Queue rollbackAllFundImplQueue, DirectExchange deadLetterExchange){
        return BindingBuilder.bind(rollbackAllFundImplQueue).to(deadLetterExchange).with(deadLetterExchangeConfig.getRollbackAllFundImplQueue().getRoutingKey());
    }
}
