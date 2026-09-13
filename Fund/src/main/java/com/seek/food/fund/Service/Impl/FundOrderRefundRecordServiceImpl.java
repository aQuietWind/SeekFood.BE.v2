package com.seek.food.fund.Service.Impl;

import com.seek.food.config.Data.RedisKeyData;
import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.Fund.FundRedisKeyConfig;
import com.seek.food.dto.Fund.FundOrderRecordDTO;
import com.seek.food.dto.Fund.FundOrderRefundRecordDTO;
import com.seek.food.fund.Caffeine.FundOrderRecordCaffeine;
import com.seek.food.fund.Caffeine.FundOrderRefundRecordCaffeine;
import com.seek.food.fund.Mapper.FundOrderRefundRecordMapper;
import com.seek.food.fund.Service.FundOrderRefundRecordService;
import com.seek.food.util.Context.TokenIdContext;
import com.seek.food.util.Redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RefreshScope
public class FundOrderRefundRecordServiceImpl implements FundOrderRefundRecordService {

    private final CommonParamRulesConfig commonParamRulesConfig;
    private final StringRedisTemplate stringRedisTemplate;
    private final FundRedisKeyConfig fundRedisKeyConfig;
    private final FundOrderRefundRecordMapper fundOrderRefundRecordMapper;
    private final FundOrderRefundRecordCaffeine fundOrderRefundRecordCaffeine;

    public FundOrderRefundRecordServiceImpl(CommonParamRulesConfig commonParamRulesConfig, StringRedisTemplate stringRedisTemplate
            , FundRedisKeyConfig fundRedisKeyConfig,FundOrderRefundRecordMapper fundOrderRefundRecordMapper
            , FundOrderRefundRecordCaffeine fundOrderRefundRecordCaffeine) {
        this.commonParamRulesConfig = commonParamRulesConfig;
        this.stringRedisTemplate = stringRedisTemplate;
        this.fundRedisKeyConfig = fundRedisKeyConfig;
        this.fundOrderRefundRecordMapper = fundOrderRefundRecordMapper;
        this.fundOrderRefundRecordCaffeine = fundOrderRefundRecordCaffeine;
    }

    //批量获取信息
    @Override
    public List<FundOrderRefundRecordDTO> getSimple(int start, int need){
        //检查需求量
        commonParamRulesConfig.needNumberCheck(need);
        //获取tokenId
        long userId= quickGetUserId();
        //检查冷却期
        quickCooldown(fundRedisKeyConfig.getFundGetSimpleOrderRefundCooldown(),userId);
        //返回查询结果
        return fundOrderRefundRecordMapper.getSimple(start,need,userId);
    }


    private long quickGetUserId(){
        return TokenIdContext.getAndCheck(commonParamRulesConfig.getUserIdStart(),commonParamRulesConfig.getIdCapacity());
    }

    private void quickCooldown(RedisKeyData keyData, long id) {
        RedisUtil.checkCooldown(stringRedisTemplate,keyData.getRedisKey(id), keyData.getDuration());
    }
}
