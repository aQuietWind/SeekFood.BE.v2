package com.seek.food.meal.Service.Impl;

import com.seek.food.config.Data.RedisKeyData;
import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.MQ.MealExchangeConfig;
import com.seek.food.config.NacosConfig.Meal.MealParamsRulesConfig;
import com.seek.food.config.NacosConfig.Meal.MealRedisKeyConfig;
import com.seek.food.dto.Meal.MealDTO;
import com.seek.food.meal.Caffeine.MealCaffeine;
import com.seek.food.meal.Mapper.MealMapper;
import com.seek.food.meal.Service.MealService;
import com.seek.food.util.CommonUtil.IdUtil;
import com.seek.food.util.Context.TokenIdContext;
import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;
import com.seek.food.util.FileUtil.FileRemove;
import com.seek.food.util.FileUtil.FileSave;
import com.seek.food.util.MQ.MQUtil;
import com.seek.food.util.Redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;

@Service
@Slf4j
@RefreshScope
public class MealServiceImpl implements MealService {

    private final CommonParamRulesConfig commonParamRulesConfig;
    private final MealParamsRulesConfig mealParamsRulesConfig;
    private final MealMapper mealMapper;
    private final MealRedisKeyConfig mealRedisKeyConfig;
    private final MealCaffeine mealCaffeine;
    private final RabbitTemplate rabbitTemplate;
    private final MealExchangeConfig mealExchangeConfig;
    private final StringRedisTemplate stringRedisTemplate;
    public MealServiceImpl(CommonParamRulesConfig commonParamRulesConfig,MealParamsRulesConfig mealParamsRulesConfig
    ,MealMapper mealMapper,MealRedisKeyConfig mealRedisKeyConfig,MealCaffeine mealCaffeine
    ,RabbitTemplate rabbitTemplate,MealExchangeConfig mealExchangeConfig,StringRedisTemplate stringRedisTemplate) {
        this.commonParamRulesConfig = commonParamRulesConfig;
        this.mealParamsRulesConfig = mealParamsRulesConfig;
        this.mealMapper = mealMapper;
        this.mealRedisKeyConfig = mealRedisKeyConfig;
        this.mealCaffeine = mealCaffeine;
        this.mealExchangeConfig = mealExchangeConfig;
        this.rabbitTemplate = rabbitTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        //初始化mealId计数器
        stringRedisTemplate.opsForValue().setIfAbsent(mealRedisKeyConfig.getMealIdCount().getName(),""+commonParamRulesConfig.getIdCapacity());
        FileSave.createDestDir(mealParamsRulesConfig.getMealShowImageDest());
    }


    //新增餐品
    @Override
    public void insertMeal(String mealName,double mealPrice,String mealContent){
        //进行格式校验
        mealParamsRulesConfig.mealNameCheck(mealName);
        mealParamsRulesConfig.mealPriceCheck(mealPrice);
        mealParamsRulesConfig.mealContentCheck(mealContent);
        //获取商家id
        long merchantId=quickGetMerchantId();
        //检查冷却期
        quickCooldown(mealRedisKeyConfig.getMealInsertCooldown(),merchantId);
        //写入DB
        mealMapper.insertMeal(IdUtil.IdGenerateByIncrease(mealRedisKeyConfig.getMealIdCount().getName(),stringRedisTemplate),mealName,mealPrice,mealContent,merchantId);
    }

    //获取预览的餐品信息
    @Override
    public List<MealDTO> getSimple(long merchantId, int start, int need){
        String tokenId=TokenIdContext.get();
        commonParamRulesConfig.merchantIdCheck(merchantId);
        commonParamRulesConfig.needNumberCheck(need);
        quickCooldown(mealRedisKeyConfig.getMealGetSimpleCooldown(),tokenId);
        return mealMapper.getSimple(merchantId,start,need);
    }

    //根据类型获取预览信息
    @Override
    public List<MealDTO> getSimpleByType(long merchantId,int type,int start,int need){
        String tokenId=TokenIdContext.get();
        commonParamRulesConfig.merchantIdCheck(merchantId);
        commonParamRulesConfig.needNumberCheck(need);
        mealParamsRulesConfig.mealTypeCheck(type);
        quickCooldown(mealRedisKeyConfig.getMealGetSimpleByTypeCooldown(),tokenId);
        return mealMapper.getSimpleByType(merchantId,type,start,need);
    }

    //获取餐品详细信息
    @Override
    public MealDTO getDetail(long mealId){
        commonParamRulesConfig.commonIdCheck(mealId);
        return mealCaffeine.getAndAutoLoad(mealId,stringRedisTemplate,mealRedisKeyConfig.getMealMessageCaffeine().getRedisKey(mealId)
        ,mealRedisKeyConfig.getMealMessageCaffeine().getDuration(),MealDTO.class,k->mealMapper.getDetail(mealId));
    }

