package com.seek.food.interaction.MQBind;

import com.seek.food.config.NacosConfig.MQ.InteractionExchangeConfig;
import com.seek.food.util.MQ.MQUtil;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class InteractionExchangeBind {

    private final InteractionExchangeConfig interactionExchangeConfig;
    @Autowired
    public InteractionExchangeBind(InteractionExchangeConfig interactionExchangeConfig) {
        this.interactionExchangeConfig = interactionExchangeConfig;
    }
    //创建一个用于交互模块消息投递的交换机
    @Bean
    public DirectExchange interactionExchange(){
        return new DirectExchange(interactionExchangeConfig.getExchangeName());
    }
    //更改商家点赞数目队列
    @Bean
    public Queue changeMerchantLikeAmountQueue(){
        return MQUtil.generateQuorumQueue(interactionExchangeConfig.getChangeMerchantLikeAmountQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding changeMerchantLikeAmountBinding(Queue changeMerchantLikeAmountQueue, DirectExchange interactionExchange){
        return BindingBuilder.bind(changeMerchantLikeAmountQueue).to(interactionExchange).with(interactionExchangeConfig.getChangeMerchantLikeAmountQueue().getRoutingKey());
    }
    //更改一级评论点赞数目队列
    @Bean
    public Queue changeFirstCommentLikeAmountQueue(){
        return MQUtil.generateQuorumQueue(interactionExchangeConfig.getChangeFirstCommentLikeAmountQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding changeFirstCommentLikeAmountBinding(Queue changeFirstCommentLikeAmountQueue, DirectExchange interactionExchange){
        return BindingBuilder.bind(changeFirstCommentLikeAmountQueue).to(interactionExchange).with(interactionExchangeConfig.getChangeFirstCommentLikeAmountQueue().getRoutingKey());
    }
    //更改二级评论点赞数目队列
    @Bean
    public Queue changeSecondCommentLikeAmountQueue(){
        return MQUtil.generateQuorumQueue(interactionExchangeConfig.getChangeSecondCommentLikeAmountQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding changeSecondCommentLikeAmountBinding(Queue changeSecondCommentLikeAmountQueue, DirectExchange interactionExchange){
        return BindingBuilder.bind(changeSecondCommentLikeAmountQueue).to(interactionExchange).with(interactionExchangeConfig.getChangeSecondCommentLikeAmountQueue().getRoutingKey());
    }
    //更改商家收藏数目队列
    @Bean
    public Queue changeMerchantCollectAmountQueue(){
        return MQUtil.generateQuorumQueue(interactionExchangeConfig.getChangeMerchantCollectAmountQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding changeMerchantCollectAmountBinding(Queue changeMerchantCollectAmountQueue, DirectExchange interactionExchange){
        return BindingBuilder.bind(changeMerchantCollectAmountQueue).to(interactionExchange).with(interactionExchangeConfig.getChangeMerchantCollectAmountQueue().getRoutingKey());
    }
    //同步点赞状态队列
    @Bean
    public Queue syncLikeStateQueue(){
        return MQUtil.generateQuorumQueue(interactionExchangeConfig.getSyncLikeStateQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding syncLikeStateBinding(Queue syncLikeStateQueue, DirectExchange interactionExchange){
        return BindingBuilder.bind(syncLikeStateQueue).to(interactionExchange).with(interactionExchangeConfig.getSyncLikeStateQueue().getRoutingKey());
    }
    //同步收藏状态队列
    @Bean
    public Queue syncCollectStateQueue(){
        return MQUtil.generateQuorumQueue(interactionExchangeConfig.getSyncCollectStateQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding syncCollectStateBinding(Queue syncCollectStateQueue, DirectExchange interactionExchange){
        return BindingBuilder.bind(syncCollectStateQueue).to(interactionExchange).with(interactionExchangeConfig.getSyncCollectStateQueue().getRoutingKey());
    }













}
