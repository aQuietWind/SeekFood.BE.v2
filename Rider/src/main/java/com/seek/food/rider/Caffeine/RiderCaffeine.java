package com.seek.food.rider.Caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.seek.food.config.NacosConfig.Rider.RiderCaffeineConfig;
import com.seek.food.dto.Rider.RiderDTO;
import com.seek.food.util.Caffeine.JvmCaffeineParent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RiderCaffeine extends JvmCaffeineParent<Long, RiderDTO> {
    // 构造注入配置
    private final RiderCaffeineConfig riderCaffeineConfig;
    @Autowired
    public RiderCaffeine(RiderCaffeineConfig riderCaffeineConfig) {
        this.riderCaffeineConfig = riderCaffeineConfig;
    }

    // 容器启动构建缓存
    @PostConstruct
    public void init() {
        super.CACHE = Caffeine.newBuilder()
                .maximumSize(riderCaffeineConfig.getRider().getMaxSize())
                .expireAfterWrite(riderCaffeineConfig.getRider().getExpireTime(), TimeUnit.MINUTES)
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
