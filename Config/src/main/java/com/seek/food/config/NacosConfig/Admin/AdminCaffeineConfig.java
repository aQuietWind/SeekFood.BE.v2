package com.seek.food.config.NacosConfig.Admin;

import com.seek.food.config.Data.CaffeineData;
import com.seek.food.config.Enum.ConfigKeyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Data
@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Admin_Caffeine_Config)
public class AdminCaffeineConfig {
    private CaffeineData suggestion;
}
