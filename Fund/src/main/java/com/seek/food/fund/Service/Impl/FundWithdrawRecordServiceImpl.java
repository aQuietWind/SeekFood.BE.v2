package com.seek.food.fund.Service.Impl;

import com.seek.food.config.Data.RedisKeyData;
import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.Fund.FundParamsRulesConfig;
import com.seek.food.config.NacosConfig.Fund.FundRedisKeyConfig;
import com.seek.food.dto.Fund.FundWithdrawRecordDTO;
import com.seek.food.fund.Caffeine.FundWithdrawRecordCaffeine;
import com.seek.food.fund.Mapper.FundWithdrawRecordMapper;
import com.seek.food.fund.Service.FundService;
import com.seek.food.fund.Service.FundWithdrawRecordService;
import com.seek.food.util.CommonUtil.IdUtil;
import com.seek.food.util.Context.TokenIdContext;
import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;
import com.seek.food.util.Redis.RedisUtil;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RefreshScope
public class FundWithdrawRecordServiceImpl implements FundWithdrawRecordService {
    private final FundWithdrawRecordMapper fundWithdrawRecordMapper;
    private final CommonParamRulesConfig commonParamRulesConfig;
    private final FundParamsRulesConfig fundParamsRulesConfig;
    private final StringRedisTemplate stringRedisTemplate;
    private final FundRedisKeyConfig fundRedisKeyConfig;
    private final FundWithdrawRecordCaffeine fundWithdrawRecordCaffeine;
    private final FundService fundService;

    public FundWithdrawRecordServiceImpl(FundWithdrawRecordMapper fundWithdrawRecordMapper
            , CommonParamRulesConfig commonParamRulesConfig, FundParamsRulesConfig fundParamsRulesConfig, StringRedisTemplate stringRedisTemplate
            , FundRedisKeyConfig fundRedisKeyConfig, FundWithdrawRecordCaffeine fundWithdrawRecordCaffeine, FundService fundService) {
        this.fundWithdrawRecordMapper = fundWithdrawRecordMapper;
        this.commonParamRulesConfig = commonParamRulesConfig;
        this.fundParamsRulesConfig = fundParamsRulesConfig;
        this.stringRedisTemplate = stringRedisTemplate;
        this.fundRedisKeyConfig = fundRedisKeyConfig;
        this.fundWithdrawRecordCaffeine = fundWithdrawRecordCaffeine;
        this.fundService = fundService;
        stringRedisTemplate.opsForValue().setIfAbsent(fundRedisKeyConfig.getFundWithdrawRecordIdCount().getName(),""+commonParamRulesConfig.getIdCapacity());
    }


    //查看简单的充值记录
    @Override
    public List<FundWithdrawRecordDTO> getSimpleWithdrawRecord(int start, int need){
        //检查需求量
        commonParamRulesConfig.needNumberCheck(need);
        //获取tokenId
        long tokenId=TokenIdContext.getAndToLong();
        //检查冷却期
        quickCooldown(fundRedisKeyConfig.getFundGetSimpleWithdrawCooldown(),tokenId);
        //返回查询结果
        return fundWithdrawRecordMapper.getSimple(tokenId,start,need);
    }

    //查看详细的充值记录
    @Override
    public FundWithdrawRecordDTO getDetailWithdrawRecord(long recordId){
        commonParamRulesConfig.commonIdCheck(recordId);
        long tokenId=TokenIdContext.getAndToLong();
        return fundWithdrawRecordCaffeine.getAndAutoLoad(recordId,stringRedisTemplate
                ,fundRedisKeyConfig.getFundWithdrawRecordCaffeineMessage().getRedisKey(recordId)
                ,fundRedisKeyConfig.getFundWithdrawRecordCaffeineMessage().getDuration()
                ,FundWithdrawRecordDTO.class,k->fundWithdrawRecordMapper.getDetail(tokenId,recordId));
    }


    //提现
    @Override
    public void withdraw(int withdrawAmount,String description){
        //检查金额大小与描述文本
        fundParamsRulesConfig.withdrawAmountCheck(withdrawAmount);
        fundParamsRulesConfig.descriptionCheck(description);
        //只允许商家和骑手提现
        long tokenId=quickGetRiderAndMerchantId();
        //检查冷却
        quickCooldown(fundRedisKeyConfig.getFundWithdrawCooldown(),tokenId);
        //写入MySQL
        fundService.decreaseFund(withdrawAmount,tokenId);
        //在Mysql写入记录
        fundWithdrawRecordMapper.insertWithdrawRecord(new FundWithdrawRecordDTO(
                IdUtil.IdGenerateByIncrease(fundRedisKeyConfig.getFundWithdrawRecordIdCount().getName(),stringRedisTemplate),
                tokenId,
                description,
                (double) withdrawAmount,
                null
        ));
    }

    private long quickGetRiderAndMerchantId(){
        long tokenId=TokenIdContext.getAndToLong();
        int idStart= Math.toIntExact(tokenId / commonParamRulesConfig.getIdCapacity());
        if (idStart!=commonParamRulesConfig.getMerchantIdStart()&&idStart!=commonParamRulesConfig.getRiderIdStart()){
            throw new BizException(ErrorCodeEnum.PARAM_ERROR);
        }
        return tokenId;
    }

    private void quickCooldown(RedisKeyData keyData, long id) {
        RedisUtil.checkCooldown(stringRedisTemplate,keyData.getRedisKey(id), keyData.getDuration());
    }
}
