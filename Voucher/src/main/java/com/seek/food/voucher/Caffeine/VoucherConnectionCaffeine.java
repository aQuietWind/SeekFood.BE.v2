package com.seek.food.voucher.Caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.seek.food.config.NacosConfig.Voucher.VoucherCaffeineConfig;
import com.seek.food.dto.Voucher.VoucherConnectionDTO;
import com.seek.food.util.Caffeine.JvmCaffeineParent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class VoucherConnectionCaffeine extends JvmCaffeineParent<Long, VoucherConnectionDTO> {
    // 构造注入配置
    private final VoucherCaffeineConfig voucherCaffeineConfig;
    @Autowired
    public VoucherConnectionCaffeine(VoucherCaffeineConfig voucherCaffeineConfig) {
        this.voucherCaffeineConfig = voucherCaffeineConfig;
    }

    // 容器启动构建缓存
    @PostConstruct
    public void init() {
        super.CACHE = Caffeine.newBuilder()
                .maximumSize(voucherCaffeineConfig.getVoucherConnection().getMaxSize())
                .expireAfterWrite(voucherCaffeineConfig.getVoucherConnection().getExpireTime(), TimeUnit.MINUTES)
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
