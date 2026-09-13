package com.seek.food.config.NacosConfig.MQ;


import com.seek.food.config.Data.QueueData;
import com.seek.food.config.Enum.ConfigKeyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Order_Exchange_Config)
@Data
public class OrderExchangeConfig {
    private String type;
    private String exchangeName;
    private QueueData registerFundOrderRecordQueue;
    private QueueData rollbackVoucherQueue;
    private QueueData rollbackFundQueue;
    private QueueData transferFundQueue;
    private QueueData rollbackAllFundDeadLetterQueue;
    private QueueData changeMerchantOrderAmountQueue;
    private QueueData changeMealSalesVolumeQueue;
    private QueueData changeUserOrderAmountQueue;
    private QueueData chatRoomInitQueue;
    private QueueData chatRoomCompleteQueue;
}
