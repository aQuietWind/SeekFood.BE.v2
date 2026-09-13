package com.seek.food.admin.Service.Impl;

import com.seek.food.admin.Service.LoginService;
import com.seek.food.config.Data.JWTData;
import com.seek.food.config.NacosConfig.Admin.AdminParamsRulesConfig;
import com.seek.food.config.NacosConfig.Admin.AdminRedisKeyConfig;
import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.Common.CommonRedisKeyConfig;
import com.seek.food.config.NacosConfig.Common.JWTConfig;
import com.seek.food.util.JWT.TokenUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@RefreshScope
@Slf4j
public class LoginServiceImpl implements LoginService {


    private final JWTData JWTAdmin;
    private final JWTConfig jwtConfig;
    private final CommonRedisKeyConfig commonRedisKeyConfig;
    private final StringRedisTemplate stringRedisTemplate;
    private final AdminParamsRulesConfig adminParamsRulesConfig;
    private final CommonParamRulesConfig commonParamRulesConfig;
    private final AdminRedisKeyConfig adminRedisKeyConfig;

    @Autowired
    public LoginServiceImpl(JWTConfig jwtConfig, StringRedisTemplate stringRedisTemplate, CommonRedisKeyConfig commonRedisKeyConfig
            , AdminParamsRulesConfig adminParamsRulesConfig, CommonParamRulesConfig commonParamRulesConfig, AdminRedisKeyConfig adminRedisKeyConfig) {
        this.jwtConfig = jwtConfig;
        this.JWTAdmin = jwtConfig.getAdmin();
        this.commonRedisKeyConfig = commonRedisKeyConfig;
        this.stringRedisTemplate = stringRedisTemplate;
        this.adminParamsRulesConfig = adminParamsRulesConfig;
        this.commonParamRulesConfig = commonParamRulesConfig;
        this.adminRedisKeyConfig = adminRedisKeyConfig;
    }
    @PostConstruct
    public void init() {
        //初始化计数器，日后可能大幅度扩增和改造该admin模块
        stringRedisTemplate.opsForValue().setIfAbsent(adminRedisKeyConfig.getAdminIdCount().getName()
                ,""+commonParamRulesConfig.getAdminIdStart()*commonParamRulesConfig.getIdCapacity());
    }

    @Override
    public void login(String name,String password,HttpServletResponse response){
        adminParamsRulesConfig.adminLoginCheck(name,password);
        getTokenByUtil(response);
    }

    //从TokenUtil中快速获取token
    private void getTokenByUtil(HttpServletResponse response){
        //获取token，并且放在请求头上
        TokenUtil.getAndRecordToken(commonParamRulesConfig.getAdminIdStart()*commonParamRulesConfig.getIdCapacity()
                ,response
                , JWTAdmin.getSecretKey()
                , JWTAdmin.getTokenDuration()
                , jwtConfig.getRequestTokenName()
                , JWTAdmin.getHeaderSign()
                , jwtConfig.getHeaderSeparator()
                , commonRedisKeyConfig.getLoginToken().getName()
                , jwtConfig.getMaxStore()
                , stringRedisTemplate);
    }
}
