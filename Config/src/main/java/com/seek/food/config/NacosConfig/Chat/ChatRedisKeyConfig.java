package com.seek.food.config.NacosConfig.Chat;

import com.seek.food.config.Data.RedisKeyData;
import com.seek.food.config.Enum.ConfigKeyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Data
@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Chat_Redis_Key_Config)
public class ChatRedisKeyConfig {
    private RedisKeyData chatRoomGetListCooldown;
    private RedisKeyData chatRecordInsertCooldown;
    private RedisKeyData chatRecordGetListCooldown;
    private RedisKeyData chatRecordWithdrawCooldown;
    private RedisKeyData chatRoomWebsocketCooldown;
    private RedisKeyData chatRoomIdCount;
    private RedisKeyData chatRecordIdCount;
}
