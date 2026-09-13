package com.seek.food.config.NacosConfig.MQ;

import com.seek.food.config.Data.QueueData;
import com.seek.food.config.Enum.ConfigKeyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Meal_Exchange_Config)
@Data
public class MealExchangeConfig {
    private String exchangeName;
    private String type;
    private QueueData deleteFileMealQueue;
    private QueueData deleteFileMealDeadLetterQueue;
    private QueueData deleteAllFileMealDeadLetterQueue;
}
