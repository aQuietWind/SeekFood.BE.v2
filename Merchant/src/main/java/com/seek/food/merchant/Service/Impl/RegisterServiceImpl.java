package com.seek.food.merchant.Service.Impl;

import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.MQ.MerchantExchangeConfig;
import com.seek.food.config.NacosConfig.Merchant.MerchantRedisKeyConfig;
import com.seek.food.dto.Merchant.MerchantEsDTO;
import com.seek.food.merchant.EsRepository.MerchantRepository;
import com.seek.food.merchant.Mapper.RegisterMapper;
import com.seek.food.merchant.Service.RegisterService;
import com.seek.food.util.CommonUtil.IdUtil;
import com.seek.food.util.MQ.MQUtil;
import com.seek.food.util.OPT.OPTUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RefreshScope
@Service
@Slf4j
public class RegisterServiceImpl implements RegisterService {
    private final RegisterMapper registerMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final MerchantRedisKeyConfig merchantRedisKeyConfig;
    private final CommonParamRulesConfig commonParamRulesConfig;
    private final MerchantRepository  merchantRepository;
    private final RabbitTemplate rabbitTemplate;
    private final MerchantExchangeConfig merchantExchangeConfig;
    public RegisterServiceImpl(RegisterMapper registerMapper,StringRedisTemplate stringRedisTemplate
    ,MerchantRedisKeyConfig merchantRedisKeyConfig,CommonParamRulesConfig commonParamRulesConfig,MerchantRepository merchantRepository
    ,RabbitTemplate rabbitTemplate,MerchantExchangeConfig merchantExchangeConfig) {
        this.registerMapper = registerMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.merchantRedisKeyConfig = merchantRedisKeyConfig;
        this.commonParamRulesConfig = commonParamRulesConfig;
        this.merchantRepository = merchantRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.merchantExchangeConfig = merchantExchangeConfig;
        stringRedisTemplate.opsForValue().setIfAbsent(merchantRedisKeyConfig.getMerchantIdCount().getName(),
                ""+commonParamRulesConfig.getMerchantIdStart()*commonParamRulesConfig.getIdCapacity());
    }

    @Override
    public String getRegisterOpt(String phoneNumber){
        //检查手机号
        commonParamRulesConfig.phoneNumberCheck(phoneNumber);
        //返回验证码
        return OPTUtil.generateOPTAndRecord(stringRedisTemplate,merchantRedisKeyConfig.getMerchantRegisterOpt().getName()+phoneNumber
        ,merchantRedisKeyConfig.getMerchantRegisterOpt().getDuration(),6);
    }

    @Override
    @Transactional
    public void toRegister(String phoneNumber,String opt,String password) {
        //检查信息
        commonParamRulesConfig.phoneNumberCheck(phoneNumber);
        commonParamRulesConfig.passwordCheck(password);
        //检查验证码
        OPTUtil.checkOPT(stringRedisTemplate,merchantRedisKeyConfig.getMerchantRegisterOpt().getName()+phoneNumber,opt);
        //获取id
        long merchantId=IdUtil.IdGenerateByIncrease(merchantRedisKeyConfig.getMerchantIdCount().getName(),stringRedisTemplate);
        //插入商家
        registerMapper.insertMerchant(merchantId,phoneNumber,password);
        //插入es
        merchantRepository.save(new MerchantEsDTO(merchantId,"商家"+merchantId
                ,0,0,null,null,false,false));
        //发至mq,使其进行初始化
        MQUtil.send(merchantExchangeConfig.getExchangeName(),merchantExchangeConfig.getRegisterFundQueue().getRoutingKey(),merchantId,rabbitTemplate);
    }














}
