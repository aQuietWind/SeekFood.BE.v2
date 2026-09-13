package com.seek.food.config.NacosConfig.Order;

import com.seek.food.config.Enum.ConfigKeyEnum;
import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Order_Params_Rules_Config)
@Data
public class OrderParamsRulesConfig {
    private int deliveryAddressMax;
    private int everyKmCost;
    private int stateNumberMax;

    //------------------------
    //校验参数
    public void deliveryAddressCheck(String address) {
        if (address==null||address.isBlank()||address.length()>deliveryAddressMax) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    };
    public void stateNumberCheck(int stateNumber) {
        if (stateNumber<0||stateNumber>stateNumberMax) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    };
    public double distanceCost(long distance) {
        return (double) ((distance/1000)*everyKmCost);
    };
}
