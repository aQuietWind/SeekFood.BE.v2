package com.seek.food.config.NacosConfig.MQ;


import com.seek.food.config.Data.QueueData;
import com.seek.food.config.Enum.ConfigKeyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Comment_Exchange_Config)
@Data
public class CommentExchangeConfig {
    private String type;
    private String exchangeName;
    private QueueData deleteFileCommentQueue;
    private QueueData changeMerchantFirstCommentAmountQueue;
    private QueueData changeSecondCommentAmountQueue;
}
