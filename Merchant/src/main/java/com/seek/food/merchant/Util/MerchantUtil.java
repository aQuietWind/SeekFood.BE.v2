package com.seek.food.merchant.Util;

import com.seek.food.config.Data.RedisStreamData;
import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.Merchant.MerchantRedisKeyConfig;
import com.seek.food.config.NacosConfig.Merchant.MerchantRedisStreamConfig;
import com.seek.food.util.Redis.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MerchantUtil {
    private final StringRedisTemplate stringRedisTemplate;
    private final CommonParamRulesConfig commonParamRulesConfig;
    private final MerchantRedisKeyConfig merchantRedisKeyConfig;
    private final RedisStreamData esSyncStream;
    @Autowired
    public MerchantUtil(StringRedisTemplate stringRedisTemplate, CommonParamRulesConfig commonParamRulesConfig
            , MerchantRedisKeyConfig merchantRedisKeyConfig, MerchantRedisStreamConfig merchantRedisStreamConfig) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.commonParamRulesConfig = commonParamRulesConfig;
        this.merchantRedisKeyConfig = merchantRedisKeyConfig;
        this.esSyncStream = merchantRedisStreamConfig.getEsSyncStream();
    }
    //通知进行同步
    public void esSync(long merchantId){
        RedisUtil.oftenSetBitAndAct(stringRedisTemplate
                ,merchantRedisKeyConfig.getMerchantEsSyncRecord().getName()
                ,merchantId
                ,true
                ,commonParamRulesConfig.getIdCapacity()
                ,commonParamRulesConfig.getIdBitmapAreaNumber()
                ,()-> stringRedisTemplate.opsForStream().add(esSyncStream.getName(), Map.of(esSyncStream.getKeyName(),""+merchantId))
                );
    }
}
