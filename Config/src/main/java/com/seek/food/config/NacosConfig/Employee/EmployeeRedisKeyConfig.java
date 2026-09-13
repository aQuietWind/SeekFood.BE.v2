package com.seek.food.config.NacosConfig.Employee;

import com.seek.food.config.Data.RedisKeyData;
import com.seek.food.config.Enum.ConfigKeyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Employee_Redis_Key_Config)
@Data
public class EmployeeRedisKeyConfig {
    private RedisKeyData employeeInsertCooldown;
    private RedisKeyData employeeGetSimpleCooldown;
    private RedisKeyData employeeUpdateMessageCooldown;
    private RedisKeyData employeeUpdatePersonImageCooldown;
    private RedisKeyData employeeDeleteCooldown;
    private RedisKeyData employeeUpdateResignCooldown;
    private RedisKeyData employeeIdCount;
    private RedisKeyData employeeMessageCaffeine;
}