package com.seek.food.config.NacosConfig.User;

import com.seek.food.config.Enum.ConfigKeyEnum;
import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.function.Function;

@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.User_Params_Rules_Config)
@Data
public class UserParamsRulesConfig {
    private int usernameLengthMax;
    private String headerImageDest;

    //------------------------
    //校验参数
    public void usernameCheck(String username) {
        if (username!=null&&username.length()>usernameLengthMax) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    };
}
