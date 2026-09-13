package com.seek.food.meal.Consumer;

import com.seek.food.config.Data.RedisStreamData;
import com.seek.food.config.Enum.MQNameKeyEnum;
import com.seek.food.config.NacosConfig.Meal.MealParamsRulesConfig;
import com.seek.food.config.NacosConfig.Meal.MealRedisStreamConfig;
import com.seek.food.meal.Mapper.MealMapper;
import com.seek.food.util.FileUtil.FileRemove;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Component
@Slf4j
public class DeleteAllFileMealImplConsumer {
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisStreamData oldFileStream;
    private final MealParamsRulesConfig mealParamsRulesConfig;
    private final MealMapper mealMapper;
    @Autowired
    public DeleteAllFileMealImplConsumer(StringRedisTemplate stringRedisTemplate, MealRedisStreamConfig mealRedisStreamConfig
    , MealParamsRulesConfig mealParamsRulesConfig, MealMapper mealMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.oldFileStream = mealRedisStreamConfig.getOldFileStream();
        this.mealParamsRulesConfig = mealParamsRulesConfig;
        this.mealMapper = mealMapper;
    }


    @RabbitListener(queues = MQNameKeyEnum.Dead_Letter_Exchange_Delete_All_File_Meal_Impl_Queue)
    public void deleteAllFileMealImplQueue(long merchantId){
        List<String> addrs=mealMapper.getAllShowImageAddrAfterDelete(merchantId);
        FileRemove.removeFileListOutError(mealParamsRulesConfig.getMealShowImageDest(),addrs,(path,e)->{
            //不再进行重试，而是留给spring后台线程进行定时处理
            log.error("path:{},在删除时发生错误",path,e);
            stringRedisTemplate.opsForStream().add(oldFileStream.getName(), Map.of(oldFileStream.getKeyName(),path));
        });
    }















}
