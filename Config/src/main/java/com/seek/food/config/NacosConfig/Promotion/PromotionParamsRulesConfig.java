package com.seek.food.config.NacosConfig.Promotion;

import com.seek.food.config.Enum.ConfigKeyEnum;
import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.time.LocalDateTime;

@Data
@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Promotion_Params_Rules_Config)
public class PromotionParamsRulesConfig {
    private int promotionTitleMax;
    private int promotionDescriptionMax;
    private int promotionNoticeMax;
    private int grabAmountMin;
    private int durationDayMin;

    public void promotionTitleCheck(String title) {
        if (title == null || title.isBlank() || title.length()>promotionTitleMax) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }

    public void promotionDescriptionCheck(String description) {
        if (description!=null&&(description.isBlank()||description.length()>promotionDescriptionMax) )throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }

    public void promotionNoticeCheck(String notice) {
        if (notice!=null&&(notice.isBlank()||notice.length()>promotionNoticeMax) )throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }

    public void grabAmountCheck(Long grabAmount) {
        if (grabAmount==null||grabAmount<grabAmountMin) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }

    public void durationDayCheck(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime==null|| endTime==null  || startTime.isAfter(LocalDateTime.now()) || endTime.isBefore(startTime.plusDays(durationDayMin)) ) {
            throw new BizException(ErrorCodeEnum.PARAM_ERROR);
        }
    }
}
