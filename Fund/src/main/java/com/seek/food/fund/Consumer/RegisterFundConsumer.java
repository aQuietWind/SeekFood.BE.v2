package com.seek.food.fund.Consumer;

import com.seek.food.config.Enum.MQNameKeyEnum;
import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.Fund.FundParamsRulesConfig;
import com.seek.food.config.NacosConfig.Fund.FundRedisKeyConfig;
import com.seek.food.fund.Mapper.FundMapper;
import com.seek.food.util.CommonUtil.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RegisterFundConsumer {
    private final FundMapper fundMapper;
    @Autowired
    public RegisterFundConsumer(FundMapper fundMapper) {
        this.fundMapper = fundMapper;
    }

    @RabbitListener(queues = MQNameKeyEnum.User_Exchange_Register_Fund_Queue)
    public void registerFundQueue(long accountId){
        try {
            fundMapper.insertFund(accountId);
        }catch (Exception e){
            log.error("account:{} ,新增Fund表数据时出现异常",accountId,e);
        }
    }
















}
