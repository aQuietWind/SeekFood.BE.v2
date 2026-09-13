package com.seek.food.config.NacosConfig.Comment;

import com.seek.food.config.Enum.ConfigKeyEnum;
import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.time.LocalDateTime;

@Data
@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Comment_Params_Rules_Config)
public class CommentParamsRulesConfig {
    private int commentDescriptionMax;
    private String firstCommentImageDest;
    private String secondCommentImageDest;

    public void commentDescriptionCheck(String description) {
        if (description!=null&&(description.isBlank()||description.length()>commentDescriptionMax) )throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }
}
