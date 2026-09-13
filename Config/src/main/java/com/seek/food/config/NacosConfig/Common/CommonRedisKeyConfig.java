package com.seek.food.config.NacosConfig.Common;

import com.seek.food.config.Data.RedisKeyData;
import com.seek.food.config.Enum.ConfigKeyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Common_Redis_Key)
@Data
public class CommonRedisKeyConfig {
    private RedisKeyData loginToken;
}
