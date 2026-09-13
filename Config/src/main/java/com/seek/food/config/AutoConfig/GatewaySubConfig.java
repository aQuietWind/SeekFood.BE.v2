package com.seek.food.config.AutoConfig;

import com.seek.food.config.NacosConfig.Gateway.GatewayBlackConfig;
import com.seek.food.config.NacosConfig.Gateway.GatewayRedisKeyConfig;
import com.seek.food.config.NacosConfig.Gateway.GatewayRequestPathConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
// 绑定当前组件对应的属性类
@EnableConfigurationProperties({GatewayRedisKeyConfig.class, GatewayBlackConfig.class, GatewayRequestPathConfig.class})
public class GatewaySubConfig {
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public GatewayBlackConfig gatewayBlackConfig(GatewayBlackConfig gatewayBlackConfig) {
        return gatewayBlackConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public GatewayRedisKeyConfig gatewayRedisKeyConfig(GatewayRedisKeyConfig gatewayRedisKeyConfig) {
        return gatewayRedisKeyConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public GatewayRequestPathConfig gatewayRequestPathConfig(GatewayRequestPathConfig gatewayRequestPathConfig) {
        return gatewayRequestPathConfig;
    }
}
