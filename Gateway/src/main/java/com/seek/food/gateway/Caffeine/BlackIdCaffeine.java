package com.seek.food.gateway.Caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.seek.food.config.NacosConfig.Gateway.GatewayBlackConfig;
import com.seek.food.util.Caffeine.JvmCaffeineParent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
@Component
public class BlackIdCaffeine extends JvmCaffeineParent<String,Long> {

    // 构造注入配置
    private final GatewayBlackConfig gatewayBlackConfig;
    @Autowired
    public BlackIdCaffeine(GatewayBlackConfig gatewayBlackConfig) {
        this.gatewayBlackConfig = gatewayBlackConfig;
    }

    // 容器启动构建缓存
    @PostConstruct
    public void init() {
        super.CACHE = Caffeine.newBuilder()
                .maximumSize(gatewayBlackConfig.getId().getCaffeineMaxSize())
                .expireAfterWrite(gatewayBlackConfig.getId().getCaffeineExpireTime(), TimeUnit.MINUTES)
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
