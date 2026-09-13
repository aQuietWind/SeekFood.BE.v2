package com.seek.food.promotion.MQBind;

import com.seek.food.config.NacosConfig.MQ.OrderExchangeConfig;
import com.seek.food.config.NacosConfig.MQ.PromotionExchangeConfig;
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
public class PromotionExchangeBind {

    private final PromotionExchangeConfig promotionExchangeConfig;
    @Autowired
    public PromotionExchangeBind(PromotionExchangeConfig promotionExchangeConfig) {
        this.promotionExchangeConfig = promotionExchangeConfig;
    }
    //创建一个该模块的交换机,用于监听该模块的行为
    @Bean
    public DirectExchange promotionExchange() {
        return new DirectExchange(promotionExchangeConfig.getExchangeName());
    }

    //文件删除队列
    @Bean
    public Queue registerVoucherConnectionQueue(){
        return MQUtil.generateQuorumQueue(promotionExchangeConfig.getRegisterVoucherConnectionQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding registerVoucherConnectionBinding(Queue registerVoucherConnectionQueue, DirectExchange promotionExchange){
        return BindingBuilder.bind(registerVoucherConnectionQueue).to(promotionExchange).with(promotionExchangeConfig.getRegisterVoucherConnectionQueue().getRoutingKey());
    }
}
