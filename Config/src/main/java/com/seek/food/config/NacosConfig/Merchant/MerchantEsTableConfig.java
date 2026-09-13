package com.seek.food.config.NacosConfig.Merchant;

import com.seek.food.config.Enum.ConfigKeyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Merchant_Es_Table_Config)
@Data
public class MerchantEsTableConfig {
    //字段名动态指示
    private String IndexName;
    private String merchantId;
    private String merchantName;
    private String merchantCollectAmount;
    private String merchantOrderAmount;
    private String merchantHomeImage;
    private String merchantLocation;
    private String isOpen;
    private String isDelete;
}
