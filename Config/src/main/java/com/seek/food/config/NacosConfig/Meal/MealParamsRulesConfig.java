package com.seek.food.config.NacosConfig.Meal;

import com.seek.food.config.Enum.ConfigKeyEnum;
import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Meal_Params_Rules_Config)
public class MealParamsRulesConfig {
    private int mealNameMax;
    private int mealDescriptionMax;
    private int mealContentMax;
    private int mealTypeMax;
    private int mealFileDeleteDay;
    private String mealShowImageDest;

    public void mealNameCheck(String mealName) {
        if (mealName == null || mealName.isBlank() || mealName.length()>mealNameMax) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }

    public void mealDescriptionCheck(String mealDescription) {
        if (mealDescription!=null&&(mealDescription.isBlank()||mealDescription.length()>mealDescriptionMax) )throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }

    public void mealContentCheck(String mealContent) {
        if (mealContent!=null&&(mealContent.isBlank()||mealContent.length()>mealContentMax)) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }

    public void mealTypeCheck(Integer mealType) {
        if (mealType!=null&&(mealType<0||mealType>mealTypeMax)) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }

    public void mealPriceCheck(Double price) {
        if (price==null||price.isNaN()||price<0||String.valueOf(price).split("\\.")[1].length()>2) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }

    public void mealNextDiscountTimeCheck(LocalDateTime nextDiscountTime) {
        if (nextDiscountTime!=null&&nextDiscountTime.isBefore(LocalDateTime.now())) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }

    public String getMillsByFileDeleteDay() {
        return String.valueOf(mealFileDeleteDay*24*60*60*1000);
    }
}
