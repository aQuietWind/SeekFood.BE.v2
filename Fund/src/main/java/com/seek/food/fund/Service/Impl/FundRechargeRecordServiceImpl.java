package com.seek.food.fund.Service.Impl;

import com.seek.food.config.Data.RedisKeyData;
import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.Fund.FundParamsRulesConfig;
import com.seek.food.config.NacosConfig.Fund.FundRedisKeyConfig;
import com.seek.food.dto.Fund.FundRechargeRecordDTO;
import com.seek.food.fund.Caffeine.FundRechargeRecordCaffeine;
import com.seek.food.fund.Mapper.FundMapper;
import com.seek.food.fund.Mapper.FundRechargeRecordMapper;
import com.seek.food.fund.Service.FundRechargeRecordService;
import com.seek.food.fund.Service.FundService;
import com.seek.food.util.CommonUtil.IdUtil;
import com.seek.food.util.Context.TokenIdContext;
import com.seek.food.util.Redis.RedisUtil;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RefreshScope
public class FundRechargeRecordServiceImpl implements FundRechargeRecordService {
    private final FundMapper fundMapper;
    private final FundRechargeRecordMapper fundRechargeRecordMapper;
    private final CommonParamRulesConfig commonParamRulesConfig;
    private final FundParamsRulesConfig fundParamsRulesConfig;
    private final StringRedisTemplate stringRedisTemplate;
    private final FundRedisKeyConfig fundRedisKeyConfig;
    private final FundRechargeRecordCaffeine fundRechargeRecordCaffeine;
    private final FundService fundService;

    public FundRechargeRecordServiceImpl(FundMapper fundMapper, FundRechargeRecordMapper fundRechargeRecordMapper
            , CommonParamRulesConfig commonParamRulesConfig, FundParamsRulesConfig fundParamsRulesConfig, StringRedisTemplate stringRedisTemplate
            , FundRedisKeyConfig fundRedisKeyConfig, FundRechargeRecordCaffeine fundRechargeRecordCaffeine, FundService fundService) {
        this.fundMapper = fundMapper;
        this.fundRechargeRecordMapper = fundRechargeRecordMapper;
        this.commonParamRulesConfig = commonParamRulesConfig;
        this.fundParamsRulesConfig = fundParamsRulesConfig;
        this.stringRedisTemplate = stringRedisTemplate;
        this.fundRedisKeyConfig = fundRedisKeyConfig;
        this.fundRechargeRecordCaffeine = fundRechargeRecordCaffeine;
        this.fundService = fundService;
        stringRedisTemplate.opsForValue().setIfAbsent(fundRedisKeyConfig.getFundRechargeRecordIdCount().getName(),""+commonParamRulesConfig.getIdCapacity());
    }

    //查看简单的充值记录
    @Override
    public List<FundRechargeRecordDTO> getSimpleRechargeRecord(int start, int need){
        //检查需求量
        commonParamRulesConfig.needNumberCheck(need);
        //获取tokenId
        long tokenId=TokenIdContext.getAndToLong();
        //检查冷却期
        quickCooldown(fundRedisKeyConfig.getFundGetSimpleRechargeCooldown(),tokenId);
        //返回查询结果
        return fundRechargeRecordMapper.getSimple(tokenId,start,need);
    }

    //查看详细的充值记录
    @Override
    public FundRechargeRecordDTO getDetailRechargeRecord(long recordId){
        commonParamRulesConfig.commonIdCheck(recordId);
        long tokenId=TokenIdContext.getAndToLong();
        return fundRechargeRecordCaffeine.getAndAutoLoad(recordId,stringRedisTemplate,fundRedisKeyConfig.getFundRechargeRecordCaffeineMessage().getRedisKey(recordId)
        ,fundRedisKeyConfig.getFundRechargeRecordCaffeineMessage().getDuration(),FundRechargeRecordDTO.class,k->fundRechargeRecordMapper.getDetail(tokenId,recordId));
    }

    //充值
    @Override
    @Transactional
    public void recharge(int rechargeAmount,String description){
        //检查金额大小与描述文本
        fundParamsRulesConfig.rechargeAmountCheck(rechargeAmount);
        fundParamsRulesConfig.descriptionCheck(description);
        //只允许用户充值
        long userId=quickGetUserId();
        //检查冷却
        quickCooldown(fundRedisKeyConfig.getFundRechargeCooldown(),userId);
        //写入DB
        fundService.increaseFund(rechargeAmount,userId);
        //写入记录档
        fundRechargeRecordMapper.insertRechargeRecord(new FundRechargeRecordDTO(
                IdUtil.IdGenerateByIncrease(fundRedisKeyConfig.getFundRechargeRecordIdCount().getName(),stringRedisTemplate)
        ,userId,description, (double) rechargeAmount,null));
    }


    private long quickGetUserId(){
        return TokenIdContext.getAndCheck(commonParamRulesConfig.getUserIdStart(),commonParamRulesConfig.getIdCapacity());
    }

    private void quickCooldown(RedisKeyData keyData, long id) {
        RedisUtil.checkCooldown(stringRedisTemplate,keyData.getRedisKey(id), keyData.getDuration());
    }
}
