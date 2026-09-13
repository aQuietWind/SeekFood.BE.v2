package com.seek.food.comment.Service.Impl;

import com.seek.food.config.Data.RedisKeyData;
import com.seek.food.config.NacosConfig.Comment.CommentParamsRulesConfig;
import com.seek.food.config.NacosConfig.Comment.CommentRedisKeyConfig;
import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.MQ.CommentExchangeConfig;
import com.seek.food.dto.Comment.FirstCommentDTO;
import com.seek.food.dto.Common.ChangeAmountDTO;
import com.seek.food.comment.Caffeine.FirstCommentCaffeine;
import com.seek.food.comment.Feign.OrderClient;
import com.seek.food.comment.Mapper.FirstCommentMapper;
import com.seek.food.comment.Service.FirstCommentService;
import com.seek.food.util.CommonUtil.IdUtil;
import com.seek.food.util.Context.TokenIdContext;
import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;
import com.seek.food.util.FileUtil.FileSave;
import com.seek.food.util.MQ.MQUtil;
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
import java.util.List;

@Service
@Slf4j
@RefreshScope
public class FirstCommentServiceImpl implements FirstCommentService {


    private final OrderClient orderClient;
    private final CommonParamRulesConfig commonParamRulesConfig;
    private final StringRedisTemplate stringRedisTemplate;
    private final CommentRedisKeyConfig commentRedisKeyConfig;
    private final FirstCommentMapper firstCommentMapper;
    private final CommentParamsRulesConfig commentParamsRulesConfig;
    private final CommentExchangeConfig commentExchangeConfig;
    private final RabbitTemplate rabbitTemplate;
    private final FirstCommentCaffeine firstCommentCaffeine;

    @Autowired
    public FirstCommentServiceImpl(OrderClient orderClient, CommonParamRulesConfig commonParamRulesConfig, StringRedisTemplate stringRedisTemplate, CommentRedisKeyConfig commentRedisKeyConfig, FirstCommentMapper firstCommentMapper, CommentParamsRulesConfig commentParamsRulesConfig, CommentExchangeConfig commentExchangeConfig, RabbitTemplate rabbitTemplate, FirstCommentCaffeine firstCommentCaffeine) {
        this.orderClient = orderClient;
        this.commonParamRulesConfig = commonParamRulesConfig;
        this.stringRedisTemplate = stringRedisTemplate;
        this.commentRedisKeyConfig = commentRedisKeyConfig;
        this.firstCommentMapper = firstCommentMapper;
        this.commentParamsRulesConfig = commentParamsRulesConfig;
        this.commentExchangeConfig = commentExchangeConfig;
        this.rabbitTemplate = rabbitTemplate;
        this.firstCommentCaffeine = firstCommentCaffeine;
    }

    @PostConstruct
    public void init(){
        //初始化id计数器
        stringRedisTemplate.opsForValue().setIfAbsent(commentRedisKeyConfig.getFirstCommentIdCount().getName(),""+commonParamRulesConfig.getIdCapacity());
        //创建目录
        FileSave.createDestDir(commentParamsRulesConfig.getFirstCommentImageDest());
    }

    //插入一级评论
    @Override
    public void insertComment(String description, long orderId, MultipartFile file){
        //检查内容
        commentParamsRulesConfig.commentDescriptionCheck(description);
        //检查冷却并获取用户id
        long userId=quickGetIdAndCheckCooldown(commentRedisKeyConfig.getCommentInsertCooldown(),quickGetUserId());
        //查询该订单是否存在
        FirstCommentDTO firstComment = orderClient.commentSelect(orderId).getData();
        //设置好必要的id
        firstComment.setFirstCommentId(IdUtil.IdGenerateByIncrease(commentRedisKeyConfig.getFirstCommentIdCount().getName(),stringRedisTemplate));
        //如果图片不为空，则保存图片并设置文件地址
        if (file!=null&&!file.isEmpty())firstComment.setCommentImageAddr(quickSave(file));
        //设置好内容
        firstComment.setCommentDescription(description);
        //尝试插入该订单
        try {
            firstCommentMapper.insertComment(firstComment);
        }catch (Exception e){
            log.error("",e);
            //失败则删除原先的图片
            if (file!=null&&!file.isEmpty())quickDeleteFile(firstComment.getCommentImageAddr());
            throw new BizException(ErrorCodeEnum.DATA_SURVIVE);
        }
        //发送至mq更改商家的一级评论数
        quickSend(commentExchangeConfig.getChangeMerchantFirstCommentAmountQueue().getRoutingKey()
                ,new ChangeAmountDTO(firstComment.getMerchantId(), 1));
        //清除可能存在的空缓存
        firstCommentCaffeine.deleteAllCaffeine(firstComment.getFirstCommentId(),stringRedisTemplate
                ,commentRedisKeyConfig.getFirstCommentCaffeine().getRedisKey(firstComment.getFirstCommentId()));
    }

