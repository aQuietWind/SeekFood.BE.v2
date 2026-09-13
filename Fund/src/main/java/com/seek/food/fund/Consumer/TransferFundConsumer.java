package com.seek.food.fund.Consumer;

import com.seek.food.config.Enum.MQNameKeyEnum;
import com.seek.food.config.NacosConfig.Fund.FundRedisKeyConfig;
import com.seek.food.dto.Fund.FundRechargeRecordDTO;
import com.seek.food.fund.Mapper.FundMapper;
import com.seek.food.fund.Mapper.FundRechargeRecordMapper;
import com.seek.food.fund.Service.FundService;
import com.seek.food.util.CommonUtil.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransferFundConsumer {
    private final FundService fundService;
    private final StringRedisTemplate stringRedisTemplate;
    private final FundRechargeRecordMapper fundRechargeRecordMapper;
    private final FundRedisKeyConfig fundRedisKeyConfig;

    @Autowired
    public TransferFundConsumer(FundService fundService, StringRedisTemplate stringRedisTemplate
            , FundRechargeRecordMapper fundRechargeRecordMapper, FundRedisKeyConfig fundRedisKeyConfig) {
        this.fundService = fundService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.fundRechargeRecordMapper = fundRechargeRecordMapper;
        this.fundRedisKeyConfig = fundRedisKeyConfig;
    }

    @RabbitListener(queues = MQNameKeyEnum.Order_Exchange_Transfer_Fund_Queue)
    public void transferFundQueue(FundRechargeRecordDTO record){
        //写入DB
        fundService.increaseFund(record.getRechargeAmount(), record.getAccountId());
        record.setRecordId(IdUtil.IdGenerateByIncrease(fundRedisKeyConfig.getFundRechargeRecordIdCount().getName(),stringRedisTemplate));
        //写入记录档, 推荐用MySQL触发器实现自动生成记录
        fundRechargeRecordMapper.insertRechargeRecord(record);
    }




















}
