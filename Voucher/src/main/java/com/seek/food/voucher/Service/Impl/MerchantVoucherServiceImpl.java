package com.seek.food.voucher.Service.Impl;

import com.seek.food.config.Data.RedisKeyData;
import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.Voucher.VoucherParamsRulesConfig;
import com.seek.food.config.NacosConfig.Voucher.VoucherRedisKeyConfig;
import com.seek.food.dto.Voucher.MerchantVoucherDTO;
import com.seek.food.util.CommonUtil.IdUtil;
import com.seek.food.util.Context.TokenIdContext;
import com.seek.food.util.Redis.RedisUtil;
import com.seek.food.voucher.Caffeine.MerchantVoucherCaffeine;
import com.seek.food.voucher.Mapper.MerchantVoucherMapper;
import com.seek.food.voucher.Service.MerchantVoucherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RefreshScope
@Slf4j
public class MerchantVoucherServiceImpl implements MerchantVoucherService {


    private final VoucherParamsRulesConfig voucherParamsRulesConfig;
    private final CommonParamRulesConfig commonParamRulesConfig;
    private final StringRedisTemplate stringRedisTemplate;
    private final VoucherRedisKeyConfig voucherRedisKeyConfig;
    private final MerchantVoucherMapper merchantVoucherMapper;
    private final MerchantVoucherCaffeine merchantVoucherCaffeine;

    public MerchantVoucherServiceImpl(VoucherParamsRulesConfig voucherParamsRulesConfig, CommonParamRulesConfig commonParamRulesConfig, StringRedisTemplate stringRedisTemplate, VoucherRedisKeyConfig voucherRedisKeyConfig, MerchantVoucherMapper merchantVoucherMapper, MerchantVoucherCaffeine merchantVoucherCaffeine) {
        this.voucherParamsRulesConfig = voucherParamsRulesConfig;
        this.commonParamRulesConfig = commonParamRulesConfig;
        this.stringRedisTemplate = stringRedisTemplate;
        this.voucherRedisKeyConfig = voucherRedisKeyConfig;
        this.merchantVoucherMapper = merchantVoucherMapper;
        this.merchantVoucherCaffeine = merchantVoucherCaffeine;
        //初始化id计数器
        stringRedisTemplate.opsForValue().setIfAbsent(voucherRedisKeyConfig.getMerchantVoucherIdCount().getName(),""+commonParamRulesConfig.getIdCapacity());
    }

    //新增商家优惠券
    @Override
    public void insertMerchantVoucher(MerchantVoucherDTO voucher){
        //校验格式
        voucherParamsRulesConfig.voucherDescriptionCheck(voucher.getVoucherDescription());
        voucherParamsRulesConfig.voucherNameCheck(voucher.getVoucherName());
        voucherParamsRulesConfig.durationDayCheck(voucher.getStartTime(), voucher.getEndTime());
        voucherParamsRulesConfig.discountCostCheck(voucher.getDiscountCost());
        voucherParamsRulesConfig.minCostCheck(voucher.getMinCost());
        //获取商家id
        long merchantId=quickGetMerchantId();
        //校验冷却期
        quickCooldown(voucherRedisKeyConfig.getMerchantVoucherInsertCooldown(),merchantId);
        //将必要的数据放进实体类
        voucher.setVoucherId(IdUtil.IdGenerateByIncrease(voucherRedisKeyConfig.getMerchantVoucherIdCount().getName(),stringRedisTemplate));
        voucher.setMerchantId(merchantId);
        //写入MySQL
        merchantVoucherMapper.insertMerchantVoucher(voucher);
    }

    //获取简易信息
    @Override
    public List<MerchantVoucherDTO> getSimple(int start, int need){
        //格式校验
        commonParamRulesConfig.needNumberCheck(need);
        //获取商家id
        long merchantId=quickGetMerchantId();
        //冷却期
        quickCooldown(voucherRedisKeyConfig.getMerchantVoucherGetSimpleCooldown(),merchantId);
        //查询
        return merchantVoucherMapper.getSimple(start,need,merchantId);
    }

    //获取还处于有效期中的商家优惠券简易信息
    @Override
    public List<MerchantVoucherDTO> getSimpleEffective(int start, int need){
        //格式校验
        commonParamRulesConfig.needNumberCheck(need);
        //获取商家id
        long merchantId=quickGetMerchantId();
        //冷却期
        quickCooldown(voucherRedisKeyConfig.getMerchantVoucherGetSimpleEffectiveCooldown(),merchantId);
        //查询
        return merchantVoucherMapper.getSimpleEffective(start,need,merchantId);
    }

    //获取详细的商家优惠券信息
    @Override
    public MerchantVoucherDTO getDetail(long voucherId){
        //格式校验
        commonParamRulesConfig.commonIdCheck(voucherId);
        //查询,并且写入缓存,不需要别的id，因为优惠券是公开的，所有人都应该查得到，并且还要提供给用户端使用
        return merchantVoucherCaffeine.getAndAutoLoad(voucherId,stringRedisTemplate
        , voucherRedisKeyConfig.getMerchantVoucherMessageCaffeine().getRedisKey(voucherId)
        , voucherRedisKeyConfig.getMerchantVoucherMessageCaffeine().getDuration(),MerchantVoucherDTO.class
        , k-> merchantVoucherMapper.getDetail(voucherId));
    }

    //检测某商家是否真的有某优惠券
    @Override
    public Boolean exist(long voucherId){
        return merchantVoucherMapper.exist(quickGetMerchantId(),voucherId);
    }

    private long quickGetMerchantId(){
        return TokenIdContext.getAndCheck(commonParamRulesConfig.getMerchantIdStart(),commonParamRulesConfig.getIdCapacity());
    }

    private void quickCooldown(RedisKeyData key,Object id){
        RedisUtil.checkCooldown(stringRedisTemplate,key.getRedisKey(id),key.getDuration());
    }
}
