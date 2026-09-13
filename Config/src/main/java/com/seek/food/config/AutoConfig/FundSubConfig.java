package com.seek.food.config.AutoConfig;

import com.seek.food.config.NacosConfig.Fund.FundCaffeineConfig;
import com.seek.food.config.NacosConfig.Fund.FundParamsRulesConfig;
import com.seek.food.config.NacosConfig.Fund.FundRedisKeyConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
// 绑定当前组件对应的属性类
@EnableConfigurationProperties({FundParamsRulesConfig.class,FundRedisKeyConfig.class, FundCaffeineConfig.class})
public class FundSubConfig {
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public FundParamsRulesConfig fundParamsRulesConfig(FundParamsRulesConfig fundParamsRulesConfig) {
        return fundParamsRulesConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public FundRedisKeyConfig fundRedisKeyConfig(FundRedisKeyConfig fundRedisKeyConfig) {
        return fundRedisKeyConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public FundCaffeineConfig fundCaffeineConfig(FundCaffeineConfig fundCaffeineConfig) {
        return fundCaffeineConfig;
    }











}
