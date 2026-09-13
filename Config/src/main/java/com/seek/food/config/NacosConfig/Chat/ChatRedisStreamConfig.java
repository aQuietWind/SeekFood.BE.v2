package com.seek.food.config.NacosConfig.Chat;

import com.seek.food.config.Data.RedisStreamData;
import com.seek.food.config.Enum.ConfigKeyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Data
@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Chat_Redis_Stream_Config)
public class ChatRedisStreamConfig {
    private RedisStreamData oldFileStream;
}
