package com.seek.food.config.NacosConfig.Merchant;

import com.seek.food.config.Data.RedisKeyData;
import com.seek.food.config.Enum.ConfigKeyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Data
@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Merchant_Redis_Key_Config)
public class MerchantRedisKeyConfig {
    private RedisKeyData merchantRegisterOpt;
    private RedisKeyData merchantLoginOpt;
    private RedisKeyData merchantUpdatePasswordOpt;
    private RedisKeyData merchantDeleteOpt;
    private RedisKeyData merchantLoginPasswordCooldown;
    private RedisKeyData merchantLoginRefreshCooldown;
    private RedisKeyData merchantUpdatePasswordCooldown;
    private RedisKeyData merchantUpdateMessageCooldown;
    private RedisKeyData merchantUpdateMasterCooldown;
    private RedisKeyData merchantAddProofCooldown;
    private RedisKeyData merchantAddShowCooldown;
    private RedisKeyData merchantRemoveProofCooldown;
    private RedisKeyData merchantRemoveShowCooldown;
    private RedisKeyData merchantReplaceProofCooldown;
    private RedisKeyData merchantReplaceShowCooldown;
    private RedisKeyData merchantUpdateHomeCooldown;
    private RedisKeyData merchantDeleteCooldown;
    private RedisKeyData merchantUpdateOpenCooldown;
    private RedisKeyData merchantGetDistanceCooldown;
    private RedisKeyData merchantMasterIsSet;
    private RedisKeyData merchantIdCount;
    private RedisKeyData merchantMessageCaffeine;
    private RedisKeyData merchantPhoneCaffeine;
    private RedisKeyData merchantEsSyncRecord;
}
