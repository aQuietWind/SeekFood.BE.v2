package com.seek.food.config.NacosConfig.Admin;

import com.seek.food.config.Enum.ConfigKeyEnum;
import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.time.LocalDateTime;

@Data
@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Admin_Params_Rules_Config)
public class AdminParamsRulesConfig {
    private String adminName;
    private String adminPassword;
    private int suggestionDescriptionMax;
    private String suggestionImageDest;

    //登录检测
    public void adminLoginCheck(String name,String password) {
        if (!adminName.equals(name)||!adminPassword.equals(password)) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }

    public void suggestionDescriptionCheck(String description) {
        if (description!=null&&(description.isBlank()||description.length()>suggestionDescriptionMax) )throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }
}
