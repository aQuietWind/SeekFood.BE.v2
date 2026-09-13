package com.seek.food.rider.Service.Impl;

import com.seek.food.config.Data.JWTData;
import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.Common.CommonRedisKeyConfig;
import com.seek.food.config.NacosConfig.Common.JWTConfig;
import com.seek.food.config.NacosConfig.Rider.RiderRedisKeyConfig;
import com.seek.food.dto.Rider.RiderDTO;
import com.seek.food.rider.Mapper.LoginMapper;
import com.seek.food.rider.Service.LoginService;
import com.seek.food.util.Context.TokenIdContext;
import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;
import com.seek.food.util.JWT.TokenUtil;
import com.seek.food.util.OPT.OPTUtil;
import com.seek.food.util.Redis.RedisUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RefreshScope
@Slf4j
public class LoginServiceImpl implements LoginService {
    private final JWTData JWTRider;
    private final JWTConfig jwtConfig;
    private final LoginMapper loginMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final CommonRedisKeyConfig commonRedisKeyConfig;
    private final CommonParamRulesConfig commonParamRulesConfig;
    private final RiderRedisKeyConfig riderRedisKeyConfig;

    @Autowired
    public LoginServiceImpl(JWTConfig jwtConfig, LoginMapper loginMapper, StringRedisTemplate stringRedisTemplate, CommonRedisKeyConfig commonRedisKeyConfig, CommonParamRulesConfig commonParamRulesConfig, RiderRedisKeyConfig riderRedisKeyConfig) {
        this.jwtConfig = jwtConfig;
        this.JWTRider = jwtConfig.getRider();
        this.loginMapper = loginMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.commonRedisKeyConfig = commonRedisKeyConfig;
        this.commonParamRulesConfig = commonParamRulesConfig;
        this.riderRedisKeyConfig = riderRedisKeyConfig;
    }

    @Override
    //获取登录所需验证码
    public String loginGetOpt(String phoneNumber){
        //验证手机号
        commonParamRulesConfig.phoneNumberCheck(phoneNumber);
        //生成token并且记录于redis
        return OPTUtil.generateOPTAndRecord(stringRedisTemplate,riderRedisKeyConfig.getLoginOpt().getRedisKey(phoneNumber)
                ,riderRedisKeyConfig.getLoginOpt().getDuration(),6);
    }

    @Override
    //手机号与验证码登录
    public RiderDTO login(String phoneNumber, String opt, HttpServletResponse response){
        //检验格式
        commonParamRulesConfig.phoneNumberCheck(phoneNumber);
        //检查验证码
        OPTUtil.checkOPT(stringRedisTemplate, riderRedisKeyConfig.getLoginOpt().getName() + phoneNumber, opt);
        //根据手机号获取目标
        return loginAndGetToken(loginMapper.loginByPhone(phoneNumber),response);
    }

    //通过密码登录
    @Override
    public RiderDTO loginByPassword(String phoneNumber, String password, HttpServletResponse response){
        //检验格式
        commonParamRulesConfig.phoneNumberCheck(phoneNumber);
        commonParamRulesConfig.passwordCheck(password);
        //检验冷却时间
        RedisUtil.checkCooldown(stringRedisTemplate,riderRedisKeyConfig.getLoginOpt().getRedisKey(phoneNumber)
                ,riderRedisKeyConfig.getLoginPasswordCooldown().getDuration());
        //验证登录
        return loginAndGetToken(loginMapper.loginByPassword(phoneNumber, password),response);
    }

    //刷新token用
    @Override
    public void loginRefresh(HttpServletResponse response){
        long riderId=TokenIdContext.getAndCheck(commonParamRulesConfig.getRiderIdStart(),commonParamRulesConfig.getIdCapacity());
        //检查冷却期，防止频繁刷新token
        RedisUtil.checkCooldown(stringRedisTemplate, riderRedisKeyConfig.getLoginRefreshCooldown().getRedisKey(riderId)
                ,riderRedisKeyConfig.getLoginRefreshCooldown().getDuration());
        loginAndGetToken(riderId, response);
    }

    //发放登录信息
    private RiderDTO loginAndGetToken(RiderDTO rider, HttpServletResponse response){
        if (rider == null) throw new BizException(ErrorCodeEnum.DATA_NOT_FOUND);
        //获取token，并且放在请求头上
        getTokenByUtil(rider.getRiderId(),  response);
        return rider;
    }

    //同样为发放登录消息，不过只需要id
    private void loginAndGetToken(long rider, HttpServletResponse response){
        getTokenByUtil(rider,response);
    }

    //从TokenUtil中快速获取token
    private void getTokenByUtil(long riderId, HttpServletResponse response){
        //获取token，并且放在请求头上
        TokenUtil.getAndRecordToken(riderId,response, JWTRider.getSecretKey()
                , JWTRider.getTokenDuration()
                , jwtConfig.getRequestTokenName()
                , JWTRider.getHeaderSign()
                , jwtConfig.getHeaderSeparator()
                , commonRedisKeyConfig.getLoginToken().getName()
                , jwtConfig.getMaxStore()
                , stringRedisTemplate);
    }



















}
