package com.seek.food.config.NacosConfig.Fund;

import com.seek.food.config.Data.CaffeineData;
import com.seek.food.config.Enum.ConfigKeyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Data
@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Fund_Caffeine_Config)
public class FundCaffeineConfig {
    private CaffeineData fund;
    private CaffeineData fundRechargeRecord;
    private CaffeineData fundWithdrawRecord;
    private CaffeineData fundOrderRefundRecord;
    private CaffeineData fundOrderRecord;
}
