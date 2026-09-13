package com.seek.food.interaction.Service.Impl;

import com.seek.food.config.Data.QueueData;
import com.seek.food.config.Data.RedisKeyData;
import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.Interaction.InteractionParamsRulesConfig;
import com.seek.food.config.NacosConfig.Interaction.InteractionRedisKeyConfig;
import com.seek.food.config.NacosConfig.MQ.InteractionExchangeConfig;
import com.seek.food.dto.Common.ChangeAmountDTO;
import com.seek.food.dto.Common.SyncStateDTO;
import com.seek.food.interaction.Mapper.CollectMapper;
import com.seek.food.interaction.Service.CollectService;
import com.seek.food.interaction.Service.LikeService;
import com.seek.food.util.Context.TokenIdContext;
import com.seek.food.util.MQ.MQUtil;
import com.seek.food.util.Redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RefreshScope
public class CollectServiceImpl implements CollectService {


    private final InteractionExchangeConfig interactionExchangeConfig;
    private final RabbitTemplate rabbitTemplate;
    private final CommonParamRulesConfig commonParamRulesConfig;
    private final StringRedisTemplate stringRedisTemplate;
    private final InteractionParamsRulesConfig interactionParamsRulesConfig;
    private final InteractionRedisKeyConfig interactionRedisKeyConfig;
    private final CollectMapper collectMapper;

    public CollectServiceImpl(InteractionExchangeConfig interactionExchangeConfig, RabbitTemplate rabbitTemplate, CommonParamRulesConfig commonParamRulesConfig, StringRedisTemplate stringRedisTemplate, InteractionParamsRulesConfig interactionParamsRulesConfig, InteractionRedisKeyConfig interactionRedisKeyConfig, CollectMapper collectMapper) {
        this.interactionExchangeConfig = interactionExchangeConfig;
        this.rabbitTemplate = rabbitTemplate;
        this.commonParamRulesConfig = commonParamRulesConfig;
        this.stringRedisTemplate = stringRedisTemplate;
        this.interactionParamsRulesConfig = interactionParamsRulesConfig;
        this.interactionRedisKeyConfig = interactionRedisKeyConfig;
        this.collectMapper = collectMapper;
    }

    //收藏商家
    @Override
    public Boolean collectMerchant(long merchantId,boolean value){
        commonParamRulesConfig.merchantIdCheck(merchantId);
        return quickCollect(interactionRedisKeyConfig.getInteractionCollectMerchant()
                ,merchantId
                ,value
                ,0
                ,interactionExchangeConfig.getChangeMerchantCollectAmountQueue());
    }

    //查看是否收藏该商家
    @Override
    public Boolean getCollectMerchant(long merchantId){
        commonParamRulesConfig.merchantIdCheck(merchantId);
        return quickGetCollect(interactionRedisKeyConfig.getInteractionCollectMerchant(),merchantId);
    }

    @Override
    public List<Long> getCollectMerchantList(int start, int need){
        commonParamRulesConfig.needNumberCheck(need);
        return collectMapper.getCollectList(start,need,quickGetTokenId(),0);
    }



    private long quickGetTokenId(){
        return TokenIdContext.getAndToLong();
    }

    private Boolean quickSetBitMap(long aimId, RedisKeyData key, Object accountId, boolean value){
        return RedisUtil.oftenSetBitWithPerX(stringRedisTemplate,key.getRedisKey(accountId),aimId,value,commonParamRulesConfig.getIdCapacity(),
                interactionParamsRulesConfig.getBitmapPerXNumber());
    }

    private Boolean quickGetBitMap(long aimId, RedisKeyData key,Object accountId){
        return RedisUtil.oftenGetBitWithPerX(stringRedisTemplate,key.getRedisKey(accountId),aimId,commonParamRulesConfig.getIdCapacity(),
                interactionParamsRulesConfig.getBitmapPerXNumber());
    }

    private void quickSend(QueueData queue, Object message){
        MQUtil.send(interactionExchangeConfig.getExchangeName(),queue.getRoutingKey(),message,rabbitTemplate);
    }

    private boolean quickCollect(RedisKeyData key,long aimId,boolean value,int type,QueueData amountQueue){
        long tokenId=quickGetTokenId();
        Boolean result = quickSetBitMap(aimId,key,tokenId,value);
        //发送消息到同步数目与状态分别两个MQ
        if(result){
            quickSend(amountQueue,new ChangeAmountDTO(aimId,value));
            quickSend(interactionExchangeConfig.getSyncCollectStateQueue(),new SyncStateDTO(aimId,tokenId,value,type));
        }
        return result;
    }

    private boolean quickGetCollect(RedisKeyData key,long aimId){
        return quickGetBitMap(aimId,key,quickGetTokenId());
    }

}
