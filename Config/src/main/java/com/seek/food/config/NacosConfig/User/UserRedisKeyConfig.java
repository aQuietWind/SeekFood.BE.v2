package com.seek.food.config.NacosConfig.User;

import com.seek.food.config.Data.RedisKeyData;
import com.seek.food.config.Enum.ConfigKeyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.User_Redis_Key_Config)
@Data
public class UserRedisKeyConfig {
    private RedisKeyData registerOpt;
    private RedisKeyData loginOpt;
    private RedisKeyData deleteUserOpt;
    private RedisKeyData updatePasswordOpt;
    private RedisKeyData registerCooldown;
    private RedisKeyData loginPasswordCooldown;
    private RedisKeyData loginRefreshCooldown;
    private RedisKeyData updateHeaderImageCooldown;
    private RedisKeyData updateMessageCooldown;
    private RedisKeyData updatePasswordCooldown;
    private RedisKeyData caffeineMessage;
    private RedisKeyData caffeinePhone;
    private RedisKeyData userIdCount;
}
