package com.seek.food.config.AutoConfig;

import com.seek.food.config.NacosConfig.User.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
// 绑定当前组件对应的属性类
@EnableConfigurationProperties({UserParamsRulesConfig.class,UserCaffeineConfig.class, UserRedisStreamConfig.class,UserRedisKeyConfig.class})
public class UserSubConfig {
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public UserParamsRulesConfig userParamsRulesConfig(UserParamsRulesConfig userParamsRulesConfig) {
        return userParamsRulesConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public UserCaffeineConfig userCaffeineConfig(UserCaffeineConfig userCaffeineConfig) {
        return userCaffeineConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public UserRedisStreamConfig userRedisStreamConfig(UserRedisStreamConfig userRedisStreamConfig) {
        return userRedisStreamConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public UserRedisKeyConfig userRedisKeyConfig(UserRedisKeyConfig userRedisKeyConfig) {
        return userRedisKeyConfig;
    }












}
