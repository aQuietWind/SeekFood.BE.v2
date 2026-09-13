package com.seek.food.fund.Service.Impl;

import com.seek.food.config.NacosConfig.Fund.FundRedisKeyConfig;
import com.seek.food.dto.Fund.FundDTO;
import com.seek.food.fund.Caffeine.FundCaffeine;
import com.seek.food.fund.Mapper.FundMapper;
import com.seek.food.fund.Service.FundService;
import com.seek.food.util.Context.TokenIdContext;
import com.seek.food.util.Redis.RedisUtil;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
@RefreshScope
public class FundServiceImpl implements FundService {
    private final FundMapper fundMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final FundRedisKeyConfig fundRedisKeyConfig;
    private final FundCaffeine fundCaffeine;

    public FundServiceImpl(FundMapper fundMapper, StringRedisTemplate stringRedisTemplate
            , FundRedisKeyConfig fundRedisKeyConfig, FundCaffeine fundCaffeine) {
        this.fundMapper = fundMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.fundRedisKeyConfig = fundRedisKeyConfig;
        this.fundCaffeine = fundCaffeine;
    }

    @Override
    public FundDTO getFund(){
        long tokenId= TokenIdContext.getAndToLong();
        //自动缓存
        return fundCaffeine.getAndAutoLoad(tokenId,stringRedisTemplate,fundRedisKeyConfig.getFundCaffeineMessage().getRedisKey(tokenId)
        ,fundRedisKeyConfig.getFundCaffeineMessage().getDuration(),FundDTO.class,k->fundMapper.getFund(tokenId));
    }

    @Override
    public void insertFund(){
        long tokenId= TokenIdContext.getAndToLong();
        RedisUtil.checkCooldown(stringRedisTemplate,fundRedisKeyConfig.getFundInsertCooldown().getRedisKey(tokenId)
                ,fundRedisKeyConfig.getFundInsertCooldown().getDuration());
        //尝试插入，如果失败则会被全局异常捕获器捕获
        fundMapper.insertFund(tokenId);
    }

    //减少资金
    @Override
    public void decreaseFund(double amount,long accountId){
        quickUpdateAndClearCaffeine(accountId,k->fundMapper.decreaseFund(accountId,amount));
    }

    //增加资金
    @Override
    public void increaseFund(double amount,long accountId){
        quickUpdateAndClearCaffeine(accountId,k->fundMapper.increaseFund(accountId,amount));
    }

    private void quickUpdateAndClearCaffeine(long accountId, Function<Long,Boolean> function){
        fundCaffeine.updateAndRemoveCaffeine(accountId,stringRedisTemplate,fundRedisKeyConfig.getFundCaffeineMessage().getRedisKey(accountId)
        ,function);
    }




}
