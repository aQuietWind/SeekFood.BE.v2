package com.seek.food.config.AutoConfig;

import com.seek.food.config.NacosConfig.Employee.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
// 绑定当前组件对应的属性类
@EnableConfigurationProperties({EmployeeCaffeineConfig.class, EmployeeRedisKeyConfig.class, EmployeeRedisStreamConfig.class
        , EmployeeParamsRulesConfig.class})
public class EmployeeSubConfig {
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public EmployeeCaffeineConfig employeeCaffeineConfig(EmployeeCaffeineConfig employeeCaffeineConfig) {
        return employeeCaffeineConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public EmployeeRedisKeyConfig employeeRedisKeyConfig(EmployeeRedisKeyConfig employeeRedisKeyConfig) {
        return employeeRedisKeyConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public EmployeeRedisStreamConfig employeeRedisStreamConfig(EmployeeRedisStreamConfig employeeRedisStreamConfig) {
        return employeeRedisStreamConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public EmployeeParamsRulesConfig employeeParamsRulesConfig(EmployeeParamsRulesConfig employeeParamsRulesConfig) {
        return employeeParamsRulesConfig;
    }
}
