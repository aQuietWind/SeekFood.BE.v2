package com.seek.food.interaction.Service.Impl;

import com.seek.food.config.Data.QueueData;
import com.seek.food.config.Data.RedisKeyData;
import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.Interaction.InteractionParamsRulesConfig;
import com.seek.food.config.NacosConfig.Interaction.InteractionRedisKeyConfig;
import com.seek.food.config.NacosConfig.MQ.InteractionExchangeConfig;
import com.seek.food.dto.Common.ChangeAmountDTO;
import com.seek.food.dto.Common.SyncStateDTO;
import com.seek.food.interaction.Service.LikeService;
import com.seek.food.util.Context.TokenIdContext;
import com.seek.food.util.MQ.MQUtil;
import com.seek.food.util.Redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RefreshScope
public class LikeServiceImpl implements LikeService {


    private final StringRedisTemplate stringRedisTemplate;
    private final CommonParamRulesConfig commonParamRulesConfig;
    private final InteractionParamsRulesConfig interactionParamsRulesConfig;
    private final InteractionRedisKeyConfig interactionRedisKeyConfig;
    private final InteractionExchangeConfig interactionExchangeConfig;
    private final RabbitTemplate rabbitTemplate;

    public LikeServiceImpl(StringRedisTemplate stringRedisTemplate, CommonParamRulesConfig commonParamRulesConfig, InteractionParamsRulesConfig interactionParamsRulesConfig, InteractionRedisKeyConfig interactionRedisKeyConfig, InteractionExchangeConfig interactionExchangeConfig, RabbitTemplate rabbitTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.commonParamRulesConfig = commonParamRulesConfig;
        this.interactionParamsRulesConfig = interactionParamsRulesConfig;
        this.interactionRedisKeyConfig = interactionRedisKeyConfig;
        this.interactionExchangeConfig = interactionExchangeConfig;
        this.rabbitTemplate = rabbitTemplate;
    }

    //点赞商家
    @Override
    public Boolean likeMerchant(long merchantId,boolean value){
        commonParamRulesConfig.merchantIdCheck(merchantId);
        return quickLike(interactionRedisKeyConfig.getInteractionLikeMerchant()
                ,merchantId
                ,value
                ,0
                ,interactionExchangeConfig.getChangeMerchantLikeAmountQueue());
    }

    //点赞一级评论
    @Override
    public Boolean likeFirstComment(long commentId,boolean value){
        commonParamRulesConfig.commonIdCheck(commentId);
        return quickLike(interactionRedisKeyConfig.getInteractionLikeFirstComment()
                ,commentId
                ,value
                ,1
                ,interactionExchangeConfig.getChangeFirstCommentLikeAmountQueue());
    }

    //点赞二级评论
    @Override
    public Boolean likeSecondComment(long commentId,boolean value){
        commonParamRulesConfig.commonIdCheck(commentId);
        return quickLike(interactionRedisKeyConfig.getInteractionLikeSecondComment()
                ,commentId
                ,value
                ,2
                ,interactionExchangeConfig.getChangeSecondCommentLikeAmountQueue());
    }

    //查看对商家的点赞
    @Override
    public Boolean getLikeMerchant(long merchantId){
        commonParamRulesConfig.merchantIdCheck(merchantId);
        return quickGetLike(interactionRedisKeyConfig.getInteractionLikeMerchant(),merchantId);
    }

    //查看对一级评论的点赞
    @Override
    public Boolean getLikeFirstComment(long commentId){
        commonParamRulesConfig.commonIdCheck(commentId);
        return quickGetLike(interactionRedisKeyConfig.getInteractionLikeFirstComment(),commentId);
    }

    //查看对二级评论的点赞
    @Override
    public Boolean getLikeSecondComment(long commentId){
        commonParamRulesConfig.commonIdCheck(commentId);
        return quickGetLike(interactionRedisKeyConfig.getInteractionLikeSecondComment(),commentId);
    }





    private long quickGetTokenId(){
        return TokenIdContext.getAndToLong();
    }

    private Boolean quickSetBitMap(long aimId, RedisKeyData key,Object accountId, boolean value){
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

    private boolean quickLike(RedisKeyData key,long aimId,boolean value,int type,QueueData amountQueue){
        long tokenId=quickGetTokenId();
        Boolean result = quickSetBitMap(aimId,key,tokenId,value);
        //发送消息到同步数目与状态分别两个MQ
        if(result){
            quickSend(amountQueue,new ChangeAmountDTO(aimId,value));
            quickSend(interactionExchangeConfig.getSyncLikeStateQueue(),new SyncStateDTO(aimId,tokenId,value,type));
        }
        return result;
    }

    private boolean quickGetLike(RedisKeyData key,long aimId){
        return quickGetBitMap(aimId,key,quickGetTokenId());
    }






}
