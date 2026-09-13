package com.seek.food.config.NacosConfig.Admin;

import com.seek.food.config.Data.RedisKeyData;
import com.seek.food.config.Enum.ConfigKeyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Data
@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Admin_Redis_Key_Config)
public class AdminRedisKeyConfig {
    private RedisKeyData adminLoginCooldown;
    private RedisKeyData adminInsertSuggestionCooldown;
    private RedisKeyData adminGetSuggestionListCooldown;
    private RedisKeyData adminAckSuggestionCooldown;
    private RedisKeyData adminSuggestionIdCount;
    private RedisKeyData adminIdCount;
    private RedisKeyData suggestionCaffeine;
}
