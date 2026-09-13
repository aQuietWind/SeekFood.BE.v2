package com.seek.food.fund.Caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.seek.food.config.NacosConfig.Fund.FundCaffeineConfig;
import com.seek.food.dto.Fund.FundDTO;
import com.seek.food.dto.Fund.FundOrderRefundRecordDTO;
import com.seek.food.util.Caffeine.JvmCaffeineParent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class FundOrderRefundRecordCaffeine extends JvmCaffeineParent<Long, FundOrderRefundRecordDTO> {
    // 构造注入配置
    private final FundCaffeineConfig fundCaffeineConfig;
    @Autowired
    public FundOrderRefundRecordCaffeine(FundCaffeineConfig fundCaffeineConfig) {
        this.fundCaffeineConfig = fundCaffeineConfig;
    }

    // 容器启动构建缓存
    @PostConstruct
    public void init() {
        super.CACHE = Caffeine.newBuilder()
                .maximumSize(fundCaffeineConfig.getFundOrderRefundRecord().getMaxSize())
                .expireAfterWrite(fundCaffeineConfig.getFundOrderRefundRecord().getExpireTime(), TimeUnit.MINUTES)
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
