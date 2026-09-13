package com.seek.food.admin.MQBind;

import com.seek.food.config.NacosConfig.MQ.AdminExchangeConfig;
import com.seek.food.util.MQ.MQUtil;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AdminExchangeBind {

    private final AdminExchangeConfig adminExchangeConfig;
    @Autowired
    public AdminExchangeBind(AdminExchangeConfig adminExchangeConfig) {
        this.adminExchangeConfig = adminExchangeConfig;
    }
    //创建一个用于管理员模块消息投递的交换机
    @Bean
    public DirectExchange adminExchange(){
        return new DirectExchange(adminExchangeConfig.getExchangeName());
    }
    //删除文件队列
    @Bean
    public Queue deleteFileAdminQueue(){
        return MQUtil.generateQuorumQueue(adminExchangeConfig.getDeleteFileAdminQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding deleteFileAdminBinding(Queue deleteFileAdminQueue, DirectExchange adminExchange){
        return BindingBuilder.bind(deleteFileAdminQueue).to(adminExchange).with(adminExchangeConfig.getDeleteFileAdminQueue().getRoutingKey());
    }












}
