package com.seek.food.merchant.Service.Impl;

import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.Common.CommonRedisKeyConfig;
import com.seek.food.config.NacosConfig.MQ.MerchantExchangeConfig;
import com.seek.food.config.NacosConfig.Merchant.*;
import com.seek.food.dto.Common.SimplePoint;
import com.seek.food.dto.Merchant.MerchantDTO;
import com.seek.food.merchant.Caffeine.MerchantCaffeine;
import com.seek.food.merchant.Caffeine.PhoneCaffeine;
import com.seek.food.merchant.Mapper.MerchantMapper;
import com.seek.food.merchant.Service.MerchantService;
import com.seek.food.merchant.Util.MerchantUtil;
import com.seek.food.util.Context.TokenIdContext;
import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;
import com.seek.food.util.FileUtil.FileSave;
import com.seek.food.util.MQ.MQUtil;
import com.seek.food.util.OPT.OPTUtil;
import com.seek.food.util.Redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.distance.DistanceUtils;
import org.locationtech.spatial4j.shape.Point;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;

@Service
@RefreshScope
@Slf4j
public class MerchantServiceImpl implements MerchantService {
    private final MerchantMapper merchantMapper;
    private final MerchantRedisKeyConfig merchantRedisKeyConfig;
    private final MerchantParamsRulesConfig merchantParamsRulesConfig;
    private final CommonParamRulesConfig commonParamRulesConfig;
    private final MerchantCaffeine merchantCaffeine;
    private final PhoneCaffeine phoneCaffeine;
    private final StringRedisTemplate stringRedisTemplate;
    private final MerchantExchangeConfig merchantExchangeConfig;
    private final RabbitTemplate rabbitTemplate;
    private final CommonRedisKeyConfig commonRedisKeyConfig;
    private final MerchantUtil merchantUtil;
    // 地球模型
    private static final SpatialContext ctx = SpatialContext.GEO;

    @Autowired
    public MerchantServiceImpl(MerchantMapper merchantMapper, MerchantRedisKeyConfig merchantRedisKeyConfig
    , CommonParamRulesConfig commonParamRulesConfig, MerchantCaffeine merchantCaffeine, StringRedisTemplate stringRedisTemplate
    , MerchantParamsRulesConfig merchantParamsRulesConfig, MerchantExchangeConfig merchantExchangeConfig
    , RabbitTemplate rabbitTemplate, PhoneCaffeine phoneCaffeine, CommonRedisKeyConfig commonRedisKeyConfig, MerchantUtil merchantUtil) {
        this.merchantMapper = merchantMapper;
        this.merchantRedisKeyConfig = merchantRedisKeyConfig;
        this.merchantParamsRulesConfig = merchantParamsRulesConfig;
        this.commonParamRulesConfig = commonParamRulesConfig;
        this.merchantCaffeine = merchantCaffeine;
        this.stringRedisTemplate = stringRedisTemplate;
        this.rabbitTemplate = rabbitTemplate;
        this.phoneCaffeine = phoneCaffeine;
        this.merchantUtil = merchantUtil;
        //提前创建目录
        FileSave.createDestDir(merchantParamsRulesConfig.getMasterImageDest());
        FileSave.createDestDir(merchantParamsRulesConfig.getProofImageDest());
        FileSave.createDestDir(merchantParamsRulesConfig.getShowImageDest());
        FileSave.createDestDir(merchantParamsRulesConfig.getHomeImageDest());
        this.merchantExchangeConfig = merchantExchangeConfig;
        this.commonRedisKeyConfig = commonRedisKeyConfig;
    }

    //获取商家详细信息
    @Override
    public MerchantDTO getMerchantDetail(long merchantId){
        commonParamRulesConfig.merchantIdCheck(merchantId);
        return merchantCaffeine.getAndAutoLoad(merchantId,stringRedisTemplate,merchantRedisKeyConfig.getMerchantMessageCaffeine().getName()+merchantId,
                merchantRedisKeyConfig.getMerchantMessageCaffeine().getDuration(),MerchantDTO.class,k->merchantMapper.getMerchantById(merchantId));
    }

    //获取商家自身详细信息
    @Override
    public MerchantDTO getMerchantSelf(){
        long merchantId= TokenIdContext.getAndCheck(commonParamRulesConfig.getMerchantIdStart(),commonParamRulesConfig.getIdCapacity());
        return getMerchantDetail(merchantId);
    }

