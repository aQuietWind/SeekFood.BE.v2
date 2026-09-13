package com.seek.food.promotion.Caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.seek.food.config.NacosConfig.Promotion.PromotionCaffeineConfig;
import com.seek.food.config.NacosConfig.Voucher.VoucherCaffeineConfig;
import com.seek.food.dto.Promotion.MerchantLoginPromotionDTO;
import com.seek.food.dto.Voucher.MerchantVoucherDTO;
import com.seek.food.util.Caffeine.JvmCaffeineParent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class MerchantLoginPromotionCaffeine extends JvmCaffeineParent<Long, MerchantLoginPromotionDTO> {
    // 构造注入配置
    private final PromotionCaffeineConfig promotionCaffeineConfig;
    @Autowired
    public MerchantLoginPromotionCaffeine(PromotionCaffeineConfig promotionCaffeineConfig) {
        this.promotionCaffeineConfig = promotionCaffeineConfig;
    }

    // 容器启动构建缓存
    @PostConstruct
    public void init() {
        super.CACHE = Caffeine.newBuilder()
                .maximumSize(promotionCaffeineConfig.getMerchantLoginPromotion().getMaxSize())
                .expireAfterWrite(promotionCaffeineConfig.getMerchantLoginPromotion().getExpireTime(), TimeUnit.MINUTES)
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
