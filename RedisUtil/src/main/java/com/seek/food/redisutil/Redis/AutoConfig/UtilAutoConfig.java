package com.seek.food.redisutil.Redis.AutoConfig;

import com.seek.food.redisutil.Redis.RedisBitMapUtil;
import com.seek.food.redisutil.Redis.RedisStreamUtil;
import com.seek.food.redisutil.Redis.RedisUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class UtilAutoConfig {
    @Bean
    @Lazy
    public RedisBitMapUtil redisBitMapUtil(StringRedisTemplate stringRedisTemplate) {
        return new RedisBitMapUtil(stringRedisTemplate);
    }
    @Bean
    @Lazy
    public RedisUtil redisUtil(StringRedisTemplate stringRedisTemplate) {
        return new RedisUtil(stringRedisTemplate);
    }
    @Bean
    @Lazy
    public RedisStreamUtil redisStreamUtil(StringRedisTemplate stringRedisTemplate) {
        return new RedisStreamUtil(stringRedisTemplate);
    }
}
