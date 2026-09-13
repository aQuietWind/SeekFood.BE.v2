package com.seek.food.user.MQBind;

import com.seek.food.config.NacosConfig.MQ.UserExchangeConfig;
import com.seek.food.util.MQ.MQUtil;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class UserExchangeBind {

    private final UserExchangeConfig userExchangeConfig;
    @Autowired
    public UserExchangeBind(UserExchangeConfig userExchangeConfig) {
        this.userExchangeConfig = userExchangeConfig;
    }
    //创建一个用于用户消息投递的交换机
    @Bean
    public DirectExchange userExchange(){
        return new DirectExchange(userExchangeConfig.getExchangeName());
    }
    //用户注册资金队列
    @Bean
    public Queue registerFundQueue(){
        return MQUtil.generateQuorumQueue(userExchangeConfig.getRegisterFundQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding registerFundBinding(Queue registerFundQueue, DirectExchange userExchange){
        return BindingBuilder.bind(registerFundQueue).to(userExchange).with(userExchangeConfig.getRegisterFundQueue().getRoutingKey());
    }
    //用户文件删除队列
    @Bean
    public Queue deleteFileUserQueue(){
        return MQUtil.generateQuorumQueue(userExchangeConfig.getDeleteFileUserQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding deleteFileUserBinding(Queue deleteFileUserQueue, DirectExchange userExchange){
        return BindingBuilder.bind(deleteFileUserQueue).to(userExchange).with(userExchangeConfig.getDeleteFileUserQueue().getRoutingKey());
    }
    //用户删除时资金操作队列
    @Bean
    public Queue deleteFundQueue(){
        return MQUtil.generateQuorumQueue(userExchangeConfig.getDeleteFundQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding deleteFundBinding(Queue deleteFundQueue, DirectExchange userExchange){
        return BindingBuilder.bind(deleteFundQueue).to(userExchange).with(userExchangeConfig.getDeleteFundQueue().getRoutingKey());
    }













}
