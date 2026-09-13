package com.seek.food.config.AutoConfig;

import com.seek.food.config.NacosConfig.Order.OrderCaffeineConfig;
import com.seek.food.config.NacosConfig.Order.OrderParamsRulesConfig;
import com.seek.food.config.NacosConfig.Order.OrderRedisKeyConfig;
import com.seek.food.config.NacosConfig.Order.OrderRiderOrderEsTableConfig;
import com.seek.food.config.NacosConfig.Voucher.VoucherCaffeineConfig;
import com.seek.food.config.NacosConfig.Voucher.VoucherParamsRulesConfig;
import com.seek.food.config.NacosConfig.Voucher.VoucherRedisKeyConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
// 绑定当前组件对应的属性类
@EnableConfigurationProperties({OrderCaffeineConfig.class, OrderParamsRulesConfig.class, OrderRedisKeyConfig.class, OrderRiderOrderEsTableConfig.class})
public class OrderSubConfig {
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public OrderCaffeineConfig orderCaffeineConfig(OrderCaffeineConfig orderCaffeineConfig) {
        return orderCaffeineConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public OrderRedisKeyConfig orderRedisKeyConfig(OrderRedisKeyConfig orderRedisKeyConfig) {
        return orderRedisKeyConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public OrderParamsRulesConfig orderParamsRulesConfig(OrderParamsRulesConfig orderParamsRulesConfig) {
        return orderParamsRulesConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public OrderRiderOrderEsTableConfig orderRiderOrderEsTableConfig(OrderRiderOrderEsTableConfig orderRiderOrderEsTableConfig) {
        return orderRiderOrderEsTableConfig;
    }







}
