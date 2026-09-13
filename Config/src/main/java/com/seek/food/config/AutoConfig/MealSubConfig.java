package com.seek.food.config.AutoConfig;

import com.seek.food.config.NacosConfig.Meal.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
// 绑定当前组件对应的属性类
@EnableConfigurationProperties({MealCaffeineConfig.class, MealRedisKeyConfig.class, MealRedisStreamConfig.class
        , MealParamsRulesConfig.class})
public class MealSubConfig {
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public MealCaffeineConfig mealCaffeineConfig(MealCaffeineConfig mealCaffeineConfig) {
        return mealCaffeineConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public MealRedisKeyConfig mealRedisKeyConfig(MealRedisKeyConfig mealRedisKeyConfig) {
        return mealRedisKeyConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public MealRedisStreamConfig mealRedisStreamConfig(MealRedisStreamConfig mealRedisStreamConfig) {
        return mealRedisStreamConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public MealParamsRulesConfig mealParamsRulesConfig(MealParamsRulesConfig mealParamsRulesConfig) {
        return mealParamsRulesConfig;
    }
}
