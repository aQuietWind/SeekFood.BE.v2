package com.seek.food.config.NacosConfig.User;

import com.seek.food.config.Data.CaffeineData;
import com.seek.food.config.Enum.ConfigKeyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.User_Caffeine_Config)
@Data
public class UserCaffeineConfig {
    private CaffeineData user;
    private CaffeineData phone;
}
