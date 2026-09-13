package com.seek.food.merchant.Caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.seek.food.config.NacosConfig.Merchant.MerchantCaffeineConfig;
import com.seek.food.config.NacosConfig.User.UserCaffeineConfig;
import com.seek.food.dto.Merchant.MerchantDTO;
import com.seek.food.dto.User.UserDTO;
import com.seek.food.util.Caffeine.JvmCaffeineParent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class MerchantCaffeine extends JvmCaffeineParent<Long, MerchantDTO> {
    // 构造注入配置
    private final MerchantCaffeineConfig merchantCaffeineConfig;
    @Autowired
    public MerchantCaffeine(MerchantCaffeineConfig merchantCaffeineConfig) {
        this.merchantCaffeineConfig = merchantCaffeineConfig;
    }

    // 容器启动构建缓存
    @PostConstruct
    public void init() {
        super.CACHE = Caffeine.newBuilder()
                .maximumSize(merchantCaffeineConfig.getMerchant().getMaxSize())
                .expireAfterWrite(merchantCaffeineConfig.getMerchant().getExpireTime(), TimeUnit.MINUTES)
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
