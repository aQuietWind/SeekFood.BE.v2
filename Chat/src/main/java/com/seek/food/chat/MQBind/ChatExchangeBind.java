package com.seek.food.chat.MQBind;

import com.seek.food.config.NacosConfig.MQ.ChatExchangeConfig;
import com.seek.food.config.NacosConfig.MQ.DeadLetterExchangeConfig;
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
public class ChatExchangeBind {

    private final ChatExchangeConfig chatExchangeConfig;
    private final DeadLetterExchangeConfig deadLetterExchangeConfig;
    @Autowired
    public ChatExchangeBind(ChatExchangeConfig chatExchangeConfig, DeadLetterExchangeConfig deadLetterExchangeConfig) {
        this.chatExchangeConfig= chatExchangeConfig;
        this.deadLetterExchangeConfig = deadLetterExchangeConfig;
    }
    //创建一个用于该模块消息投递的交换机
    @Bean
    public DirectExchange chatExchange() {
        return new DirectExchange(chatExchangeConfig.getExchangeName());
    }


    //用于延时删除文件的队列
    @Bean
    public Queue deleteFileChatDeadLetterQueue(){
        return MQUtil.getDeadQuorumQueue(chatExchangeConfig.getDeleteFileChatDeadLetterQueue().getName()
                ,deadLetterExchangeConfig.getExchangeName(),deadLetterExchangeConfig.getDeleteFileChatImplQueue().getRoutingKey());
    }
    //绑定交换机与队列
    @Bean
    public Binding deleteFileChatDeadLetterBinding(Queue deleteFileChatDeadLetterQueue, DirectExchange chatExchange){
        return BindingBuilder.bind(deleteFileChatDeadLetterQueue).to(chatExchange).with(chatExchangeConfig.getDeleteFileChatDeadLetterQueue().getRoutingKey());
    }
    //用于通知webSocket信息的队列
    @Bean
    public Queue chatInformQueue(){
        return MQUtil.generateQuorumQueue(chatExchangeConfig.getChatInformQueue().getName());
    }
    //绑定交换机与队列
    @Bean
    public Binding chatInformBinding(Queue chatInformQueue, DirectExchange chatExchange){
        return BindingBuilder.bind(chatInformQueue).to(chatExchange).with(chatExchangeConfig.getChatInformQueue().getRoutingKey());
    }



















}