    //批量获取简易信息
    @Override
    public List<FirstCommentDTO> getSimple(long merchantId, int start, int need){
        //检查格式
        commonParamRulesConfig.needNumberCheck(need);
        commonParamRulesConfig.merchantIdCheck(merchantId);
        //检查冷却
        quickCooldown(commentRedisKeyConfig.getFirstGetSimpleCooldown(),TokenIdContext.get());
        //返回结果
        return firstCommentMapper.getSimple(merchantId,start,need);
    }

    //获取某一级评论的详细信息
    @Override
    public FirstCommentDTO getDetail(long commentId){
        //检查格式
        commonParamRulesConfig.commonIdCheck(commentId);
        //获取信息并且写入缓存
        return firstCommentCaffeine.getAndAutoLoad(
                commentId
                ,stringRedisTemplate
                ,commentRedisKeyConfig.getFirstCommentCaffeine().getRedisKey(commentId)
                ,commentRedisKeyConfig.getFirstCommentCaffeine().getDuration()
                ,FirstCommentDTO.class
                , k->firstCommentMapper.getDetail(commentId));
    }

    //删除评论
    @Override
    public void deleteComment(long commentId){
        //检查评论id
        commonParamRulesConfig.commonIdCheck(commentId);
        //检查冷却
        long userId=quickGetIdAndCheckCooldown(commentRedisKeyConfig.getCommentDeleteCooldown(),quickGetUserId());
        //删除评论,如果你觉得该评论业务应该需要强一致性,可以在这里加一个删除缓存
        if (!firstCommentMapper.deleteComment(commentId,userId))throw new BizException(ErrorCodeEnum.DATA_NOT_FOUND);
        //删除照片
        String addr=firstCommentMapper.getImageAddrAfterDelete(commentId);
        if (addr!=null&&!addr.isEmpty())quickDeleteFile(addr);
    }


    private void quickCooldown(RedisKeyData key, Object id){
        RedisUtil.checkCooldown(stringRedisTemplate,key.getRedisKey(id),key.getDuration());
    }

    private long quickGetUserId(){
        return TokenIdContext.getAndCheck(commonParamRulesConfig.getUserIdStart(),commonParamRulesConfig.getIdCapacity());
    }


    private long quickGetIdAndCheckCooldown(RedisKeyData key, long id){
        quickCooldown(key,id);
        return id;
    }

    private String quickSave(MultipartFile file){
        return FileSave.quickCheckAndSaveFile(
                file
                ,commentParamsRulesConfig.getFirstCommentImageDest()
                ,commonParamRulesConfig.getImageSize()
                ,commonParamRulesConfig.getImageType());
    }
    private void quickDeleteFile(String addr){
        MQUtil.send(commentExchangeConfig.getExchangeName(),commentExchangeConfig.getDeleteFileCommentQueue().getRoutingKey()
                , Paths.get(commentParamsRulesConfig.getFirstCommentImageDest(),addr).toString(),rabbitTemplate);
    }
    private void quickSend(String routingKey,Object message){
        MQUtil.send(commentExchangeConfig.getExchangeName(),routingKey,message,rabbitTemplate);
    }

}
