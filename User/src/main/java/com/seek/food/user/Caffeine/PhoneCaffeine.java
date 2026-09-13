package com.seek.food.user.Caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.seek.food.config.NacosConfig.User.UserCaffeineConfig;
import com.seek.food.util.Caffeine.JvmCaffeineParent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class PhoneCaffeine extends JvmCaffeineParent<Long,String> {
    // 构造注入配置
    private final UserCaffeineConfig userCaffeineConfig;
    @Autowired
    public PhoneCaffeine(UserCaffeineConfig userCaffeineConfig) {
        this.userCaffeineConfig = userCaffeineConfig;
    }

    // 容器启动构建缓存
    @PostConstruct
    public void init() {
        super.CACHE = Caffeine.newBuilder()
                .maximumSize(userCaffeineConfig.getUser().getMaxSize())
                .expireAfterWrite(userCaffeineConfig.getUser().getExpireTime(), TimeUnit.MINUTES)
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
