package com.seek.food.order.MQBind;

import com.seek.food.config.NacosConfig.MQ.DeadLetterExchangeConfig;
import com.seek.food.config.NacosConfig.MQ.MealExchangeConfig;
import com.seek.food.config.NacosConfig.MQ.OrderExchangeConfig;
import com.seek.food.util.MQ.MQUtil;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderExchangeBind {

    private final OrderExchangeConfig orderExchangeConfig;
    private final DeadLetterExchangeConfig deadLetterExchangeConfig;
    @Autowired
    public OrderExchangeBind(OrderExchangeConfig orderExchangeConfig, DeadLetterExchangeConfig deadLetterExchangeConfig) {
        this.orderExchangeConfig= orderExchangeConfig;
        this.deadLetterExchangeConfig = deadLetterExchangeConfig;
    }
    //创建一个用于该模块消息投递的交换机
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(orderExchangeConfig.getExchangeName());
    }



    //用于注册资金订单记录的队列
    @Bean
    public Queue registerFundOrderRecordQueue(){
        return MQUtil.generateQuorumQueue(orderExchangeConfig.getRegisterFundOrderRecordQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding registerFundOrderRecordBinding(Queue registerFundOrderRecordQueue, DirectExchange orderExchange){
        return BindingBuilder.bind(registerFundOrderRecordQueue).to(orderExchange).with(orderExchangeConfig.getRegisterFundOrderRecordQueue().getRoutingKey());
    }

    //用于回滚优惠券的队列
    @Bean
    public Queue rollbackVoucherQueue(){
        return MQUtil.generateQuorumQueue(orderExchangeConfig.getRollbackVoucherQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding rollbackVoucherBinding(Queue rollbackVoucherQueue, DirectExchange orderExchange){
        return BindingBuilder.bind(rollbackVoucherQueue).to(orderExchange).with(orderExchangeConfig.getRollbackVoucherQueue().getRoutingKey());
    }

    //用于回滚资金的队列
    @Bean
    public Queue rollbackFundQueue(){
        return MQUtil.generateQuorumQueue(orderExchangeConfig.getRollbackFundQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding rollbackFundBinding(Queue rollbackFundQueue, DirectExchange orderExchange){
        return BindingBuilder.bind(rollbackFundQueue).to(orderExchange).with(orderExchangeConfig.getRollbackFundQueue().getRoutingKey());
    }
    //用于打款资金的队列
    @Bean
    public Queue transferFundQueue(){
        return MQUtil.generateQuorumQueue(orderExchangeConfig.getTransferFundQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding transferFundBinding(Queue transferFundQueue, DirectExchange orderExchange){
        return BindingBuilder.bind(transferFundQueue).to(orderExchange).with(orderExchangeConfig.getTransferFundQueue().getRoutingKey());
    }
    //延时回滚一切的死信队列,绑定死信交换机，同时监听FundExchange与OrderExchange,但是实现的消费者会出现在Order模块
    @Bean
    public Queue rollbackAllFundDeadLetterQueue(){
        return MQUtil.getDeadQuorumQueue(orderExchangeConfig.getRollbackAllFundDeadLetterQueue().getName()
                ,deadLetterExchangeConfig.getExchangeName(),deadLetterExchangeConfig.getRollbackAllFundImplQueue().getRoutingKey());
    }
    //绑定交换机与队列
    @Bean
    public Binding rollbackAllFundDeadLetterBinding(Queue rollbackAllFundDeadLetterQueue, DirectExchange fundExchange){
        return BindingBuilder.bind(rollbackAllFundDeadLetterQueue).to(fundExchange).with(orderExchangeConfig.getRollbackAllFundDeadLetterQueue().getRoutingKey());
    }
    //用于改变数目的队列
    @Bean
    public Queue changeMerchantOrderAmountQueue(){
        return MQUtil.generateQuorumQueue(orderExchangeConfig.getChangeMerchantOrderAmountQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding changeMerchantOrderAmountBinding(Queue changeMerchantOrderAmountQueue, DirectExchange orderExchange){
        return BindingBuilder.bind(changeMerchantOrderAmountQueue).to(orderExchange).with(orderExchangeConfig.getChangeMerchantOrderAmountQueue().getRoutingKey());
    }
    //用于改变数目的队列
    @Bean
    public Queue changeMealSalesVolumeQueue(){
        return MQUtil.generateQuorumQueue(orderExchangeConfig.getChangeMealSalesVolumeQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding changeMealSalesVolumeBinding(Queue changeMealSalesVolumeQueue, DirectExchange orderExchange){
        return BindingBuilder.bind(changeMealSalesVolumeQueue).to(orderExchange).with(orderExchangeConfig.getChangeMealSalesVolumeQueue().getRoutingKey());
    }
    //用于改变数目的队列
    @Bean
    public Queue changeUserOrderAmountQueue(){
        return MQUtil.generateQuorumQueue(orderExchangeConfig.getChangeUserOrderAmountQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding changeUserOrderAmountBinding(Queue changeUserOrderAmountQueue, DirectExchange orderExchange){
        return BindingBuilder.bind(changeUserOrderAmountQueue).to(orderExchange).with(orderExchangeConfig.getChangeUserOrderAmountQueue().getRoutingKey());
    }
    //用于聊天室初始化的队列
    @Bean
    public Queue chatRoomInitQueue(){
        return MQUtil.generateQuorumQueue(orderExchangeConfig.getChatRoomInitQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding chatRoomInitBinding(Queue chatRoomInitQueue, DirectExchange orderExchange){
        return BindingBuilder.bind(chatRoomInitQueue).to(orderExchange).with(orderExchangeConfig.getChatRoomInitQueue().getRoutingKey());
    }
    //用于聊天室切换为完成状态的队列
    @Bean
    public Queue chatRoomCompleteQueue(){
        return MQUtil.generateQuorumQueue(orderExchangeConfig.getChatRoomCompleteQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding chatRoomCompleteBinding(Queue chatRoomCompleteQueue, DirectExchange orderExchange){
        return BindingBuilder.bind(chatRoomCompleteQueue).to(orderExchange).with(orderExchangeConfig.getChatRoomCompleteQueue().getRoutingKey());
    }
}