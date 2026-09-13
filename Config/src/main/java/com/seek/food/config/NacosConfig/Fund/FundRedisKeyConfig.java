package com.seek.food.config.NacosConfig.Fund;

import com.seek.food.config.Data.RedisKeyData;
import com.seek.food.config.Enum.ConfigKeyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Fund_Redis_Key_Config)
@Data
public class FundRedisKeyConfig {
    private RedisKeyData fundInsertCooldown;
    private RedisKeyData fundRechargeCooldown;
    private RedisKeyData fundWithdrawCooldown;
    private RedisKeyData fundGetSimpleRechargeCooldown;
    private RedisKeyData fundGetSimpleWithdrawCooldown;
    private RedisKeyData fundGetSimpleOrderCooldown;
    private RedisKeyData fundGetSimpleOrderRefundCooldown;
    private RedisKeyData fundPayOrderRecordCooldown;
    private RedisKeyData fundRollbackOrderRecordCooldown;
    private RedisKeyData fundDecreaseLock;
    private RedisKeyData fundCaffeineMessage;
    private RedisKeyData fundRechargeRecordCaffeineMessage;
    private RedisKeyData fundWithdrawRecordCaffeineMessage;
    private RedisKeyData fundOrderRecordCaffeineMessage;
    private RedisKeyData fundOrderRefundRecordCaffeineMessage;
    private RedisKeyData fundRechargeRecordIdCount;
    private RedisKeyData fundWithdrawRecordIdCount;
    private RedisKeyData fundOrderRecordIdCount;
    private RedisKeyData fundOrderRefundRecordIdCount;
}
