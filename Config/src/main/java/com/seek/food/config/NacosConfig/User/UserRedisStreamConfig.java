package com.seek.food.config.NacosConfig.User;

import com.seek.food.config.Data.RedisStreamData;
import com.seek.food.config.Enum.ConfigKeyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.User_Redis_Stream_Config)
@Data
public class UserRedisStreamConfig {
    private RedisStreamData oldFileStream;
}
