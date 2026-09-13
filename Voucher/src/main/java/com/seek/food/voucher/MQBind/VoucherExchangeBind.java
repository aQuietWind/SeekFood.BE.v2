package com.seek.food.voucher.MQBind;

import com.seek.food.config.NacosConfig.MQ.VoucherExchangeConfig;
import com.seek.food.util.MQ.MQUtil;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class VoucherExchangeBind {

    private final VoucherExchangeConfig voucherExchangeConfig;
    @Autowired
    public VoucherExchangeBind(VoucherExchangeConfig voucherExchangeConfig) {
        this.voucherExchangeConfig = voucherExchangeConfig;
    }
    //创建一个用于优惠券的交换机
    @Bean
    public DirectExchange voucherExchange(){
        return new DirectExchange(voucherExchangeConfig.getExchangeName());
    }
    //订单确认队列
    @Bean
    public Queue orderAckQueue(){
        return MQUtil.generateQuorumQueue(voucherExchangeConfig.getOrderAckQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding orderAckBinding(Queue orderAckQueue, DirectExchange voucherExchange){
        return BindingBuilder.bind(orderAckQueue).to(voucherExchange).with(voucherExchangeConfig.getOrderAckQueue().getRoutingKey());
    }













}
