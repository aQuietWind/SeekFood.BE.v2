package com.seek.food.merchant.Consumer;

import com.seek.food.config.Data.RedisStreamData;
import com.seek.food.config.Enum.MQNameKeyEnum;
import com.seek.food.config.NacosConfig.Merchant.MerchantRedisStreamConfig;
import com.seek.food.config.NacosConfig.User.UserParamsRulesConfig;
import com.seek.food.config.NacosConfig.User.UserRedisStreamConfig;
import com.seek.food.util.FileUtil.FileRemove;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class DeleteFileMerchantConsumer {
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisStreamData oldFileStream;
    @Autowired
    public DeleteFileMerchantConsumer(StringRedisTemplate stringRedisTemplate, MerchantRedisStreamConfig merchantRedisStreamConfig) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.oldFileStream = merchantRedisStreamConfig.getOldFileStream();
    }


    @RabbitListener(queues = MQNameKeyEnum.Merchant_Exchange_Delete_File_Queue)
    public void deleteFileMerchantQueue(String path){
        try {
            FileRemove.removeFileByPath(path);
        }catch (Exception e){
            //不再进行重试，而是留给spring后台线程进行定时处理
            log.error("path:{},在删除时发生错误",path,e);
            stringRedisTemplate.opsForStream().add(oldFileStream.getName(),Map.of(oldFileStream.getKeyName(),path));
        }
    }
}
