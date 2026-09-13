package com.seek.food.fund.Consumer;

import com.seek.food.config.Enum.MQNameKeyEnum;
import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.Fund.FundParamsRulesConfig;
import com.seek.food.config.NacosConfig.Fund.FundRedisKeyConfig;
import com.seek.food.config.NacosConfig.MQ.FundExchangeConfig;
import com.seek.food.dto.Fund.FundOrderRecordDTO;
import com.seek.food.fund.Caffeine.FundOrderRecordCaffeine;
import com.seek.food.fund.Mapper.FundOrderRecordMapper;
import com.seek.food.util.CommonUtil.IdUtil;
import com.seek.food.util.MQ.MQUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RegisterFundOrderRecordConsumer {

    private final FundOrderRecordMapper fundOrderRecordMapper;
    private final FundParamsRulesConfig fundParamsRulesConfig;
    private final FundExchangeConfig fundExchangeConfig;
    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final FundRedisKeyConfig fundRedisKeyConfig;
    private final FundOrderRecordCaffeine fundOrderRecordCaffeine;

    @Autowired
    public RegisterFundOrderRecordConsumer(FundOrderRecordMapper fundOrderRecordMapper, FundParamsRulesConfig fundParamsRulesConfig
            , FundExchangeConfig fundExchangeConfig, RabbitTemplate rabbitTemplate, StringRedisTemplate stringRedisTemplate, FundRedisKeyConfig fundRedisKeyConfig, CommonParamRulesConfig commonParamRulesConfig, FundOrderRecordCaffeine fundOrderRecordCaffeine) {
        this.fundOrderRecordMapper = fundOrderRecordMapper;
        this.fundParamsRulesConfig = fundParamsRulesConfig;
        this.fundExchangeConfig = fundExchangeConfig;
        this.rabbitTemplate = rabbitTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.fundRedisKeyConfig = fundRedisKeyConfig;
        //初始化计数器
        stringRedisTemplate.opsForValue().setIfAbsent(fundRedisKeyConfig.getFundOrderRecordIdCount().getName(),""+commonParamRulesConfig.getIdCapacity());
        this.fundOrderRecordCaffeine = fundOrderRecordCaffeine;
    }

    @RabbitListener(queues = MQNameKeyEnum.Order_Exchange_Register_Fund_Order_Record_Queue)
    public void registerFundOrderRecordQueue(FundOrderRecordDTO record) {
        //设置期限
        record.setDeadline(fundParamsRulesConfig.getDeadline());
        record.setAbleRollbackTime(fundParamsRulesConfig.getRollbackTime());
        record.setRecordId(IdUtil.IdGenerateByIncrease(fundRedisKeyConfig.getFundOrderRecordIdCount().getName(),stringRedisTemplate));
        //新增该记录
        fundOrderRecordMapper.insertRecord(record);
        //删除可能存在的空缓存
        fundOrderRecordCaffeine.deleteAllCaffeine(record.getRecordId(),stringRedisTemplate
                ,fundRedisKeyConfig.getFundOrderRecordCaffeineMessage().getRedisKey(record.getRecordId()));
        //发送一条延时全局回滚消息,无论支付未支付，只要到回滚时间，统一回滚资金和优惠券
        MQUtil.sendWithTLL(fundExchangeConfig.getExchangeName(),fundExchangeConfig.getRollbackAllFundDeadLetterQueue().getRoutingKey()
                ,record.getOrderId() , rabbitTemplate , MQUtil.minuteToMillis(fundParamsRulesConfig.getRollbackMinuteMax())
        );
    }




















}
