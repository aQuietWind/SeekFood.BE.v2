package com.seek.food.config.AutoConfig;

import com.seek.food.config.NacosConfig.Comment.CommentCaffeineConfig;
import com.seek.food.config.NacosConfig.Comment.CommentParamsRulesConfig;
import com.seek.food.config.NacosConfig.Comment.CommentRedisKeyConfig;
import com.seek.food.config.NacosConfig.Comment.CommentRedisStreamConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
// 绑定当前组件对应的属性类
@EnableConfigurationProperties({CommentCaffeineConfig.class, CommentRedisKeyConfig.class, CommentRedisStreamConfig.class
        , CommentParamsRulesConfig.class})
public class CommentSubConfig {
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public CommentCaffeineConfig commentCaffeineConfig(CommentCaffeineConfig commentCaffeineConfig) {
        return commentCaffeineConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public CommentRedisStreamConfig commentRedisStreamConfig(CommentRedisStreamConfig commentRedisStreamConfig) {
        return commentRedisStreamConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public CommentRedisKeyConfig commentRedisKeyConfig(CommentRedisKeyConfig commentRedisKeyConfig) {
        return commentRedisKeyConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public CommentParamsRulesConfig commentParamsRulesConfig(CommentParamsRulesConfig commentParamsRulesConfig) {
        return commentParamsRulesConfig;
    }
}
