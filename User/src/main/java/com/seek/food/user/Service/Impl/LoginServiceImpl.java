package com.seek.food.user.Service.Impl;

import com.seek.food.config.Data.JWTData;
import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.Common.CommonRedisKeyConfig;
import com.seek.food.config.NacosConfig.Common.JWTConfig;
import com.seek.food.config.NacosConfig.User.UserRedisKeyConfig;
import com.seek.food.util.Context.TokenIdContext;
import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;
import com.seek.food.dto.User.UserDTO;
import com.seek.food.config.NacosConfig.User.UserParamsRulesConfig;
import com.seek.food.user.Mapper.LoginMapper;
import com.seek.food.user.Service.LoginService;
import com.seek.food.util.OPT.OPTUtil;
import com.seek.food.util.JWT.TokenUtil;
import com.seek.food.util.Redis.RedisUtil;
import com.seek.food.util.TimeUtil.DurationUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RefreshScope
@Slf4j
public class LoginServiceImpl implements LoginService {
    private final JWTData JWTUser;
    private final JWTConfig jwtConfig;
    private final CommonRedisKeyConfig commonRedisKeyConfig;
    private final LoginMapper loginMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final CommonParamRulesConfig commonParamRulesConfig;
    private final UserRedisKeyConfig userRedisKeyConfig;

    @Autowired
    public LoginServiceImpl(JWTConfig jwtConfig, LoginMapper loginMapper, UserParamsRulesConfig userParamsRulesConfig
    ,StringRedisTemplate stringRedisTemplate,CommonRedisKeyConfig commonRedisKeyConfig,UserRedisKeyConfig userRedisKeyConfig
    ,CommonParamRulesConfig commonParamRulesConfig) {
        this.jwtConfig = jwtConfig;
        this.JWTUser = jwtConfig.getUser();
        this.commonRedisKeyConfig = commonRedisKeyConfig;
        this.loginMapper = loginMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.commonParamRulesConfig = commonParamRulesConfig;
        this.userRedisKeyConfig = userRedisKeyConfig;
    }

    @Override
    //获取登录所需验证码
    public String loginGetOpt(String phoneNumber){
        log.info("phone number:{} ,尝试获取登录验证码",phoneNumber);
        //验证手机号
        commonParamRulesConfig.phoneNumberCheck(phoneNumber);
        //生成token并且记录于redis
        return OPTUtil.generateOPTAndRecord(stringRedisTemplate,userRedisKeyConfig.getLoginOpt().getName() + phoneNumber
                ,userRedisKeyConfig.getLoginOpt().getDuration(),6);
    }

    @Override
    //手机号与验证码登录
    public UserDTO login(String phoneNumber, String opt, HttpServletResponse response){
        //检验格式
        commonParamRulesConfig.phoneNumberCheck(phoneNumber);
        //检查验证码
        OPTUtil.checkOPT(stringRedisTemplate, userRedisKeyConfig.getLoginOpt().getName() + phoneNumber, opt);
        //根据手机号获取目标
        return loginAndGetToken(loginMapper.getUserByPhoneNumber(phoneNumber),response);
    }

    //通过密码登录
    @Override
    public UserDTO loginByPassword(String phoneNumber, String password, HttpServletResponse response){
        //检验格式
        commonParamRulesConfig.phoneNumberCheck(phoneNumber);
        commonParamRulesConfig.passwordCheck(password);
        //检验冷却时间
        RedisUtil.checkCooldown(stringRedisTemplate,userRedisKeyConfig.getLoginOpt().getName() + phoneNumber
                ,userRedisKeyConfig.getLoginPasswordCooldown().getDuration());
        //验证登录
        return loginAndGetToken(loginMapper.getUserByPassword(phoneNumber, password),response);
    }

    //刷新token用
    @Override
    public void loginRefresh(HttpServletResponse response){
        long userId=TokenIdContext.getAndCheck(commonParamRulesConfig.getUserIdStart(),commonParamRulesConfig.getIdCapacity());
        //检查冷却期，防止频繁刷新token
        RedisUtil.checkCooldown(stringRedisTemplate, userRedisKeyConfig.getLoginRefreshCooldown().getName()+userId
                ,userRedisKeyConfig.getLoginRefreshCooldown().getDuration());
        loginAndGetToken(userId, response);
    }

    //发放登录信息
    private UserDTO loginAndGetToken(UserDTO user, HttpServletResponse response){
        if (user == null) throw new BizException(ErrorCodeEnum.DATA_NOT_FOUND);
        //获取token，并且放在请求头上
        getTokenByUtil(user.getUserId(),  response);
        return user;
    }

    //同样为发放登录消息，不过只需要id
    private void loginAndGetToken(long userId, HttpServletResponse response){
        getTokenByUtil(userId,response);
    }

    //从TokenUtil中快速获取token
    private void getTokenByUtil(long userId, HttpServletResponse response){
        //获取token，并且放在请求头上
        TokenUtil.getAndRecordToken(userId,response, JWTUser.getSecretKey()
                , JWTUser.getTokenDuration()
                , jwtConfig.getRequestTokenName()
                , JWTUser.getHeaderSign()
                , jwtConfig.getHeaderSeparator()
                , commonRedisKeyConfig.getLoginToken().getName()
                , jwtConfig.getMaxStore()
                , stringRedisTemplate);
    }



















}
