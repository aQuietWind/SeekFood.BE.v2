package com.seek.food.config.NacosConfig.Rider;

import com.seek.food.config.Data.RedisKeyData;
import com.seek.food.config.Enum.ConfigKeyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Data
@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Rider_Redis_Key_Config)
public class RiderRedisKeyConfig {
    private RedisKeyData registerOpt;
    private RedisKeyData loginOpt;
    private RedisKeyData deleteRiderOpt;
    private RedisKeyData updatePasswordOpt;
    private RedisKeyData registerCooldown;
    private RedisKeyData loginPasswordCooldown;
    private RedisKeyData loginRefreshCooldown;
    private RedisKeyData updatePersonImageCooldown;
    private RedisKeyData updatePasswordCooldown;
    private RedisKeyData riderIdCount;
    private RedisKeyData riderMessageCaffeine;
    private RedisKeyData riderPhoneCaffeine;
}
