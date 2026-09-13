package com.seek.food.chat.Scheduled;

import com.seek.food.config.Data.RedisStreamData;
import com.seek.food.config.NacosConfig.Chat.ChatRedisStreamConfig;
import com.seek.food.config.NacosConfig.Rider.RiderRedisStreamConfig;
import com.seek.food.util.FileUtil.FileRemove;
import com.seek.food.util.Redis.RedisUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

//重新对无法删除的文件进行删除
@Component
@Slf4j
public class OldFileClearScheduled {
    private StringRedisTemplate stringRedisTemplate;
    private RedisStreamData oldFileStream;
    @Autowired
    public OldFileClearScheduled(StringRedisTemplate stringRedisTemplate, ChatRedisStreamConfig chatRedisStreamConfig) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.oldFileStream = chatRedisStreamConfig.getOldFileStream();
    }
    @Scheduled(fixedDelay = 5000)
    public void clearOldFiles() {
        //快速进行读取与处理
        RedisUtil.readStreamAndHandle(stringRedisTemplate,oldFileStream.getName(),
                oldFileStream.getConsumer().getGroupName(),1,3,1,oldFileStream.getKeyName()
                ,data-> {
                    String path = (String) data;
                    try {
                        //获取数据并进行处理
                        FileRemove.removeFileByPath(path);
                    } catch (Exception e) {
                        log.error("有旧文件:{}在定时重新删除时发生错误:", path, e);
                    }
                });
    }
    @PostConstruct
    public void init(){
        RedisUtil.createStreamConsumerGroup(stringRedisTemplate,oldFileStream.getName(),oldFileStream.getConsumer().getGroupName());
    }










}
