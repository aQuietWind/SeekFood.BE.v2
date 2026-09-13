package com.seek.food.fund.Consumer;

import com.seek.food.config.Enum.MQNameKeyEnum;
import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.Fund.FundRedisKeyConfig;
import com.seek.food.fund.Service.FundOrderRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RollbackFundConsumer {
    private final FundOrderRecordService fundOrderRecordService;

    @Autowired
    public RollbackFundConsumer(StringRedisTemplate stringRedisTemplate, FundRedisKeyConfig fundRedisKeyConfig
            , CommonParamRulesConfig commonParamRulesConfig, FundOrderRecordService fundOrderRecordService) {
        stringRedisTemplate.opsForValue().setIfAbsent(fundRedisKeyConfig.getFundOrderRefundRecordIdCount().getName(),""+commonParamRulesConfig.getIdCapacity());
        this.fundOrderRecordService = fundOrderRecordService;
    }

    @RabbitListener(queues = MQNameKeyEnum.Order_Exchange_Rollback_Fund_Queue)
    public void rollbackFundQueue(Long orderId) {
        //确认退款
        fundOrderRecordService.ackRefund(orderId);
    }

}
