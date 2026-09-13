package com.seek.food.config.AutoConfig;

import com.seek.food.config.NacosConfig.Rider.RiderCaffeineConfig;
import com.seek.food.config.NacosConfig.Rider.RiderParamsRulesConfig;
import com.seek.food.config.NacosConfig.Rider.RiderRedisKeyConfig;
import com.seek.food.config.NacosConfig.Rider.RiderRedisStreamConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
// 绑定当前组件对应的属性类
@EnableConfigurationProperties({RiderCaffeineConfig.class, RiderRedisKeyConfig.class, RiderRedisStreamConfig.class
        , RiderParamsRulesConfig.class})
public class RiderSubConfig {
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public RiderCaffeineConfig riderCaffeineConfig(RiderCaffeineConfig riderCaffeineConfig) {
        return riderCaffeineConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public RiderRedisKeyConfig riderRedisKeyConfig(RiderRedisKeyConfig riderRedisKeyConfig) {
        return riderRedisKeyConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public RiderRedisStreamConfig riderRedisStreamConfig(RiderRedisStreamConfig riderRedisStreamConfig) {
        return riderRedisStreamConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public RiderParamsRulesConfig riderParamsRulesConfig(RiderParamsRulesConfig riderParamsRulesConfig) {
        return riderParamsRulesConfig;
    }
}