    //设置店主信息
    @Override
    public void setMerchantMaster(String masterName, String masterCode, MultipartFile masterImage){
        //获取商家id
        long merchantId=TokenIdContext.getAndCheck(commonParamRulesConfig.getMerchantIdStart(),commonParamRulesConfig.getIdCapacity());
        //检查是否还未设置过该信息
        if (RedisUtil.oftenGetBit(stringRedisTemplate,merchantRedisKeyConfig.getMerchantMasterIsSet().getName(),merchantId,
                commonParamRulesConfig.getIdCapacity(),commonParamRulesConfig.getIdBitmapAreaNumber()))throw new BizException(ErrorCodeEnum.CONDITION_NOT_PASS);
        //检查格式
        commonParamRulesConfig.personNameCheck(masterName);
        commonParamRulesConfig.codeCheck(masterCode);
        //检查冷却期，同时一定程度上作为伪分布式锁防止多重设置
        RedisUtil.checkCooldown(stringRedisTemplate,merchantRedisKeyConfig.getMerchantUpdateMasterCooldown().getName()+merchantId,
                merchantRedisKeyConfig.getMerchantUpdateMasterCooldown().getDuration());
        //检查照片并且保存
        String addr=FileSave.quickCheckAndSaveFile(masterImage,merchantParamsRulesConfig.getMasterImageDest()
                ,commonParamRulesConfig.getImageSize(),commonParamRulesConfig.getImageType());
        //写入mysql,并且清除缓存
        merchantCaffeine.updateAndRemoveCaffeine(merchantId,stringRedisTemplate,merchantRedisKeyConfig.getMerchantMessageCaffeine().getName()+merchantId
                , k->merchantMapper.setMerchantMaster(merchantId,masterName,masterCode,addr));
        //设置该信息已经被修改
        RedisUtil.oftenSetBit(stringRedisTemplate,merchantRedisKeyConfig.getMerchantMasterIsSet().getName(),merchantId
                ,true,commonParamRulesConfig.getIdCapacity(),commonParamRulesConfig.getIdBitmapAreaNumber());
    }

    //添加营业证明照片
    @Override
    @Transactional
    public void addMerchantProofImage(MultipartFile image){
        //获取id
        long merchantId=TokenIdContext.getAndCheck(commonParamRulesConfig.getMerchantIdStart(),commonParamRulesConfig.getIdCapacity());
        //检查冷却
        RedisUtil.checkCooldown(stringRedisTemplate, merchantRedisKeyConfig.getMerchantAddProofCooldown().getRedisKey(merchantId)
                ,merchantRedisKeyConfig.getMerchantAddProofCooldown().getDuration());
        //检查文件，并且保存
        String addr=FileSave.quickCheckAndSaveFile(image,merchantParamsRulesConfig.getProofImageDest(),commonParamRulesConfig.getImageSize(),commonParamRulesConfig.getImageType());
        //写入MySQL，并检查是否成功
        if (!merchantMapper.addMerchantProofImage(merchantId,addr,merchantParamsRulesConfig.getProofImageNumberMax())) {
            //发消息使已经保存文件删除
            quickToMQDeleteFile(merchantParamsRulesConfig.getProofImageDest(),addr);
            throw new BizException(ErrorCodeEnum.CONDITION_NOT_PASS);
        }
        //清除缓存
        quickDeleteCaffeine(merchantId);
    }

    //删除营业证明照片
    @Override
    public void removeMerchantProofImage(int index){
        long merchantId=TokenIdContext.getAndCheck(commonParamRulesConfig.getMerchantIdStart(),commonParamRulesConfig.getIdCapacity());
        //检查冷却
        RedisUtil.checkCooldown(stringRedisTemplate, merchantRedisKeyConfig.getMerchantRemoveProofCooldown().getName()+merchantId,merchantRedisKeyConfig.getMerchantAddProofCooldown().getDuration());
        //回显获取信息
        String oldAddr=quickGetProofOldAddr(merchantId,index);
        //mysql删除该地址,防止可能出现的并发问题，虽然加了冷却期，但是仍旧提防
        if(!merchantMapper.removeMerchantProofImage(merchantId,oldAddr,index))throw new BizException(ErrorCodeEnum.DATA_NOT_FOUND);
        //发消息使目标文件删除
        quickToMQDeleteFile(merchantParamsRulesConfig.getProofImageDest(),oldAddr);
        //清除缓存
        quickDeleteCaffeine(merchantId);
    }

