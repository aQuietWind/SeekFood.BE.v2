package com.seek.food.employee.MQBind;

import com.seek.food.config.NacosConfig.MQ.EmployeeExchangeConfig;
import com.seek.food.util.MQ.MQUtil;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RefreshScope
public class EmployeeExchangeBind {

    private final EmployeeExchangeConfig employeeExchangeConfig;
    @Autowired
    public EmployeeExchangeBind(EmployeeExchangeConfig employeeExchangeConfig) {
        this.employeeExchangeConfig = employeeExchangeConfig;
    }
    //创建一个用于职员消息投递的交换机
    @Bean
    public DirectExchange employeeExchange() {
        return new DirectExchange(employeeExchangeConfig.getExchangeName());
    }
    //职员数量改变队列
    @Bean
    public Queue changeEmployeeAmountQueue(){
        return MQUtil.generateQuorumQueue(employeeExchangeConfig.getChangeEmployeeAmountQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding changeEmployeeAmountBinding(Queue changeEmployeeAmountQueue, DirectExchange employeeExchange){
        return BindingBuilder.bind(changeEmployeeAmountQueue).to(employeeExchange).with(employeeExchangeConfig.getChangeEmployeeAmountQueue().getRoutingKey());
    }
    //文件删除队列
    @Bean
    public Queue deleteFileEmployeeQueue(){
        return MQUtil.generateQuorumQueue(employeeExchangeConfig.getDeleteFileEmployeeQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding deleteFileEmployeeBinding(Queue deleteFileEmployeeQueue, DirectExchange employeeExchange){
        return BindingBuilder.bind(deleteFileEmployeeQueue).to(employeeExchange).with(employeeExchangeConfig.getDeleteFileEmployeeQueue().getRoutingKey());
    }
}
