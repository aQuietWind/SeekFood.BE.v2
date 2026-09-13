package com.seek.food.rider.MQBind;

import com.seek.food.config.NacosConfig.MQ.RiderExchangeConfig;
import com.seek.food.config.NacosConfig.MQ.UserExchangeConfig;
import com.seek.food.util.MQ.MQUtil;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RiderExchangeBind {

    private final RiderExchangeConfig riderExchangeConfig;
    @Autowired
    public RiderExchangeBind(RiderExchangeConfig riderExchangeConfig) {
        this.riderExchangeConfig = riderExchangeConfig;
    }
    //创建一个用于该模块消息投递的交换机
    @Bean
    public DirectExchange riderExchange(){
        return new DirectExchange(riderExchangeConfig.getExchangeName());
    }
    //资金账户注册队列
    @Bean
    public Queue registerFundQueue(){
        return MQUtil.generateQuorumQueue(riderExchangeConfig.getRegisterFundQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding registerFundBinding(Queue registerFundQueue, DirectExchange riderExchange){
        return BindingBuilder.bind(registerFundQueue).to(riderExchange).with(riderExchangeConfig.getRegisterFundQueue().getRoutingKey());
    }
    //资金账户删除队列
    @Bean
    public Queue deleteFundQueue(){
        return MQUtil.generateQuorumQueue(riderExchangeConfig.getDeleteFundQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding deleteFundBinding(Queue deleteFundQueue, DirectExchange riderExchange){
        return BindingBuilder.bind(deleteFundQueue).to(riderExchange).with(riderExchangeConfig.getDeleteFundQueue().getRoutingKey());
    }
    //文件删除队列
    @Bean
    public Queue deleteFileRiderQueue(){
        return MQUtil.generateQuorumQueue(riderExchangeConfig.getDeleteFileRiderQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding deleteFileRiderBinding(Queue deleteFileRiderQueue, DirectExchange riderExchange){
        return BindingBuilder.bind(deleteFileRiderQueue).to(riderExchange).with(riderExchangeConfig.getDeleteFileRiderQueue().getRoutingKey());
    }





}