    //更换营业证明照片
    @Override
    public void replaceMerchantProofImage(MultipartFile image,int index){
        long merchantId=TokenIdContext.getAndCheck(commonParamRulesConfig.getMerchantIdStart(),commonParamRulesConfig.getIdCapacity());
        //检查冷却
        RedisUtil.checkCooldown(stringRedisTemplate, merchantRedisKeyConfig.getMerchantReplaceProofCooldown().getRedisKey(merchantId)
                ,merchantRedisKeyConfig.getMerchantReplaceShowCooldown().getDuration());
        //回显获取信息
        String oldAddr=quickGetProofOldAddr(merchantId,index);
        //检查文件，并且保存
        String addr=FileSave.quickCheckAndSaveFile(image,merchantParamsRulesConfig.getProofImageDest(),commonParamRulesConfig.getImageSize(),commonParamRulesConfig.getImageType());
        //mysql更新该地址,防止可能出现的并发问题，虽然加了冷却期，但是仍旧提防
        if(!merchantMapper.replaceMerchantProofImage(merchantId,addr,oldAddr,index)) {
            //发消息使已经刚刚保存的文件删除
            quickToMQDeleteFile(merchantParamsRulesConfig.getProofImageDest(),addr);
            throw new BizException(ErrorCodeEnum.DATA_NOT_FOUND);
        }
        //发消息使已经旧文件删除
        quickToMQDeleteFile(merchantParamsRulesConfig.getProofImageDest(),oldAddr);
        //清除缓存
        quickDeleteCaffeine(merchantId);
    }

    //添加商家展示照片
    @Override
    public void addShowImage(MultipartFile image){
        //获取id
        long merchantId=TokenIdContext.getAndCheck(commonParamRulesConfig.getMerchantIdStart(),commonParamRulesConfig.getIdCapacity());
        //检查冷却
        RedisUtil.checkCooldown(stringRedisTemplate, merchantRedisKeyConfig.getMerchantAddShowCooldown().getRedisKey(merchantId)
                ,merchantRedisKeyConfig.getMerchantAddShowCooldown().getDuration());
        //检查文件，并且保存
        String addr=FileSave.quickCheckAndSaveFile(image,merchantParamsRulesConfig.getShowImageDest()
                ,commonParamRulesConfig.getImageSize(),commonParamRulesConfig.getImageType());
        //写入MySQL，并检查是否成功
        if (!merchantMapper.addShowImage(merchantId,addr,merchantParamsRulesConfig.getShowImageNumberMax())) {
            //发消息使已经保存文件删除
            quickToMQDeleteFile(merchantParamsRulesConfig.getShowImageDest(),addr);
            throw new BizException(ErrorCodeEnum.CONDITION_NOT_PASS);
        }
        //清除缓存
        quickDeleteCaffeine(merchantId);
    }

    //删除商家展示照片
    @Override
    public void removeShowImage(int index){
        long merchantId=TokenIdContext.getAndCheck(commonParamRulesConfig.getMerchantIdStart(),commonParamRulesConfig.getIdCapacity());
        //检查冷却
        RedisUtil.checkCooldown(stringRedisTemplate, merchantRedisKeyConfig.getMerchantRemoveShowCooldown().getRedisKey(merchantId)
                ,merchantRedisKeyConfig.getMerchantRemoveShowCooldown().getDuration());
        //回显获取信息
        String oldAddr=quickGetShowOldAddr(merchantId,index);
        //mysql删除该地址,防止可能出现的并发问题，虽然加了冷却期，但是仍旧提防
        if(!merchantMapper.removeShowImage(merchantId,oldAddr,index))throw new BizException(ErrorCodeEnum.DATA_NOT_FOUND);
        //发消息使目标文件删除
        quickToMQDeleteFile(merchantParamsRulesConfig.getShowImageDest(),oldAddr);
        //清除缓存
        quickDeleteCaffeine(merchantId);
    }

