package com.seek.food.redisutil.Redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

//此处可能属于过度引入redisson，可以将redisson的工具封装拆出来，作为另外一个模块使用
@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host}")
    private String host;
    @Value("${spring.data.redis.port}")
    private String port;
    @Value("${spring.data.redis.password}")
    private String password;

    @Bean
    @Lazy
    public RedissonClient redissonClient() {
        //获取配置对象
        Config config = new Config();
        //设置地址和密码,常规的读取配置会读取空字符串，所以需要进行判断变为null值
        config.useSingleServer().setAddress("redis://"+host+":"+port).setPassword((password.isEmpty()||password.isBlank())?null:password);
        //导入配置，并且返回工具对象给SpringBoot
        return Redisson.create(config);
    }

    @Bean
    @Lazy
    public RedissonUtil redissonUtil(RedissonClient redissonClient) {
        return new RedissonUtil(redissonClient);
    }
}