    //更改餐品常规的信息
    @Override
    public void updateMessage(MealDTO meal){
        long merchantId=quickGetMerchantId();
        //检查参数
        commonParamRulesConfig.commonIdCheck(meal.getMealId());
        mealParamsRulesConfig.mealTypeCheck(meal.getMealType());
        if (meal.getMealName()!=null) mealParamsRulesConfig.mealNameCheck(meal.getMealName());
        mealParamsRulesConfig.mealContentCheck(meal.getMealContent());
        mealParamsRulesConfig.mealDescriptionCheck(meal.getMealDescription());
        mealParamsRulesConfig.mealNextDiscountTimeCheck(meal.getNextDiscountTime());
        //检查冷却期
        quickCooldown(mealRedisKeyConfig.getMealUpdateMessageCooldown(),merchantId);
        meal.setMerchantId(merchantId);
        //更新并且删除缓存
        quickUpdateAndClearAllCaffeine(meal.getMealId(),k->mealMapper.updateMealMessage(meal));
    }

    //更改展示图片
    @Override
    public void updateShowImage(long mealId, MultipartFile file){
        long merchantId=quickGetMerchantId();
        commonParamRulesConfig.commonIdCheck(mealId);
        quickCooldown(mealRedisKeyConfig.getMealUpdateShowImageCooldown(),merchantId);
        //先保存新文件
        String addr= FileSave.quickCheckAndSaveFile(file,mealParamsRulesConfig.getMealShowImageDest()
                ,commonParamRulesConfig.getImageSize(),commonParamRulesConfig.getImageType());
        //获取老地址
        String oldAddr=mealMapper.getShowImageAddr(mealId);
        //更新地址，如果失败就删除新文件
        if (!mealMapper.updateShowImage(mealId,addr,oldAddr,merchantId)){
            //mq删除新文件
            MQUtil.send(mealExchangeConfig.getExchangeName(),mealExchangeConfig.getDeleteFileMealQueue().getRoutingKey()
            ,FileRemove.resolvePath(mealParamsRulesConfig.getMealShowImageDest(),addr),rabbitTemplate);
            throw new BizException(ErrorCodeEnum.DATA_NOT_FOUND);
        }
        //延时删除老文件
        quickDeleteFile(mealParamsRulesConfig.getMealShowImageDest(),oldAddr);
    }

    //更改价格
    @Override
    public void updatePrice(long mealId,double price){
        long merchantId=quickGetMerchantId();
        commonParamRulesConfig.commonIdCheck(mealId);
        mealParamsRulesConfig.mealPriceCheck(price);
        //检查冷却
        quickCooldown(mealRedisKeyConfig.getMealUpdatePriceCooldown(),merchantId);
        //更新价格，并且清除缓存
        quickUpdateAndClearAllCaffeine(mealId,k->mealMapper.updatePrice(mealId,price,merchantId));
    }

    //更改出售状态
    @Override
    public void updateSell(long mealId){
        long merchantId=quickGetMerchantId();
        commonParamRulesConfig.commonIdCheck(mealId);
        //检查冷却
        quickCooldown(mealRedisKeyConfig.getMealUpdateSellCooldown(),merchantId);
        //更新出售状态，并且清除缓存
        quickUpdateAndClearAllCaffeine(mealId,k->mealMapper.updateSell(mealId,merchantId));
    }

    //删除餐品
    @Override
    public void deleteMeal(long mealId){
        long merchantId=quickGetMerchantId();
        commonParamRulesConfig.commonIdCheck(mealId);
        //检查冷却
        quickCooldown(mealRedisKeyConfig.getMealDeleteCooldown(),merchantId);
        //写入于DB,并且清除缓存
        quickUpdateAndClearAllCaffeine(mealId,k->mealMapper.deleteMeal(mealId,merchantId));
        //延时删除图片
        quickDeleteFile(mealParamsRulesConfig.getMealShowImageDest(),mealMapper.getShowImageAddrAfterDelete(mealId));
    }


    private long quickGetMerchantId(){
        return TokenIdContext.getAndCheck(commonParamRulesConfig.getMerchantIdStart(),commonParamRulesConfig.getIdCapacity());
    }

    private void quickCooldown(RedisKeyData key,Object id){
        RedisUtil.checkCooldown(stringRedisTemplate,key.getRedisKey(id),key.getDuration());
    }

    private void quickUpdateAndClearAllCaffeine(long id,Function<Long,Boolean> updateFunction){
        //更新并且删除普通缓存，如果更新失败会自动抛异常，所以不需要判断更新成功
        mealCaffeine.updateAndRemoveCaffeine(id,stringRedisTemplate,mealRedisKeyConfig.getMealMessageCaffeine().getRedisKey(id),updateFunction);
    }

    private void quickDeleteFile(String dest,String addr){
        if (addr==null)return;
        MQUtil.sendWithTLL(mealExchangeConfig.getExchangeName(),mealExchangeConfig.getDeleteFileMealDeadLetterQueue().getRoutingKey()
        , Paths.get(dest,addr).toString(),rabbitTemplate,mealParamsRulesConfig.getMillsByFileDeleteDay());
    }

}
