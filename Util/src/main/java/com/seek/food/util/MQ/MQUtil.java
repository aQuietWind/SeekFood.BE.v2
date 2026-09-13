package com.seek.food.util.MQ;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class MQUtil {

    //产生CorrelationData
    public static CorrelationData getCorrelation(String exChangeName,String routingKey){
        //生成一个带有随机id的correlationData
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        //设置回调函数
        correlationData.getFuture().whenComplete((r,e)->{
            if (e!=null)log.error("Exchange:{}发送消息到RoutingKey:{},发生异常",exChangeName,routingKey,e);
            if (!r.isAck())log.error("Exchange:{}发送消息到RoutingKey:{},未能成功到达交换机",exChangeName,routingKey);
        });
        return correlationData;
    }

    //产生CorrelationData
    public static CorrelationData getCorrelationWithId(String exChangeName,String routingKey,String id){
        //生成一个带有指定id的correlationData
        CorrelationData correlationData = new CorrelationData(id);
        //设置回调函数
        correlationData.getFuture().whenComplete((r,e)->{
            if (e!=null)log.error("Exchange:{}发送消息到RoutingKey:{},发生异常",exChangeName,routingKey,e);
            if (!r.isAck())log.error("Exchange:{}发送消息到RoutingKey:{},未能成功到达交换机",exChangeName,routingKey);
        });
        return correlationData;
    }


    //发送消息
    public static void send(String exchangeName, String routingKey, Object message, RabbitTemplate rabbitTemplate){
        rabbitTemplate.convertAndSend(exchangeName,routingKey,message,getCorrelation(exchangeName,routingKey));
    }

    //发送带过期时间的消息并且返回letterId使用
    public static String sendWithTLLAndGetId(String exchangeName, String routingKey, Object message, RabbitTemplate rabbitTemplate
    ,String tllMilliSeconds){
        String letterId=UUID.randomUUID().toString();
        rabbitTemplate.convertAndSend(exchangeName,routingKey
                ,message
                , msg->{
            //设置ttl
            msg.getMessageProperties().setExpiration(tllMilliSeconds);
            msg.getMessageProperties().setMessageId(letterId);
            return msg;}
                ,getCorrelationWithId(exchangeName,routingKey,letterId));
        return letterId;
    }

    //发送带过期时间的消息
    public static void sendWithTLL(String exchangeName, String routingKey, Object message, RabbitTemplate rabbitTemplate
    ,String tllMilliSeconds){
        rabbitTemplate.convertAndSend(exchangeName,routingKey
                ,message
                , msg->{
            //设置ttl
            msg.getMessageProperties().setExpiration(tllMilliSeconds);
            return msg;}
                ,getCorrelation(exchangeName,routingKey));
    }

    //生成一个仲裁队列
    public static Queue generateQuorumQueue(String queueName){
        Map<String, Object> args = new HashMap<>();
        //设置队列模式为quorum仲裁模式
        args.put("x-queue-type", "quorum");
        return new Queue(queueName, true, false, false, args);
    }

    //生成一个死信仲裁队列
    public static Queue getDeadQuorumQueue(String queueName,String deadLetterExchangeName,String deadLetterRoutingKey){
        Map<String, Object> args = new HashMap<>();
        //设置队列模式为quorum仲裁模式
        args.put("x-queue-type", "quorum");
        //绑定死信交换机
        args.put("x-dead-letter-exchange", deadLetterExchangeName);
        //死信转发时使用的routingKey
        args.put("x-dead-letter-routing-key", deadLetterRoutingKey);
        return new Queue(queueName, true, false, false, args);
    }

    //分钟转毫秒
    public static String minuteToMillis(int minute){
        return ""+minute*60*1000;
    }

}
