package com.seek.food.merchant.Service.Impl;

import com.seek.food.config.Data.JWTData;
import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.Common.CommonRedisKeyConfig;
import com.seek.food.config.NacosConfig.Common.JWTConfig;
import com.seek.food.config.NacosConfig.Merchant.MerchantRedisKeyConfig;
import com.seek.food.dto.Merchant.MerchantDTO;
import com.seek.food.merchant.Mapper.LoginMapper;
import com.seek.food.merchant.Service.LoginService;
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
    private final CommonParamRulesConfig commonParamRulesConfig;
    private final StringRedisTemplate stringRedisTemplate;
    private final MerchantRedisKeyConfig merchantRedisKeyConfig;
    private final LoginMapper loginMapper;
    private final JWTConfig jwtConfig;
    private final CommonRedisKeyConfig commonRedisKeyConfig;
    private final JWTData jwtMerchant;
    @Autowired
    public LoginServiceImpl(CommonParamRulesConfig commonParamRulesConfig,StringRedisTemplate stringRedisTemplate
    ,MerchantRedisKeyConfig merchantRedisKeyConfig,LoginMapper loginMapper,JWTConfig jwtConfig,CommonRedisKeyConfig commonRedisKeyConfig) {
        this.commonParamRulesConfig = commonParamRulesConfig;
        this.stringRedisTemplate = stringRedisTemplate;
        this.merchantRedisKeyConfig = merchantRedisKeyConfig;
        this.loginMapper = loginMapper;
        this.jwtConfig = jwtConfig;
        this.commonRedisKeyConfig = commonRedisKeyConfig;
        this.jwtMerchant = jwtConfig.getMerchant();
    }


    @Override
    public String getLoginOpt(String phoneNumber){
        commonParamRulesConfig.phoneNumberCheck(phoneNumber);
        return OPTUtil.generateOPTAndRecord(stringRedisTemplate,merchantRedisKeyConfig.getMerchantLoginOpt().getName()+phoneNumber,
                merchantRedisKeyConfig.getMerchantLoginOpt().getDuration(),6);
    }

    @Override
    public MerchantDTO login(String phoneNumber, String opt, HttpServletResponse httpServletResponse){
        commonParamRulesConfig.phoneNumberCheck(phoneNumber);
        OPTUtil.checkOPT(stringRedisTemplate,merchantRedisKeyConfig.getMerchantLoginOpt().getName()+phoneNumber,opt);
        MerchantDTO merchant;
        //检查是否有数据
        if ((merchant=loginMapper.login(phoneNumber))==null)throw new BizException(ErrorCodeEnum.DATA_NOT_FOUND);
        return loginAndGetToken(merchant,httpServletResponse);
    }

    @Override
    public MerchantDTO loginByPassword(String phoneNumber, String password,HttpServletResponse httpServletResponse){
        commonParamRulesConfig.phoneNumberCheck(phoneNumber);
        commonParamRulesConfig.passwordCheck(password);
        //检查冷却
        RedisUtil.checkCooldown(stringRedisTemplate,merchantRedisKeyConfig.getMerchantLoginPasswordCooldown().getName(),
                merchantRedisKeyConfig.getMerchantLoginPasswordCooldown().getDuration());
        MerchantDTO merchant;
        //检查是否有数据
        if ((merchant=loginMapper.loginByPassword(phoneNumber,password))==null)throw new BizException(ErrorCodeEnum.DATA_NOT_FOUND);
        return loginAndGetToken(merchant,httpServletResponse);
    }

    //刷新token用
    @Override
    public void loginRefresh(HttpServletResponse response){
        long merchantId= TokenIdContext.getAndCheck(commonParamRulesConfig.getMerchantIdStart(),commonParamRulesConfig.getIdCapacity());
        //检查冷却期，防止频繁刷新token
        RedisUtil.checkCooldown(stringRedisTemplate, merchantRedisKeyConfig.getMerchantLoginRefreshCooldown().getName()+merchantId
                ,merchantRedisKeyConfig.getMerchantLoginRefreshCooldown().getDuration());
        loginAndGetToken(TokenIdContext.getAndToLong(), response);
    }

    //发放登录信息
    private MerchantDTO loginAndGetToken(MerchantDTO merchant, HttpServletResponse response){
        if (merchant == null) throw new BizException(ErrorCodeEnum.DATA_NOT_FOUND);
        //获取token，并且放在请求头上
        getTokenByUtil(merchant.getMerchantId(),  response);
        return merchant;
    }

    //同样为发放登录消息，不过只需要id
    private void loginAndGetToken(long merchantId, HttpServletResponse response){
        getTokenByUtil(merchantId,response);
    }

    //从TokenUtil中快速获取token
    private void getTokenByUtil(long merchantId, HttpServletResponse response){
        //获取token，并且放在请求头上
        TokenUtil.getAndRecordToken(merchantId,response, jwtMerchant.getSecretKey()
                , jwtMerchant.getTokenDuration()
                , jwtConfig.getRequestTokenName()
                , jwtMerchant.getHeaderSign()
                , jwtConfig.getHeaderSeparator()
                , commonRedisKeyConfig.getLoginToken().getName()
                , jwtConfig.getMaxStore()
                , stringRedisTemplate);
    }






}
