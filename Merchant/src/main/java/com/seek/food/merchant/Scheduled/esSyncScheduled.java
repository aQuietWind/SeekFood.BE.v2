package com.seek.food.merchant.Scheduled;

import com.seek.food.config.Data.RedisStreamData;
import com.seek.food.config.NacosConfig.MQ.MerchantExchangeConfig;
import com.seek.food.config.NacosConfig.Merchant.MerchantRedisStreamConfig;
import com.seek.food.util.FileUtil.FileRemove;
import com.seek.food.util.MQ.MQUtil;
import com.seek.food.util.Redis.RedisUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

//重新对无法删除的文件进行删除
@Component
@Slf4j
public class esSyncScheduled {
    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisStreamData esSyncStream;
    private final MerchantExchangeConfig merchantExchangeConfig;
    @Autowired
    public esSyncScheduled(StringRedisTemplate stringRedisTemplate, MerchantRedisStreamConfig merchantRedisStreamConfig, RabbitTemplate rabbitTemplate
    , MerchantExchangeConfig merchantExchangeConfig) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.esSyncStream = merchantRedisStreamConfig.getEsSyncStream();
        this.rabbitTemplate = rabbitTemplate;
        this.merchantExchangeConfig = merchantExchangeConfig;
    }
    @Scheduled(fixedDelay = 5000)
    public void esSync() {
        //通过快速方法直接进行处理
        RedisUtil.readStreamAndHandle(stringRedisTemplate,esSyncStream.getName()
        ,esSyncStream.getConsumer().getGroupName(),1,10,1,esSyncStream.getKeyName(),data->{
            long merchantId=Long.parseLong((String) data);
            try {
                //获取数据并进行处理
                MQUtil.send(merchantExchangeConfig.getExchangeName(), merchantExchangeConfig.getEsSyncMerchantQueue().getRoutingKey(),
                        merchantId,rabbitTemplate);
            }catch (Exception e){
                log.error("商家:{},在定时投放至MQ进行es同步时，发生错误:",merchantId,e);
            }
        });
    }
    @PostConstruct
    public void init(){
        RedisUtil.createStreamConsumerGroup(stringRedisTemplate,esSyncStream.getName(),esSyncStream.getConsumer().getGroupName());
    }










}
