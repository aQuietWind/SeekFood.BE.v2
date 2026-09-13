package com.seek.food.config.NacosConfig.Comment;

import com.seek.food.config.Data.RedisKeyData;
import com.seek.food.config.Enum.ConfigKeyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Data
@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Comment_Redis_Key_Config)
public class CommentRedisKeyConfig {
    private RedisKeyData commentInsertCooldown;
    private RedisKeyData firstGetSimpleCooldown;
    private RedisKeyData secondGetListCooldown;
    private RedisKeyData commentDeleteCooldown;
    private RedisKeyData firstCommentIdCount;
    private RedisKeyData secondCommentIdCount;
    private RedisKeyData firstCommentCaffeine;
}
