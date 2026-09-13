package com.seek.food.voucher.Service.Impl;

import com.seek.food.config.Data.RedisKeyData;
import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.Voucher.VoucherRedisKeyConfig;
import com.seek.food.dto.Voucher.VoucherConnectionDTO;
import com.seek.food.util.Context.TokenIdContext;
import com.seek.food.util.Redis.RedisUtil;
import com.seek.food.voucher.Caffeine.VoucherConnectionCaffeine;
import com.seek.food.voucher.Mapper.VoucherConnectionMapper;
import com.seek.food.voucher.Service.VoucherConnectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RefreshScope
@Slf4j
public class VoucherConnectionServiceImpl implements VoucherConnectionService {


    private final CommonParamRulesConfig commonParamRulesConfig;
    private final StringRedisTemplate stringRedisTemplate;
    private final VoucherRedisKeyConfig voucherRedisKeyConfig;
    private final VoucherConnectionMapper voucherConnectionMapper;
    private final VoucherConnectionCaffeine voucherConnectionCaffeine;

    public VoucherConnectionServiceImpl(CommonParamRulesConfig commonParamRulesConfig, StringRedisTemplate stringRedisTemplate, VoucherRedisKeyConfig voucherRedisKeyConfig, VoucherConnectionMapper voucherConnectionMapper, VoucherConnectionCaffeine voucherConnectionCaffeine) {
        this.commonParamRulesConfig = commonParamRulesConfig;
        this.stringRedisTemplate = stringRedisTemplate;
        this.voucherRedisKeyConfig = voucherRedisKeyConfig;
        this.voucherConnectionMapper = voucherConnectionMapper;
        this.voucherConnectionCaffeine = voucherConnectionCaffeine;
    }

    //用户批量获取该持有关系
    @Override
    public List<VoucherConnectionDTO> getSimple(int start, int need){
        //格式校验
        commonParamRulesConfig.needNumberCheck(need);
        //获取用户id
        long userId=quickGetUserId();
        //冷却期
        quickCooldown(voucherRedisKeyConfig.getVoucherConnectionGetSimpleCooldown(),userId);
        //查询
        return voucherConnectionMapper.getSimple(start,need,userId);
    }

    //用户批量获取有效持有关系
    @Override
    public List<VoucherConnectionDTO> getSimpleEffective(int start, int need){
        //格式校验
        commonParamRulesConfig.needNumberCheck(need);
        //获取用户id
        long userId=quickGetUserId();
        //冷却期
        quickCooldown(voucherRedisKeyConfig.getVoucherConnectionGetSimpleEffectiveCooldown(),userId);
        //查询
        return voucherConnectionMapper.getSimpleEffective(start,need,userId);
    }

    //用户获取持有关系的详细信息
    @Override
    public VoucherConnectionDTO getDetail(long connectionId){
        //格式校验
        commonParamRulesConfig.commonIdCheck(connectionId);
        //获取用户id
        long userId=quickGetUserId();
        //查询,并且写入缓存,不需要别的id，因为优惠券是公开的，所有人都应该查得到，并且还要提供给用户端使用
        return voucherConnectionCaffeine.getAndAutoLoad(connectionId,stringRedisTemplate
                , voucherRedisKeyConfig.getVoucherConnectionMessageCaffeine().getRedisKey(connectionId)
                , voucherRedisKeyConfig.getVoucherConnectionMessageCaffeine().getDuration(), VoucherConnectionDTO.class
                , k-> voucherConnectionMapper.getDetail(connectionId,userId));
    }

    //判断用户是否从活动中已经获取了优惠券
    @Override
    public boolean exist(long promotionId){
        return voucherConnectionMapper.exist(quickGetUserId(),promotionId);
    }

    //锁定某优惠券为某订单使用
    @Override
    public boolean lock(long connectionId,long orderId){
        return voucherConnectionMapper.lock(quickGetUserId(),connectionId,orderId);
    }

    //检查某优惠券是否符合条件
    @Override
    public Double check(long connectionId,double cost){
        return voucherConnectionMapper.check(quickGetUserId(),connectionId,cost);
    }

    //回滚优惠券，并且清除缓存
    @Override
    public void rollback(long orderId){
        Long connectionId=voucherConnectionMapper.getConnectionIdByOrderId(orderId);
        if (connectionId==null)return;
        voucherConnectionMapper.rollback(orderId);
        quickClearCaffeine(connectionId);
    }



    private long quickGetUserId(){
        return TokenIdContext.getAndCheck(commonParamRulesConfig.getUserIdStart(),commonParamRulesConfig.getIdCapacity());
    }

    private void quickCooldown(RedisKeyData key, Object id){
        RedisUtil.checkCooldown(stringRedisTemplate,key.getRedisKey(id),key.getDuration());
    }

    private void quickClearCaffeine(long connectionId){
        voucherConnectionCaffeine.deleteAllCaffeine(connectionId,stringRedisTemplate
                ,voucherRedisKeyConfig.getVoucherConnectionMessageCaffeine().getRedisKey(connectionId));
    }
}
