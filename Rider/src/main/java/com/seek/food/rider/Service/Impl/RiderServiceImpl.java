package com.seek.food.rider.Service.Impl;

import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.MQ.RiderExchangeConfig;
import com.seek.food.config.NacosConfig.Rider.RiderParamsRulesConfig;
import com.seek.food.config.NacosConfig.Rider.RiderRedisKeyConfig;
import com.seek.food.dto.Rider.RiderDTO;
import com.seek.food.rider.Caffeine.RiderCaffeine;
import com.seek.food.rider.Caffeine.RiderPhoneCaffeine;
import com.seek.food.rider.Mapper.RiderMapper;
import com.seek.food.rider.Service.RiderService;
import com.seek.food.util.Context.TokenIdContext;
import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;
import com.seek.food.util.FileUtil.FileSave;
import com.seek.food.util.MQ.MQUtil;
import com.seek.food.util.OPT.OPTUtil;
import com.seek.food.util.Redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

@Service
@Slf4j
@RefreshScope
public class RiderServiceImpl implements RiderService {


    private final CommonParamRulesConfig commonParamRulesConfig;
    private final RiderCaffeine riderCaffeine;
    private final StringRedisTemplate stringRedisTemplate;
    private final RiderRedisKeyConfig riderRedisKeyConfig;
    private final RiderMapper riderMapper;
    private final RiderPhoneCaffeine riderPhoneCaffeine;
    private final RiderParamsRulesConfig riderParamsRulesConfig;
    private final RiderExchangeConfig riderExchangeConfig;
    private final RabbitTemplate rabbitTemplate;

    public RiderServiceImpl(CommonParamRulesConfig commonParamRulesConfig, RiderCaffeine riderCaffeine, StringRedisTemplate stringRedisTemplate
            , RiderRedisKeyConfig riderRedisKeyConfig, RiderMapper riderMapper, RiderPhoneCaffeine riderPhoneCaffeine
            , RiderParamsRulesConfig riderParamsRulesConfig, RiderExchangeConfig riderExchangeConfig, RabbitTemplate rabbitTemplate) {
        this.commonParamRulesConfig = commonParamRulesConfig;
        this.riderCaffeine = riderCaffeine;
        this.stringRedisTemplate = stringRedisTemplate;
        this.riderRedisKeyConfig = riderRedisKeyConfig;
        this.riderMapper = riderMapper;
        this.riderPhoneCaffeine = riderPhoneCaffeine;
        this.riderParamsRulesConfig = riderParamsRulesConfig;
        this.riderExchangeConfig = riderExchangeConfig;
        this.rabbitTemplate = rabbitTemplate;
        //先创建目录，这样后续就不用创建了
        FileSave.createDestDir(riderParamsRulesConfig.getRiderPersonImageDest());
    }

    public RiderDTO getDetail(long riderId){
        //检查id
        commonParamRulesConfig.riderIdCheck(riderId);
        //获取骑手信息
        return riderCaffeine.getAndAutoLoad(riderId,stringRedisTemplate,riderRedisKeyConfig.getRiderMessageCaffeine().getRedisKey(riderId)
                ,riderRedisKeyConfig.getRiderMessageCaffeine().getDuration(),RiderDTO.class,k->riderMapper.getDetail(riderId));
    }

    public RiderDTO getSelf(){
        return getDetail(quickGetRiderId());
    }

    public void updatePersonImage(MultipartFile file){
        //获取id
        long riderId = quickGetRiderId();
        //冷却期校验
        RedisUtil.checkCooldown(stringRedisTemplate,riderRedisKeyConfig.getUpdatePersonImageCooldown().getRedisKey(riderId)
                ,riderRedisKeyConfig.getUpdatePersonImageCooldown().getDuration());
        //获取旧头像路径
        String oldAddr=riderMapper.getPersonImageAddr(riderId);
        //快速保存
        String addr= FileSave.quickCheckAndSaveFile(file,riderParamsRulesConfig.getRiderPersonImageDest(), commonParamRulesConfig.getImageSize()
                , commonParamRulesConfig.getImageType());
        //检查是否成功
        if (!riderMapper.updatePersonImage(addr,oldAddr,riderId)) {
            //发消息使已经保存文件删除
            quickDeleteFile(Paths.get(riderParamsRulesConfig.getRiderPersonImageDest(),addr));
            throw new BizException(ErrorCodeEnum.DATA_NOT_FOUND);
        }
        //删除缓存
        riderCaffeine.deleteAllCaffeine(riderId,stringRedisTemplate,riderRedisKeyConfig.getRiderMessageCaffeine().getRedisKey(riderId));
        //发送消息到mq中删除旧文件
        if (oldAddr!=null&&!oldAddr.isBlank()) quickDeleteFile(Paths.get(riderParamsRulesConfig.getRiderPersonImageDest(),oldAddr));
    }

