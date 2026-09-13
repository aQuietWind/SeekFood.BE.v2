package com.seek.food.meal.MQBind;

import com.seek.food.config.NacosConfig.MQ.DeadLetterExchangeConfig;
import com.seek.food.config.NacosConfig.MQ.MealExchangeConfig;
import com.seek.food.util.MQ.MQUtil;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MealExchangeBind {

    private final MealExchangeConfig mealExchangeConfig;
    private final DeadLetterExchangeConfig deadLetterExchangeConfig;
    @Autowired
    public MealExchangeBind(MealExchangeConfig mealExchangeConfig,DeadLetterExchangeConfig deadLetterExchangeConfig) {
        this.mealExchangeConfig= mealExchangeConfig;
        this.deadLetterExchangeConfig = deadLetterExchangeConfig;
    }
    //创建一个用于餐品消息投递的交换机
    @Bean
    public DirectExchange mealExchange() {
        return new DirectExchange(mealExchangeConfig.getExchangeName());
    }



    //文件删除队列
    @Bean
    public Queue deleteFileMealQueue(){
        return MQUtil.generateQuorumQueue(mealExchangeConfig.getDeleteFileMealQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding deleteFileMealBinding(Queue deleteFileMealQueue, DirectExchange mealExchange){
        return BindingBuilder.bind(deleteFileMealQueue).to(mealExchange).with(mealExchangeConfig.getDeleteFileMealQueue().getRoutingKey());
    }


    //文件死信队列,绑定死信交换机，同时监听MealExchange
    @Bean
    public Queue deleteFileMealDeadLetterQueue(){
        return MQUtil.getDeadQuorumQueue(mealExchangeConfig.getDeleteFileMealDeadLetterQueue().getName()
                ,deadLetterExchangeConfig.getExchangeName(),deadLetterExchangeConfig.getDeleteFileMealImplQueue().getRoutingKey());
    }
    //绑定交换机与队列
    @Bean
    public Binding deleteMealDeadLetterBinding(Queue deleteFileMealDeadLetterQueue, DirectExchange mealExchange){
        return BindingBuilder.bind(deleteFileMealDeadLetterQueue).to(mealExchange).with(mealExchangeConfig.getDeleteFileMealDeadLetterQueue().getRoutingKey());
    }


    //批量文件删除死信队列
    @Bean
    public Queue deleteAllFileMealDeadLetterQueue(){
        return MQUtil.getDeadQuorumQueue(mealExchangeConfig.getDeleteAllFileMealDeadLetterQueue().getName()
        ,deadLetterExchangeConfig.getExchangeName(),deadLetterExchangeConfig.getDeleteAllFileMealImplQueue().getRoutingKey());
    }
    //绑定交换机与队列
    @Bean
    public Binding deleteAllFileMealDeadLetterBinding(Queue deleteAllFileMealDeadLetterQueue, DirectExchange mealExchange){
        return BindingBuilder.bind(deleteAllFileMealDeadLetterQueue).to(mealExchange).with(mealExchangeConfig.getDeleteAllFileMealDeadLetterQueue().getRoutingKey());
    }
}