package com.seek.food.redisutil.Redis;


import com.seek.food.configobject.RedisData.RedisStreamData;
import com.seek.food.configobject.UtilObject.Function.RunWithParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.List;

@Slf4j
@Lazy
public class RedisStreamUtil {


    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public RedisStreamUtil(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    //快速从Stream消费者组获取消息
    public List<MapRecord<String,Object,Object>> readStreamLastest(RedisStreamData stream, String consumerGroupName, int consumerId
            , int count, int waitSeconds){
        return stringRedisTemplate.opsForStream().read(
                Consumer.from(consumerGroupName,"consumer"+consumerId),     //指定消费者
                StreamReadOptions.empty().count(count).block(Duration.ofSeconds(waitSeconds)),    //指定最大数与阻塞时间
                StreamOffset.create(stream.getName(), ReadOffset.lastConsumed())      //指定目标stream与目标索引为最后一条未处理
        );      //返回值是一个定义好的MapRecord的List形式
    }

    //快速进行Stream获取消息并且消费,直至无法获取到消息
    public void readStreamAndHandle(RedisStreamData stream, String consumerGroupName, int consumerId
            , int count, int waitSeconds, RunWithParam<Object> handler){
        for (;true;) {
            List<MapRecord<String, Object, Object>> records = readStreamLastest(stream, consumerGroupName, consumerId, count, waitSeconds);
            if (records.isEmpty()) break;
            //以下为业务逻辑
            for (MapRecord<String, Object, Object> record : records) {
                handler.function(record.getValue().get(stream.getMessageKey()));
                //通过id确认消息
                stringRedisTemplate.opsForStream().acknowledge(stream.getMessageKey(),consumerGroupName,record.getId());
            }
        }
    }

    //创建消费者组
    public void createStreamConsumerGroup(RedisStreamData stream,String consumerGroupName){
        //可能已经存在生产者组
        try {
            stringRedisTemplate.opsForStream().createGroup(stream.getName(),consumerGroupName);
        }catch (Exception ignored){}
    }

}