    public String getUpdatePasswordOpt(){
        //获取id
        long riderId = quickGetRiderId();
        //获取手机号
        String phone=quickGetPhone(riderId);
        //生成验证码
        return OPTUtil.generateOPTAndRecord(stringRedisTemplate,riderRedisKeyConfig.getUpdatePasswordOpt().getRedisKey(phone)
                ,riderRedisKeyConfig.getUpdatePasswordOpt().getDuration(),6);
    }

    public void updatePassword(String password, String opt){
        //获取id
        long riderId = quickGetRiderId();
        //获取手机号
        String phone=quickGetPhone(riderId);
        //检查验证码
        OPTUtil.checkOPT(stringRedisTemplate,riderRedisKeyConfig.getUpdatePasswordOpt().getRedisKey(phone),opt);
        //检查冷却期
        RedisUtil.checkCooldown(stringRedisTemplate,riderRedisKeyConfig.getUpdatePasswordCooldown().getRedisKey(phone)
                , riderRedisKeyConfig.getUpdatePasswordCooldown().getDuration());
        //更新密码
        riderMapper.updatePassword(password,riderId);
    }

    public String getDeleteOpt(){
        //获取id
        long riderId = quickGetRiderId();
        //获取手机号
        String phone=quickGetPhone(riderId);
        //生成验证码
        return OPTUtil.generateOPTAndRecord(stringRedisTemplate,riderRedisKeyConfig.getDeleteRiderOpt().getRedisKey(phone)
                ,riderRedisKeyConfig.getDeleteRiderOpt().getDuration(),6);
    }

    public void delete(String opt){
        //获取id
        long riderId = quickGetRiderId();
        //获取手机号
        String phone=quickGetPhone(riderId);
        //检查验证码
        OPTUtil.checkOPT(stringRedisTemplate,riderRedisKeyConfig.getDeleteRiderOpt().getRedisKey(phone),opt);
        //删除骑手
        quickUpdate(riderId,k->riderMapper.delete(riderId));
        //删除图片
        quickDeleteFile(Paths.get(riderParamsRulesConfig.getRiderPersonImageDest(), riderMapper.getPersonImageAddrAfterDelete(riderId)));
        //发送至MQ删除资金账户
        MQUtil.send(riderExchangeConfig.getExchangeName(),riderExchangeConfig.getDeleteFundQueue().getRoutingKey()
                ,riderId,rabbitTemplate);
    }

    private long quickGetRiderId(){
        return TokenIdContext.getAndCheck(commonParamRulesConfig.getRiderIdStart(),commonParamRulesConfig.getIdCapacity());
    }

    private String quickGetPhone(long riderId){
        return riderPhoneCaffeine.getAndAutoLoad(riderId,stringRedisTemplate,riderRedisKeyConfig.getRiderPhoneCaffeine().getRedisKey(riderId)
                ,riderRedisKeyConfig.getRiderPhoneCaffeine().getDuration(),String.class,k->riderMapper.getPhoneByRiderId(riderId));
    }

    private void quickDeleteFile(Path path){
        MQUtil.send(riderExchangeConfig.getExchangeName(),riderExchangeConfig.getDeleteFileRiderQueue().getRoutingKey()
                ,path.toString(),rabbitTemplate);
    }

    private void quickUpdate(long riderId, Function<Long,Boolean> function){
        riderCaffeine.updateAndRemoveCaffeine(riderId,stringRedisTemplate,riderRedisKeyConfig.getRiderMessageCaffeine().getRedisKey(riderId)
                ,function);
    }
}
