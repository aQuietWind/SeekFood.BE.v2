package com.seek.food.config.NacosConfig.Voucher;

import com.seek.food.config.Enum.ConfigKeyEnum;
import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.time.LocalDateTime;
import java.util.List;

@Data
@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Voucher_Params_Rules_Config)
public class VoucherParamsRulesConfig {
    private int voucherNameMax;
    private int voucherDescriptionMax;
    private int durationDayMin;

    public void voucherNameCheck(String voucherName) {
        if (voucherName == null || voucherName.isBlank() || voucherName.length()>voucherNameMax) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }

    public void voucherDescriptionCheck(String voucherDescription) {
        if (voucherDescription!=null&&(voucherDescription.isBlank()||voucherDescription.length()>voucherDescriptionMax) )throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }

    public void durationDayCheck(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime==null|| endTime==null  || startTime.isAfter(LocalDateTime.now()) || endTime.isBefore(startTime.plusDays(durationDayMin)) ) {
            throw new BizException(ErrorCodeEnum.PARAM_ERROR);
        }
    }

    public void minCostCheck(Double minCost) {
        if (minCost==null||minCost<0) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
        costCheck(minCost);
    }

    public void discountCostCheck(Double discountCost) {
        if (discountCost==null||discountCost<0) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
        costCheck(discountCost);
    }

    public void costCheck(Double cost) {
        if (cost==null||cost.isNaN()||cost<0||String.valueOf(cost).split("\\.")[1].length()>2) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }
}
