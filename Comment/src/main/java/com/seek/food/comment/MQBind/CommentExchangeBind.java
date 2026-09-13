package com.seek.food.comment.MQBind;

import com.seek.food.config.NacosConfig.MQ.CommentExchangeConfig;
import com.seek.food.util.MQ.MQUtil;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class CommentExchangeBind {

    private final CommentExchangeConfig commentExchangeConfig;
    @Autowired
    public CommentExchangeBind(CommentExchangeConfig commentExchangeConfig) {
        this.commentExchangeConfig = commentExchangeConfig;
    }
    //创建一个用于评论模块消息投递的交换机
    @Bean
    public DirectExchange commentExchange(){
        return new DirectExchange(commentExchangeConfig.getExchangeName());
    }
    //删除文件队列
    @Bean
    public Queue deleteFileCommentQueue(){
        return MQUtil.generateQuorumQueue(commentExchangeConfig.getDeleteFileCommentQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding deleteFileCommentBinding(Queue deleteFileCommentQueue, DirectExchange commentExchange){
        return BindingBuilder.bind(deleteFileCommentQueue).to(commentExchange).with(commentExchangeConfig.getDeleteFileCommentQueue().getRoutingKey());
    }
    //改变商家的一级评论数队列
    @Bean
    public Queue changeMerchantFirstCommentAmountQueue(){
        return MQUtil.generateQuorumQueue(commentExchangeConfig.getChangeMerchantFirstCommentAmountQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding changeMerchantFirstCommentAmountBinding(Queue changeMerchantFirstCommentAmountQueue, DirectExchange commentExchange){
        return BindingBuilder.bind(changeMerchantFirstCommentAmountQueue).to(commentExchange).with(commentExchangeConfig.getChangeMerchantFirstCommentAmountQueue().getRoutingKey());
    }
    //改变一级评论的二级评论数队列
    @Bean
    public Queue changeSecondCommentAmountQueue(){
        return MQUtil.generateQuorumQueue(commentExchangeConfig.getChangeSecondCommentAmountQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding changeSecondCommentAmountBinding(Queue changeSecondCommentAmountQueue, DirectExchange commentExchange){
        return BindingBuilder.bind(changeSecondCommentAmountQueue).to(commentExchange).with(commentExchangeConfig.getChangeSecondCommentAmountQueue().getRoutingKey());
    }













}
