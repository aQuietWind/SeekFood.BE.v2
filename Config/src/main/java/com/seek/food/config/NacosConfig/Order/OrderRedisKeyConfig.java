package com.seek.food.config.NacosConfig.Order;

import com.seek.food.config.Data.RedisKeyData;
import com.seek.food.config.Enum.ConfigKeyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Order_Redis_Key_Config)
@Data
public class OrderRedisKeyConfig {
    private RedisKeyData orderInsertLock;
    private RedisKeyData orderGetSimpleCooldown;
    private RedisKeyData orderGetSimpleByStateCooldown;
    private RedisKeyData orderRefundCooldown;
    private RedisKeyData orderMerchantRejectCooldown;
    private RedisKeyData orderMerchantAckCooldown;
    private RedisKeyData orderMerchantMakeCooldown;
    private RedisKeyData orderRiderAcceptCooldown;
    private RedisKeyData orderRiderAckCooldown;
    private RedisKeyData orderRiderDeliveryCooldown;
    private RedisKeyData orderUserReceiveCooldown;
    private RedisKeyData orderRiderGetEsOrderCooldown;
    private RedisKeyData orderCommentSelectCooldown;
    private RedisKeyData orderIdCount;
    private RedisKeyData orderMessageCaffeine;
}