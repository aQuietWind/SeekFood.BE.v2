package com.seek.food.config.NacosConfig.Order;

import com.seek.food.config.Enum.ConfigKeyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Order_Rider_Order_Es_Table_Config)
@Data
public class OrderRiderOrderEsTableConfig {
    //字段名动态指示
    private String IndexName;
    private String orderId;
    private String merchantId;
    private String deliveryAddress;
    private String deliveryLatLon;
    private String mealContent;
    private String number;
    private String riderCost;
    private String accept;
}
