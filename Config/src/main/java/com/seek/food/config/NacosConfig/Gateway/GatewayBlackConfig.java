package com.seek.food.config.NacosConfig.Gateway;

import com.seek.food.config.Data.GatewayIpIdBlackData;
import com.seek.food.config.Enum.ConfigKeyEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Gateway_Black_Config)
@Data
public class GatewayBlackConfig {
    private GatewayIpIdBlackData ip;
    private GatewayIpIdBlackData id;
}
