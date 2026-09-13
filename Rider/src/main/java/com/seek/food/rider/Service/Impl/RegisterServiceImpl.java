package com.seek.food.rider.Service.Impl;

import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.MQ.RiderExchangeConfig;
import com.seek.food.config.NacosConfig.Rider.RiderRedisKeyConfig;
import com.seek.food.dto.Rider.RiderDTO;
import com.seek.food.rider.Caffeine.RiderCaffeine;
import com.seek.food.rider.Mapper.RegisterMapper;
import com.seek.food.rider.Service.RegisterService;
import com.seek.food.util.CommonUtil.IdUtil;
import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;
import com.seek.food.util.MQ.MQUtil;
import com.seek.food.util.OPT.OPTUtil;
import com.seek.food.util.Redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


@Service
@RefreshScope
@Slf4j
public class RegisterServiceImpl implements RegisterService {
    private final CommonParamRulesConfig commonParamRulesConfig;
    private final StringRedisTemplate stringRedisTemplate;
    private final RiderRedisKeyConfig riderRedisKeyConfig;
    private final RiderExchangeConfig riderExchangeConfig;
    private final RabbitTemplate rabbitTemplate;
    private final RegisterMapper registerMapper;
    private final RiderCaffeine riderCaffeine;

    @Autowired
    public RegisterServiceImpl(CommonParamRulesConfig commonParamRulesConfig, StringRedisTemplate stringRedisTemplate
            , RiderRedisKeyConfig riderRedisKeyConfig, RiderExchangeConfig riderExchangeConfig, RabbitTemplate rabbitTemplate
            , RegisterMapper registerMapper, RiderCaffeine riderCaffeine) {
        this.commonParamRulesConfig = commonParamRulesConfig;
        this.stringRedisTemplate = stringRedisTemplate;
        this.riderRedisKeyConfig = riderRedisKeyConfig;
        this.riderExchangeConfig = riderExchangeConfig;
        this.rabbitTemplate = rabbitTemplate;
        this.registerMapper = registerMapper;
        //初始化id计数器
        stringRedisTemplate.opsForValue().setIfAbsent(riderRedisKeyConfig.getRiderIdCount().getName(),""+commonParamRulesConfig.getRiderIdStart()*commonParamRulesConfig.getIdCapacity());
        this.riderCaffeine = riderCaffeine;
    }


    //获取注册所需的验证码
    @Override
    public String registerGetOpt(String phoneNumber) {
        //验证手机号
        commonParamRulesConfig.phoneNumberCheck(phoneNumber);
        //生成验证码
        return OPTUtil.generateOPTAndRecord(stringRedisTemplate,riderRedisKeyConfig.getRegisterOpt().getRedisKey(phoneNumber)
                ,riderRedisKeyConfig.getRegisterOpt().getDuration(),6);
    }

    //注册骑手
    @Override
    public void registerRider(RiderDTO rider, String opt) {
        //验证格式
        commonParamRulesConfig.personNameCheck(rider.getRiderName());
        commonParamRulesConfig.codeCheck(rider.getRiderCode());
        commonParamRulesConfig.phoneNumberCheck(rider.getRiderPhoneNumber());
        commonParamRulesConfig.passwordCheck(rider.getRiderPassword());
        if (rider.getRiderSex()==null) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
        commonParamRulesConfig.sexCheck(rider.getRiderSex());
        //验证验证码
        OPTUtil.checkOPT(stringRedisTemplate,riderRedisKeyConfig.getRegisterOpt().getRedisKey(rider.getRiderPhoneNumber())
                , opt);
        //校验冷却期
        RedisUtil.checkCooldown(stringRedisTemplate,riderRedisKeyConfig.getRegisterCooldown().getRedisKey(rider.getRiderPhoneNumber())
                ,riderRedisKeyConfig.getRegisterCooldown().getDuration());
        //生成id
        rider.setRiderId(IdUtil.IdGenerateByIncrease(riderRedisKeyConfig.getRiderIdCount().getName(),stringRedisTemplate));
        //插入骑手
        registerMapper.registerRider(rider);
        //写入MQ
        MQUtil.send(riderExchangeConfig.getExchangeName(),riderExchangeConfig.getRegisterFundQueue().getRoutingKey(),rider.getRiderId()
                ,rabbitTemplate);
        //删除可能存在的空缓存（当有人试图在插入前查询该riderId时就会出现）
        riderCaffeine.deleteAllCaffeine(rider.getRiderId(),stringRedisTemplate,riderRedisKeyConfig.getRiderMessageCaffeine().getRedisKey(rider.getRiderId()));
    }








}
