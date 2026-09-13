package com.seek.food.config.AutoConfig;

import com.seek.food.config.NacosConfig.Voucher.VoucherCaffeineConfig;
import com.seek.food.config.NacosConfig.Voucher.VoucherParamsRulesConfig;
import com.seek.food.config.NacosConfig.Voucher.VoucherRedisKeyConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
// 绑定当前组件对应的属性类
@EnableConfigurationProperties({VoucherCaffeineConfig.class, VoucherParamsRulesConfig.class, VoucherRedisKeyConfig.class})
public class VoucherSubConfig {
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public VoucherCaffeineConfig voucherCaffeineConfig(VoucherCaffeineConfig voucherCaffeineConfig) {
        return voucherCaffeineConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public VoucherRedisKeyConfig voucherRedisKeyConfig(VoucherRedisKeyConfig voucherRedisKeyConfig) {
        return voucherRedisKeyConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public VoucherParamsRulesConfig voucherParamsRulesConfig(VoucherParamsRulesConfig voucherParamsRulesConfig) {
        return voucherParamsRulesConfig;
    }
}
