package com.seek.food.config.NacosConfig.MQ;

import com.seek.food.config.Data.QueueData;
import com.seek.food.config.Enum.ConfigKeyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Employee_Exchange_Config)
@Data
public class EmployeeExchangeConfig {
    private String type;
    private String exchangeName;
    private QueueData changeEmployeeAmountQueue;
    private QueueData deleteFileEmployeeQueue;
}