    //更换商家展示照片
    @Override
    public void replaceShowImage(MultipartFile image,int index){
        long merchantId=TokenIdContext.getAndCheck(commonParamRulesConfig.getMerchantIdStart(),commonParamRulesConfig.getIdCapacity());
        //检查冷却
        RedisUtil.checkCooldown(stringRedisTemplate, merchantRedisKeyConfig.getMerchantReplaceShowCooldown().getRedisKey(merchantId)
                ,merchantRedisKeyConfig.getMerchantReplaceShowCooldown().getDuration());
        //回显获取信息
        String oldAddr=quickGetShowOldAddr(merchantId,index);
        //检查文件，并且保存
        String addr=FileSave.quickCheckAndSaveFile(image,merchantParamsRulesConfig.getShowImageDest()
                ,commonParamRulesConfig.getImageSize(),commonParamRulesConfig.getImageType());
        //mysql更新该地址,防止可能出现的并发问题，虽然加了冷却期，但是仍旧提防
        if(!merchantMapper.replaceShowImage(merchantId,addr,oldAddr,index)) {
            //发消息使刚刚保存的文件删除
            quickToMQDeleteFile(merchantParamsRulesConfig.getShowImageDest(),addr);
            throw new BizException(ErrorCodeEnum.DATA_NOT_FOUND);
        }
        //发消息使老文件删除
        quickToMQDeleteFile(merchantParamsRulesConfig.getShowImageDest(),oldAddr);
        //清除缓存
        quickDeleteCaffeine(merchantId);
    }

    //更新商家的封面图片
    @Override
    public void updateHomeImage(MultipartFile image){
        //获取id
        long merchantId=TokenIdContext.getAndCheck(commonParamRulesConfig.getMerchantIdStart(),commonParamRulesConfig.getIdCapacity());
        //冷却期校验
        RedisUtil.checkCooldown(stringRedisTemplate,merchantRedisKeyConfig.getMerchantUpdateHomeCooldown().getRedisKey(merchantId)
                ,merchantRedisKeyConfig.getMerchantUpdateHomeCooldown().getDuration());
        //获取旧头像路径
        String oldAddr=merchantMapper.getHomeImage(merchantId);
        //快速保存
        String addr=FileSave.quickCheckAndSaveFile(image,merchantParamsRulesConfig.getHomeImageDest(), commonParamRulesConfig.getImageSize()
                , commonParamRulesConfig.getImageType());
        //检查是否成功
        if (!merchantMapper.updateHomeImage(merchantId,addr,oldAddr)) {
            //发消息使已经保存文件删除
            quickToMQDeleteFile(merchantParamsRulesConfig.getHomeImageDest(),addr);
            throw new BizException(ErrorCodeEnum.DATA_NOT_FOUND);
        }
        //如果成功,且旧地址不为空,发送消息到mq中删除旧文件
        if (oldAddr!=null&&!oldAddr.isEmpty()) quickToMQDeleteFile(merchantParamsRulesConfig.getHomeImageDest(),oldAddr);
        //清除缓存
        quickDeleteCaffeine(merchantId);
        //通知同步
        esSync(merchantId);
    }

    //更改商家的信息
    @Override
    @Transactional
    public void updateMerchantMessage(MerchantDTO merchant){
        //获取id
        long merchantId=TokenIdContext.getAndCheck(commonParamRulesConfig.getMerchantIdStart(),commonParamRulesConfig.getIdCapacity());
        //检查参数
        merchantParamsRulesConfig.merchantNameCheck(merchant.getMerchantName());
        merchantParamsRulesConfig.showDescriptionCheck(merchant.getMerchantShowDescription());
        merchantParamsRulesConfig.merchantAddrCheck(merchant.getMerchantAddr());
        commonParamRulesConfig.lonAndLatCheck(merchant.getMerchantLon(),merchant.getMerchantLat());
        //检查冷却期
        RedisUtil.checkCooldown(stringRedisTemplate,merchantRedisKeyConfig.getMerchantUpdateMessageCooldown().getRedisKey(merchantId)
                ,merchantRedisKeyConfig.getMerchantUpdateMessageCooldown().getDuration());
        //将id放入merchant实体类
        merchant.setMerchantId(merchantId);
        //更新信息,并且删除缓存
        merchantCaffeine.updateAndRemoveCaffeine(merchantId,stringRedisTemplate,merchantRedisKeyConfig.getMerchantMessageCaffeine().getRedisKey(merchantId)
                ,k->merchantMapper.updateMessage(merchant));
        //通知同步
        esSync(merchantId);
    }

