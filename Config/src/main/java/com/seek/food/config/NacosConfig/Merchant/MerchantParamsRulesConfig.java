package com.seek.food.config.NacosConfig.Merchant;

import com.seek.food.config.Enum.ConfigKeyEnum;
import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Data
@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Merchant_Params_Rules_Config)
public class MerchantParamsRulesConfig {
    private int merchantNameMax;
    private int proofImageNumberMax;
    private int showImageNumberMax;
    private int showDescriptionMax;
    private int merchantAddrMax;
    private String masterImageDest;
    private String proofImageDest;
    private String showImageDest;
    private String homeImageDest;

    public void merchantNameCheck(String merchantName) {
        if (merchantName == null) return;
        if (merchantName.isBlank()||merchantName.length()>merchantNameMax)throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }

    public void showDescriptionCheck(String showDescription) {
        if (showDescription != null&&showDescription.length()>merchantNameMax) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }

    public void merchantAddrCheck(String merchantAddr) {
        if (merchantAddr != null&&merchantAddr.length()>merchantAddrMax) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }
}
