package com.seek.food.config.NacosConfig.Meal;

import com.seek.food.config.Data.RedisStreamData;
import com.seek.food.config.Enum.ConfigKeyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Data
@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Meal_Redis_Stream_Config)
public class MealRedisStreamConfig {
    private RedisStreamData oldFileStream;
}