    //获取更改商家密码的验证码
    @Override
    public String getUpdatePasswordOpt(){
        //获取Id并且自动检查
        long merchantId=TokenIdContext.getAndCheck(commonParamRulesConfig.getMerchantIdStart(),commonParamRulesConfig.getIdCapacity());
        //获取手机号，做发送手机号业务模拟
        String phoneNumber=phoneCaffeine.getAndAutoLoad(merchantId,stringRedisTemplate,merchantRedisKeyConfig.getMerchantPhoneCaffeine().getRedisKey(merchantId)
                ,merchantRedisKeyConfig.getMerchantPhoneCaffeine().getDuration(),String.class,k->merchantMapper.getPhoneNumber(merchantId)
        );
        //生成6位数验证码并且返回
        return OPTUtil.generateOPTAndRecord(stringRedisTemplate,merchantRedisKeyConfig.getMerchantUpdatePasswordOpt().getRedisKey(phoneNumber)
        ,merchantRedisKeyConfig.getMerchantUpdatePasswordOpt().getDuration(),6);
    }

    //改密码
    @Override
    public void updatePassword(String newPassword, String opt){
        //检查密码格式
        commonParamRulesConfig.passwordCheck(newPassword);
        //获取Id并且自动检查
        long merchantId=TokenIdContext.getAndCheck(commonParamRulesConfig.getMerchantIdStart(),commonParamRulesConfig.getIdCapacity());
        //获取手机号，做发送手机号业务模拟
        String phoneNumber=phoneCaffeine.getAndAutoLoad(merchantId,stringRedisTemplate,merchantRedisKeyConfig.getMerchantPhoneCaffeine().getRedisKey(merchantId)
                ,merchantRedisKeyConfig.getMerchantPhoneCaffeine().getDuration(),String.class,k->merchantMapper.getPhoneNumber(merchantId)
        );
        //检查验证码
        OPTUtil.checkOPT(stringRedisTemplate,merchantRedisKeyConfig.getMerchantUpdatePasswordOpt().getRedisKey(phoneNumber),opt);
        //检查冷却期，防止恶意刷接口
        RedisUtil.checkCooldown(stringRedisTemplate,merchantRedisKeyConfig.getMerchantUpdatePasswordCooldown().getRedisKey(merchantId)
        ,merchantRedisKeyConfig.getMerchantUpdatePasswordCooldown().getDuration());
        if (!merchantMapper.updatePassword(merchantId,newPassword)) throw new BizException(ErrorCodeEnum.DATA_NOT_FOUND);
    }

    //获取注销账户所需的验证码
    @Override
    public String getDeleteMerchantOpt(){
        //获取Id并且自动检查
        long merchantId=TokenIdContext.getAndCheck(commonParamRulesConfig.getMerchantIdStart(),commonParamRulesConfig.getIdCapacity());
        //获取手机号，做发送手机号业务模拟
        String phoneNumber=phoneCaffeine.getAndAutoLoad(merchantId,stringRedisTemplate,merchantRedisKeyConfig.getMerchantPhoneCaffeine().getRedisKey(merchantId)
                ,merchantRedisKeyConfig.getMerchantPhoneCaffeine().getDuration(),String.class,k->merchantMapper.getPhoneNumber(merchantId)
        );
        //生成6位数验证码并且返回
        return OPTUtil.generateOPTAndRecord(stringRedisTemplate,merchantRedisKeyConfig.getMerchantDeleteOpt().getRedisKey(phoneNumber)
                ,merchantRedisKeyConfig.getMerchantDeleteOpt().getDuration(),6);
    }

    //注销账户
    @Override
    public void deleteMerchant(String opt){
        //获取Id并且自动检查
        long merchantId=TokenIdContext.getAndCheck(commonParamRulesConfig.getMerchantIdStart(),commonParamRulesConfig.getIdCapacity());
        //获取手机号，做发送手机号业务模拟
        String phoneNumber=phoneCaffeine.getAndAutoLoad(merchantId,stringRedisTemplate,merchantRedisKeyConfig.getMerchantPhoneCaffeine().getRedisKey(merchantId)
                ,merchantRedisKeyConfig.getMerchantPhoneCaffeine().getDuration(),String.class,k->merchantMapper.getPhoneNumber(merchantId)
        );
        //检查验证码
        OPTUtil.checkOPT(stringRedisTemplate,merchantRedisKeyConfig.getMerchantDeleteOpt().getRedisKey(phoneNumber),opt);
        //检查冷却期，防止恶意反复注销
        RedisUtil.checkCooldown(stringRedisTemplate,merchantRedisKeyConfig.getMerchantDeleteCooldown().getRedisKey(phoneNumber)
                ,merchantRedisKeyConfig.getMerchantDeleteCooldown().getDuration());
        //删除商家,并且删除缓存
        merchantCaffeine.updateAndRemoveCaffeine(merchantId,stringRedisTemplate,merchantRedisKeyConfig.getMerchantMessageCaffeine().getRedisKey(merchantId)
                ,k->merchantMapper.deleteMerchant(merchantId));
        //删除token存储
        stringRedisTemplate.delete(commonRedisKeyConfig.getLoginToken().getRedisKey(merchantId));
        //mq同步Es进行逻辑删除操作
        esSync(merchantId);
        //mq通知进行后续销毁操作
        MQUtil.send(merchantExchangeConfig.getExchangeName(),merchantExchangeConfig.getDeleteMerchantQueue().getRoutingKey(),merchantId,rabbitTemplate);
    }

