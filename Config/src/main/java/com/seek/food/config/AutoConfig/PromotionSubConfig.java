package com.seek.food.config.AutoConfig;

import com.seek.food.config.NacosConfig.Promotion.PromotionCaffeineConfig;
import com.seek.food.config.NacosConfig.Promotion.PromotionParamsRulesConfig;
import com.seek.food.config.NacosConfig.Promotion.PromotionRedisKeyConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
// 绑定当前组件对应的属性类
@EnableConfigurationProperties({PromotionCaffeineConfig.class, PromotionRedisKeyConfig.class, PromotionParamsRulesConfig.class})
public class PromotionSubConfig {
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public PromotionCaffeineConfig promotionCaffeineConfig(PromotionCaffeineConfig promotionCaffeineConfig) {
        return promotionCaffeineConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public PromotionRedisKeyConfig promotionRedisKeyConfig(PromotionRedisKeyConfig promotionRedisKeyConfig) {
        return promotionRedisKeyConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public PromotionParamsRulesConfig promotionParamsRulesConfig(PromotionParamsRulesConfig promotionParamsRulesConfig) {
        return promotionParamsRulesConfig;
    }
}
