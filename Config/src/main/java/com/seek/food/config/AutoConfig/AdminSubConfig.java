package com.seek.food.config.AutoConfig;

import com.seek.food.config.NacosConfig.Admin.AdminCaffeineConfig;
import com.seek.food.config.NacosConfig.Admin.AdminParamsRulesConfig;
import com.seek.food.config.NacosConfig.Admin.AdminRedisKeyConfig;
import com.seek.food.config.NacosConfig.Admin.AdminRedisStreamConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
// 绑定当前组件对应的属性类
@EnableConfigurationProperties({AdminCaffeineConfig.class, AdminRedisKeyConfig.class, AdminRedisStreamConfig.class
        , AdminParamsRulesConfig.class})
public class AdminSubConfig {
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public AdminCaffeineConfig adminCaffeineConfig(AdminCaffeineConfig adminCaffeineConfig) {
        return adminCaffeineConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public AdminRedisKeyConfig adminRedisKeyConfig(AdminRedisKeyConfig adminRedisKeyConfig) {
        return adminRedisKeyConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public AdminRedisStreamConfig adminRedisStreamConfig(AdminRedisStreamConfig adminRedisStreamConfig) {
        return adminRedisStreamConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public AdminParamsRulesConfig adminParamsRulesConfig(AdminParamsRulesConfig adminParamsRulesConfig) {
        return adminParamsRulesConfig;
    }















}

