package com.seek.food.util.CommonUtil;


import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class IdUtil {
    private static final Random random=new Random();
    public static long IdGenerateByIncreaseRandom(String redisKey,StringRedisTemplate stringRedisTemplate,int maxRandomNumber) {
        return stringRedisTemplate.opsForValue().increment(redisKey,random.nextInt(maxRandomNumber));
    }
    public static long IdGenerateByIncrease(String redisKey,StringRedisTemplate stringRedisTemplate) {
        return stringRedisTemplate.opsForValue().increment(redisKey);
    }
}














