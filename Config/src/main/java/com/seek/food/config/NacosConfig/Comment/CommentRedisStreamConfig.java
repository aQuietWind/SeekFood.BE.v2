package com.seek.food.config.NacosConfig.Comment;

import com.seek.food.config.Data.RedisStreamData;
import com.seek.food.config.Enum.ConfigKeyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Data
@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Comment_Redis_Stream_Config)
public class CommentRedisStreamConfig {
    private RedisStreamData oldFileStream;
}
