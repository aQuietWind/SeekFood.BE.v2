package com.seek.food.config.NacosConfig.Voucher;

import com.seek.food.config.Data.RedisKeyData;
import com.seek.food.config.Enum.ConfigKeyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Data
@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Voucher_Redis_Key_Config)
public class VoucherRedisKeyConfig {
    private RedisKeyData merchantVoucherInsertCooldown;
    private RedisKeyData merchantVoucherGetSimpleCooldown;
    private RedisKeyData merchantVoucherGetSimpleEffectiveCooldown;
    private RedisKeyData voucherConnectionGetSimpleCooldown;
    private RedisKeyData voucherConnectionGetSimpleEffectiveCooldown;
    private RedisKeyData merchantVoucherIdCount;
    private RedisKeyData voucherConnectionIdCount;
    private RedisKeyData merchantVoucherMessageCaffeine;
    private RedisKeyData voucherConnectionMessageCaffeine;
}
