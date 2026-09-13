package com.seek.food.config.AutoConfig;

import com.seek.food.config.NacosConfig.Interaction.InteractionCaffeineConfig;
import com.seek.food.config.NacosConfig.Interaction.InteractionParamsRulesConfig;
import com.seek.food.config.NacosConfig.Interaction.InteractionRedisKeyConfig;
import com.seek.food.config.NacosConfig.Interaction.InteractionRedisStreamConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
// 绑定当前组件对应的属性类
@EnableConfigurationProperties({InteractionCaffeineConfig.class, InteractionRedisKeyConfig.class, InteractionRedisStreamConfig.class
        , InteractionParamsRulesConfig.class})
public class InteractionSubConfig {
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public InteractionCaffeineConfig interactionCaffeineConfig(InteractionCaffeineConfig interactionCaffeineConfig) {
        return interactionCaffeineConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public InteractionRedisKeyConfig interactionRedisKeyConfig(InteractionRedisKeyConfig interactionRedisKeyConfig) {
        return interactionRedisKeyConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public InteractionRedisStreamConfig interactionRedisStreamConfig(InteractionRedisStreamConfig interactionRedisStreamConfig) {
        return interactionRedisStreamConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public InteractionParamsRulesConfig interactionParamsRulesConfig(InteractionParamsRulesConfig interactionParamsRulesConfig) {
        return interactionParamsRulesConfig;
    }
}
