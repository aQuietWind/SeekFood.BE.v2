package com.seek.food.user.Service.Impl;

import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.Common.CommonRedisKeyConfig;
import com.seek.food.config.NacosConfig.MQ.UserExchangeConfig;
import com.seek.food.config.NacosConfig.User.UserParamsRulesConfig;
import com.seek.food.config.NacosConfig.User.UserRedisKeyConfig;
import com.seek.food.dto.User.UserDTO;
import com.seek.food.user.Caffeine.PhoneCaffeine;
import com.seek.food.user.Caffeine.UserCaffeine;
import com.seek.food.user.Mapper.UserMapper;
import com.seek.food.user.Service.UserService;
import com.seek.food.util.Context.TokenIdContext;
import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;
import com.seek.food.util.FileUtil.FileSave;
import com.seek.food.util.MQ.MQUtil;
import com.seek.food.util.OPT.OPTUtil;
import com.seek.food.util.Redis.RedisUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@RefreshScope
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserCaffeine userCaffeine;
    private final StringRedisTemplate stringRedisTemplate;
    private final UserParamsRulesConfig userParamsRulesConfig;
    private final RabbitTemplate rabbitTemplate;
    private final UserExchangeConfig userExchangeConfig;
    private final PhoneCaffeine phoneCaffeine;
    private final CommonRedisKeyConfig commonRedisKeyConfig;
    private final CommonParamRulesConfig commonParamRulesConfig;
    private final UserRedisKeyConfig userRedisKeyConfig;
    @Autowired
    public UserServiceImpl(UserMapper userMapper,UserCaffeine userCaffeine,StringRedisTemplate stringRedisTemplate
    ,UserParamsRulesConfig userParamsRulesConfig,RabbitTemplate rabbitTemplate,UserExchangeConfig userExchangeConfig
    ,PhoneCaffeine phoneCaffeine,CommonRedisKeyConfig commonRedisKeyConfig,CommonParamRulesConfig commonParamRulesConfig
    ,UserRedisKeyConfig userRedisKeyConfig) {
        this.userMapper = userMapper;
        this.userCaffeine = userCaffeine;
        this.stringRedisTemplate = stringRedisTemplate;
        this.userParamsRulesConfig = userParamsRulesConfig;
        this.rabbitTemplate = rabbitTemplate;
        this.userExchangeConfig = userExchangeConfig;
        this.phoneCaffeine = phoneCaffeine;
        this.commonRedisKeyConfig = commonRedisKeyConfig;
        this.commonParamRulesConfig = commonParamRulesConfig;
        this.userRedisKeyConfig = userRedisKeyConfig;
    }
    // Bean 注入完成后再执行初始化
    @PostConstruct
    public void initPath() {
        //提前创建目录，后面头像保存无需再校验
        FileSave.createDestDir(userParamsRulesConfig.getHeaderImageDest());
    }


    //获取某一用户详细信息
    @Override
    public UserDTO getUserDetailMessage(long userId){
        //验证id是否属于userId而不是别的
        commonParamRulesConfig.userIdCheck(userId);
        //从缓存中取出结果并且返回
        return userCaffeine.getAndAutoLoad(userId,stringRedisTemplate,userRedisKeyConfig.getCaffeineMessage().getName()+userId
        ,userRedisKeyConfig.getCaffeineMessage().getDuration(),UserDTO.class,key->userMapper.getUserDetailMessage(userId));
    }


    //获取用户个人信息
    @Override
    public  UserDTO getUserSelfMessage(){
        long userId= TokenIdContext.getAndCheck(commonParamRulesConfig.getUserIdStart(),commonParamRulesConfig.getIdCapacity());
        //直接返回mysql最新数据,避免用户自身的一致性问题
        return userMapper.getUserDetailMessage(userId);
    }


    //获取更改密码所需的验证码
    @Override
    public String updateUserPasswordGetOpt(String phoneNumber){
        //验证格式
        commonParamRulesConfig.phoneNumberCheck(phoneNumber);
        //获取验证码
        return OPTUtil.generateOPTAndRecord(stringRedisTemplate,userRedisKeyConfig.getUpdatePasswordOpt().getName()+phoneNumber
                ,userRedisKeyConfig.getUpdatePasswordOpt().getDuration(),6);
    }


    //更改密码
    @Override
    public void updateUserPassword(String phoneNumber, String newPassword,String opt){
        //验证格式
        commonParamRulesConfig.phoneNumberCheck(phoneNumber);
        commonParamRulesConfig.passwordCheck(newPassword);
        //校验冷却期
        RedisUtil.checkCooldown(stringRedisTemplate,userRedisKeyConfig.getUpdatePasswordCooldown().getName()+phoneNumber,
                userRedisKeyConfig.getUpdatePasswordCooldown().getDuration());
        //检查验证码
        OPTUtil.checkOPT(stringRedisTemplate,userRedisKeyConfig.getUpdatePasswordOpt().getName()+phoneNumber,opt);
        //如果mysql无目标数据会返回false
        if (!userMapper.updateUserPassword(phoneNumber,newPassword))throw new BizException(ErrorCodeEnum.DATA_NOT_FOUND);
    }


    //更改头像
    @Override
    public void updateUserHeader(MultipartFile file){
        //获取id
        long userId=TokenIdContext.getAndCheck(commonParamRulesConfig.getUserIdStart(),commonParamRulesConfig.getIdCapacity());
        //冷却期校验
        RedisUtil.checkCooldown(stringRedisTemplate,userRedisKeyConfig.getUpdateHeaderImageCooldown().getName()+userId
                ,userRedisKeyConfig.getUpdateHeaderImageCooldown().getDuration());
        //获取旧头像路径
        String oldAddr=userMapper.getHeaderPath(userId);
        //快速保存
        String addr=FileSave.quickCheckAndSaveFile(file,userParamsRulesConfig.getHeaderImageDest(), commonParamRulesConfig.getImageSize()
                , commonParamRulesConfig.getImageType());
        //检查是否成功
        if (!userMapper.updateUserHeader(userId,addr,oldAddr)) {
            //发消息使已经保存文件删除
            MQUtil.send(userExchangeConfig.getExchangeName(),userExchangeConfig.getDeleteFileUserQueue().getRoutingKey()
                    , Paths.get(userParamsRulesConfig.getHeaderImageDest(),addr).toString(),rabbitTemplate);
            throw new BizException(ErrorCodeEnum.DATA_NOT_FOUND);
        }
        //发送消息到mq中删除旧文件
        if (oldAddr!=null&&!oldAddr.isBlank()) MQUtil.send(userExchangeConfig.getExchangeName(),userExchangeConfig.getDeleteFileUserQueue().getRoutingKey()
                , Paths.get(userParamsRulesConfig.getHeaderImageDest(),oldAddr).toString(),rabbitTemplate);
    }


    //更改用户自身信息
    @Override
    public void updateUserMessage(UserDTO userDTO){
        //获取Id并检验
        long userId=TokenIdContext.getAndCheck(commonParamRulesConfig.getUserIdStart(),commonParamRulesConfig.getIdCapacity());
        //检验性别，姓名，生日时期（出生日期）
        commonParamRulesConfig.sexCheck(userDTO.getSex());
        commonParamRulesConfig.birthdayCheck(userDTO.getBirthday());
        userParamsRulesConfig.usernameCheck(userDTO.getUsername());
        //检查冷却
        RedisUtil.checkCooldown(stringRedisTemplate,userRedisKeyConfig.getUpdateMessageCooldown().getName()+userId
                ,userRedisKeyConfig.getUpdateMessageCooldown().getDuration());
        userDTO.setUserId(userId);
        if(!userMapper.updateUserMessage(userDTO))throw new BizException(ErrorCodeEnum.DATA_NOT_FOUND);
    }


    //多用户粗览信息获取
    @Override
    public List<UserDTO> getUsersSimpleMessage(List<Long> userIds){
        if (userIds.isEmpty())return new ArrayList<>();
        //校验参数
        for (Long userId:userIds) commonParamRulesConfig.userIdCheck(userId);
        return userMapper.getUsersSimpleMessage(userIds);
    }


    //删除所需获取验证码
    @Override
    public String getUserDeleteOpt(){
        //获取userId并且校验
        long userId=TokenIdContext.getAndCheck(commonParamRulesConfig.getUserIdStart(),commonParamRulesConfig.getIdCapacity());
        //获取手机号,只做业务的手机号获取模拟，无实际用途
        String phoneNumber=phoneCaffeine.getAndAutoLoad(userId,stringRedisTemplate,RedisUtil.redisKeyMix(userRedisKeyConfig.getCaffeinePhone().getName(),userId)
        ,userRedisKeyConfig.getCaffeinePhone().getDuration(),String.class, userMapper::getPhoneNumber);
        //产生验证码
        return OPTUtil.generateOPTAndRecord(stringRedisTemplate,RedisUtil.redisKeyMix(userRedisKeyConfig.getDeleteUserOpt().getName(),userId),
                userRedisKeyConfig.getDeleteUserOpt().getDuration(),6);
    }


    //删除用户
    @Override
    public void deleteUser(String opt){
        //userId获取
        long userId=TokenIdContext.getAndCheck(commonParamRulesConfig.getUserIdStart(),commonParamRulesConfig.getIdCapacity());
        //检验验证码
        OPTUtil.checkOPT(stringRedisTemplate,RedisUtil.redisKeyMix(userRedisKeyConfig.getDeleteUserOpt().getName(),userId),opt);
        //逻辑删除用户
        if (!userMapper.deleteUser(userId))throw new BizException(ErrorCodeEnum.DATA_NOT_FOUND);
        //清空token
        stringRedisTemplate.delete(commonRedisKeyConfig.getLoginToken().getRedisKey(userId));
        //删除旧照片
        MQUtil.send(userExchangeConfig.getExchangeName(),userExchangeConfig.getDeleteFundQueue().getRoutingKey()
        ,Paths.get(userParamsRulesConfig.getHeaderImageDest(),userMapper.getDeleteHeaderPath(userId)).toString(),rabbitTemplate);
        //传递消息队列进行其他模块后续操作
        MQUtil.send(userExchangeConfig.getExchangeName(),userExchangeConfig.getDeleteFundQueue().getRoutingKey(),userId,rabbitTemplate);
    }



















}
