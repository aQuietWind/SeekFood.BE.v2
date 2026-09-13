package com.seek.food.employee.Caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.seek.food.config.NacosConfig.Employee.EmployeeCaffeineConfig;
import com.seek.food.config.NacosConfig.Merchant.MerchantCaffeineConfig;
import com.seek.food.dto.Employee.EmployeeDTO;
import com.seek.food.dto.Merchant.MerchantDTO;
import com.seek.food.util.Caffeine.JvmCaffeineParent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class EmployeeCaffeine extends JvmCaffeineParent<Long, EmployeeDTO> {
    // 构造注入配置
    private final EmployeeCaffeineConfig employeeCaffeineConfig;
    @Autowired
    public EmployeeCaffeine(EmployeeCaffeineConfig employeeCaffeineConfig) {
        this.employeeCaffeineConfig = employeeCaffeineConfig;
    }

    // 容器启动构建缓存
    @PostConstruct
    public void init() {
        super.CACHE = Caffeine.newBuilder()
                .maximumSize(employeeCaffeineConfig.getEmployee().getMaxSize())
                .expireAfterWrite(employeeCaffeineConfig.getEmployee().getExpireTime(), TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    // 容器销毁清理缓存
    @PreDestroy
    public void destroy() {
        super.CACHE.cleanUp();
        super.CACHE.invalidateAll();
    }
}
