package com.seek.food.config.NacosConfig.Common;


import com.seek.food.config.Data.JWTData;
import com.seek.food.config.Enum.ConfigKeyEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.HashMap;

@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.JWT_Config)
@Data
public class JWTConfig {
    private JWTData user;
    private JWTData merchant;
    private JWTData rider;
    private JWTData admin;
    private String headerSeparator;
    private String headerTokenName;
    private String RequestTokenName;
    private String maxStore;

    public JWTData[] getAllJWTData() {
        return new JWTData[]{user, merchant, rider, admin};
    }

}
