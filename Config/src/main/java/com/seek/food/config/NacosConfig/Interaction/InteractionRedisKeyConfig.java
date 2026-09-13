package com.seek.food.config.NacosConfig.Interaction;

import com.seek.food.config.Data.RedisKeyData;
import com.seek.food.config.Enum.ConfigKeyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Data
@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Interaction_Redis_Key_Config)
public class InteractionRedisKeyConfig {
    private RedisKeyData interactionLikeMerchant;
    private RedisKeyData interactionLikeFirstComment;
    private RedisKeyData interactionLikeSecondComment;
    private RedisKeyData interactionCollectMerchant;
    private RedisKeyData mealInsertCooldown;
    private RedisKeyData mealIdCount;
    private RedisKeyData mealMessageCaffeine;
}
