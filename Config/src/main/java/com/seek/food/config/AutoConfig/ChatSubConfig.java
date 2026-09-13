package com.seek.food.config.AutoConfig;

import com.seek.food.config.NacosConfig.Chat.ChatParamsRulesConfig;
import com.seek.food.config.NacosConfig.Chat.ChatRedisKeyConfig;
import com.seek.food.config.NacosConfig.Chat.ChatRedisStreamConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
// 绑定当前组件对应的属性类
@EnableConfigurationProperties({ChatRedisKeyConfig.class, ChatRedisStreamConfig.class
        , ChatParamsRulesConfig.class})
public class ChatSubConfig {
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public ChatRedisKeyConfig chatRedisKeyConfig(ChatRedisKeyConfig chatRedisKeyConfig) {
        return chatRedisKeyConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public ChatRedisStreamConfig chatRedisStreamConfig(ChatRedisStreamConfig chatRedisStreamConfig) {
        return chatRedisStreamConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public ChatParamsRulesConfig chatParamsRulesConfig(ChatParamsRulesConfig chatParamsRulesConfig) {
        return chatParamsRulesConfig;
    }
}
