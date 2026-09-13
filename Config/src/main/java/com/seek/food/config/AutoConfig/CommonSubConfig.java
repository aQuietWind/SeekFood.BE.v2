package com.seek.food.config.AutoConfig;

import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.Common.CommonRedisKeyConfig;
import com.seek.food.config.NacosConfig.Common.JWTConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
// 绑定当前组件对应的属性类
@EnableConfigurationProperties({JWTConfig.class, CommonRedisKeyConfig.class, CommonParamRulesConfig.class})
public class CommonSubConfig {
    @Bean
    @ConditionalOnMissingBean // 业务可自定义覆盖
    @Lazy // 用到才实例化，启动不创建对象
    public JWTConfig jwtConfig(JWTConfig jwtConfig) {
        return jwtConfig;
    }

    @Bean
    @ConditionalOnMissingBean // 业务可自定义覆盖
    @Lazy // 用到才实例化，启动不创建对象
    public CommonRedisKeyConfig commonRedisKeyConfig(CommonRedisKeyConfig commonRedisKeyConfig) {
        return commonRedisKeyConfig;
    }
    @Bean
    @ConditionalOnMissingBean // 业务可自定义覆盖
    @Lazy // 用到才实例化，启动不创建对象
    public CommonParamRulesConfig commonParamRulesConfig(CommonParamRulesConfig commonParamRulesConfig) {
        return commonParamRulesConfig;
    }
}
