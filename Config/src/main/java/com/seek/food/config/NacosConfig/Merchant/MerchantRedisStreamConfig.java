package com.seek.food.config.NacosConfig.Merchant;

import com.seek.food.config.Data.RedisStreamData;
import com.seek.food.config.Enum.ConfigKeyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Data
@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Merchant_Redis_Stream_Config)
public class MerchantRedisStreamConfig {
    private RedisStreamData oldFileStream;
    private RedisStreamData esSyncStream;
}
