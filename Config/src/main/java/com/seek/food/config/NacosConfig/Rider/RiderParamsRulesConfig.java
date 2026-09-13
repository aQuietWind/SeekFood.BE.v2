package com.seek.food.config.NacosConfig.Rider;

import com.seek.food.config.Enum.ConfigKeyEnum;
import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.time.LocalDateTime;

@Data
@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Rider_Params_Rules_Config)
public class RiderParamsRulesConfig {
    private String riderPersonImageDest;
}
