package com.seek.food.config.NacosConfig.Fund;

import com.seek.food.config.Enum.ConfigKeyEnum;
import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.time.LocalDateTime;


@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Fund_Params_Rules_Config)
@Data
public class FundParamsRulesConfig {
    private int rechargeAmountMax;
    private int withdrawAmountMax;
    private int descriptionMax;
    private int deadlineMinuteMax;
    private int rollbackMinuteMax;


    public void rechargeAmountCheck(int rechargeAmount) {
        if (rechargeAmount > rechargeAmountMax||rechargeAmount <= 0) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }
    public void withdrawAmountCheck(int withdrawAmount) {
        if (withdrawAmount > withdrawAmountMax||withdrawAmount <= 0) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }
    public void descriptionCheck(String description) {
        if (description!=null &&  description.length()>descriptionMax) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }
    public LocalDateTime getDeadline() {
        return LocalDateTime.now().plusMinutes(deadlineMinuteMax);
    }
    public LocalDateTime getRollbackTime() {
        return LocalDateTime.now().plusMinutes(rollbackMinuteMax);
    }
}
