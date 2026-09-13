package com.seek.food.config.NacosConfig.Promotion;

import com.seek.food.config.Data.RedisKeyData;
import com.seek.food.config.Enum.ConfigKeyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Data
@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Promotion_Redis_Key_Config)
public class PromotionRedisKeyConfig {
    private RedisKeyData merchantLoginPromotionInsertCooldown;
    private RedisKeyData merchantGrabPromotionInsertCooldown;
    private RedisKeyData merchantLoginPromotionGetSimpleCooldown;
    private RedisKeyData merchantLoginPromotionGetSimpleEffectiveCooldown;
    private RedisKeyData merchantLoginPromotionIdCount;
    private RedisKeyData merchantLoginPromotionMessageCaffeine;
    private RedisKeyData merchantGrabPromotionGetSimpleCooldown;
    private RedisKeyData merchantGrabPromotionGetSimpleEffectiveCooldown;
    private RedisKeyData merchantLoginPromotionGetVoucherLock;
    private RedisKeyData merchantGrabPromotionGetVoucherLock;
    private RedisKeyData merchantGrabPromotionIdCount;
    private RedisKeyData merchantGrabPromotionMessageCaffeine;
}