    //开业或者停业
    @Override
    public void updateOpen(){
        //获取id
        long merchantId=TokenIdContext.getAndCheck(commonParamRulesConfig.getMerchantIdStart(),commonParamRulesConfig.getIdCapacity());
        //检查冷却
        RedisUtil.checkCooldown(stringRedisTemplate,merchantRedisKeyConfig.getMerchantUpdateOpenCooldown().getRedisKey(merchantId)
        ,merchantRedisKeyConfig.getMerchantUpdateOpenCooldown().getDuration());
        //更新状态,并且删除缓存
        merchantCaffeine.updateAndRemoveCaffeine(merchantId,stringRedisTemplate,merchantRedisKeyConfig.getMerchantMessageCaffeine().getRedisKey(merchantId)
                ,k->merchantMapper.updateOpen(merchantId));
        //通知同步
        esSync(merchantId);
    }

    //获取距离
    @Override
    public Long getDistance(double lon,double lat,long merchantId){
        //检测经纬度格式
        commonParamRulesConfig.lonAndLatCheck(lon,lat);
        //检测商家id的合法性
        commonParamRulesConfig.merchantIdCheck(merchantId);
        //获取id
        long tokenId=TokenIdContext.getAndToLong();
        //检查冷却
        RedisUtil.checkCooldown(stringRedisTemplate,merchantRedisKeyConfig.getMerchantGetDistanceCooldown().getRedisKey(tokenId)
        ,merchantRedisKeyConfig.getMerchantGetDistanceCooldown().getDuration());
        SimplePoint simplePoint=merchantMapper.getLonLat(merchantId);
        if (simplePoint==null)return null;
        Point userPoint=ctx.getShapeFactory().pointLatLon(lat,lon);
        Point merchantPoint=ctx.getShapeFactory().pointLatLon(simplePoint.getLat(),simplePoint.getLon());
        //返回值为角度，所以要再乘上常量变为km,最后乘上1000变为m
        return (long) (ctx.calcDistance(userPoint,merchantPoint)*DistanceUtils.DEG_TO_KM*1000);
    }




    //快捷的发送至MQ删除文件
    private void quickToMQDeleteFile(String dest,String addr){
        //发消息使已经保存文件删除
        MQUtil.send(merchantExchangeConfig.getExchangeName(),merchantExchangeConfig.getDeleteFileMerchantQueue().getRoutingKey()
                , Paths.get(dest,addr).toString(),rabbitTemplate);
    }

    //快速获取证明图片旧地址
    private String quickGetProofOldAddr(long merchantId,int index){
        String oldAddr=merchantMapper.getMerchantProofImageByIndex(merchantId,index);
        if (oldAddr==null)throw new BizException(ErrorCodeEnum.DATA_NOT_FOUND);
        return oldAddr;
    }

    //快速获取展示图片旧地址
    private String quickGetShowOldAddr(long merchantId,int index){
        String oldAddr=merchantMapper.getShowImageByIndex(merchantId,index);
        if (oldAddr==null)throw new BizException(ErrorCodeEnum.DATA_NOT_FOUND);
        return oldAddr;
    }

    //快速清除商家缓存
    private void quickDeleteCaffeine(long merchantId){
        merchantCaffeine.deleteAllCaffeine(merchantId,stringRedisTemplate,merchantRedisKeyConfig.getMerchantMessageCaffeine().getRedisKey(merchantId));
    }

    //通知进行同步
    public void esSync(long merchantId){
        merchantUtil.esSync(merchantId);
    }





}
