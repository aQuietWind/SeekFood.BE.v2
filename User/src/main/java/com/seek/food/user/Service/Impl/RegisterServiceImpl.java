package com.seek.food.user.Service.Impl;

import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.MQ.UserExchangeConfig;
import com.seek.food.config.NacosConfig.User.UserRedisKeyConfig;
import com.seek.food.util.MQ.MQUtil;
import com.seek.food.config.NacosConfig.User.UserParamsRulesConfig;
import com.seek.food.user.Mapper.RegisterMapper;
import com.seek.food.user.Service.RegisterService;
import com.seek.food.util.CommonUtil.IdUtil;
import com.seek.food.util.OPT.OPTUtil;
import com.seek.food.util.Redis.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


@Service
@RefreshScope
public class RegisterServiceImpl implements RegisterService {
    private final UserParamsRulesConfig userParamsRulesConfig;
    private final StringRedisTemplate stringRedisTemplate;
    private final RegisterMapper registerMapper;
    private final UserExchangeConfig userExchangeConfig;
    private final RabbitTemplate rabbitTemplate;
    private final CommonParamRulesConfig commonParamRulesConfig;
    private final UserRedisKeyConfig userRedisKeyConfig;
    private static final Logger logger = LoggerFactory.getLogger(RegisterServiceImpl.class);
    @Autowired
    public RegisterServiceImpl(UserParamsRulesConfig userParamsRulesConfig, StringRedisTemplate stringRedisTemplate
    , RegisterMapper registerMapper , UserExchangeConfig userExchangeConfig, RabbitTemplate rabbitTemplate
    ,CommonParamRulesConfig commonParamRulesConfig,UserRedisKeyConfig userRedisKeyConfig) {
        this.userParamsRulesConfig = userParamsRulesConfig;
        this.stringRedisTemplate = stringRedisTemplate;
        this.registerMapper = registerMapper;
        this.userExchangeConfig = userExchangeConfig;
        this.rabbitTemplate = rabbitTemplate;
        this.commonParamRulesConfig = commonParamRulesConfig;
        this.userRedisKeyConfig = userRedisKeyConfig;
        //初始化userIdCount
        stringRedisTemplate.opsForValue().setIfAbsent(userRedisKeyConfig.getUserIdCount().getName(),""+commonParamRulesConfig.getIdCapacity());
    }


    //获取注册所需的验证码
    @Override
    public String registerGetOpt(String phoneNumber) {
        logger.info("phone number:{} ,进行注册获取验证码",phoneNumber);
        //验证手机号
        commonParamRulesConfig.phoneNumberCheck(phoneNumber);
        return OPTUtil.generateOPTAndRecord(stringRedisTemplate,userRedisKeyConfig.getRegisterOpt().getName() + phoneNumber
                ,userRedisKeyConfig.getRegisterOpt().getDuration(),6);
    }

    //注册用户
    @Override
    public void registerUser(String phoneNumber, String password, String opt) {
        //检验格式
        commonParamRulesConfig.phoneNumberCheck(phoneNumber);
        commonParamRulesConfig.passwordCheck(password);
        //检验验证码
        OPTUtil.checkOPT(stringRedisTemplate,userRedisKeyConfig.getRegisterOpt().getName() + phoneNumber,opt);
        //校验冷却期
        RedisUtil.checkCooldown(stringRedisTemplate,userRedisKeyConfig.getRegisterCooldown().getName()+phoneNumber,
                userRedisKeyConfig.getRegisterCooldown().getDuration());
        //生成id
        long userId=IdUtil.IdGenerateByIncrease(userRedisKeyConfig.getUserIdCount().getName(),stringRedisTemplate);
        //写入mysql,失败会报错
        registerMapper.insertUser(userId, phoneNumber, password);
        MQUtil.send(userExchangeConfig.getExchangeName(),userExchangeConfig.getRegisterFundQueue().getRoutingKey(),userId,rabbitTemplate);
        logger.info("phone number:{} ,成功注册用户",phoneNumber);
    }








}
