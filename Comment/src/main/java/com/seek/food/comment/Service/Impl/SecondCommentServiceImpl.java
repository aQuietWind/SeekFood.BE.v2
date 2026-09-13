package com.seek.food.comment.Service.Impl;

import com.seek.food.comment.Mapper.SecondCommentMapper;
import com.seek.food.comment.Service.FirstCommentService;
import com.seek.food.comment.Service.SecondCommentService;
import com.seek.food.config.Data.RedisKeyData;
import com.seek.food.config.NacosConfig.Comment.CommentParamsRulesConfig;
import com.seek.food.config.NacosConfig.Comment.CommentRedisKeyConfig;
import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.MQ.CommentExchangeConfig;
import com.seek.food.dto.Comment.FirstCommentDTO;
import com.seek.food.dto.Comment.SecondCommentDTO;
import com.seek.food.dto.Common.ChangeAmountDTO;
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
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.List;

@Service
@Slf4j
@RefreshScope
public class SecondCommentServiceImpl implements SecondCommentService {

    private final CommentParamsRulesConfig commentParamsRulesConfig;
    private final CommonParamRulesConfig commonParamRulesConfig;
    private final CommentExchangeConfig commentExchangeConfig;
    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final CommentRedisKeyConfig commentRedisKeyConfig;
    private final FirstCommentService firstCommentService;
    private final SecondCommentMapper secondCommentMapper;

    public SecondCommentServiceImpl(CommentParamsRulesConfig commentParamsRulesConfig, CommonParamRulesConfig commonParamRulesConfig, CommentExchangeConfig commentExchangeConfig, RabbitTemplate rabbitTemplate, StringRedisTemplate stringRedisTemplate, CommentRedisKeyConfig commentRedisKeyConfig, FirstCommentService firstCommentService, SecondCommentMapper secondCommentMapper) {
        this.commentParamsRulesConfig = commentParamsRulesConfig;
        this.commonParamRulesConfig = commonParamRulesConfig;
        this.commentExchangeConfig = commentExchangeConfig;
        this.rabbitTemplate = rabbitTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.commentRedisKeyConfig = commentRedisKeyConfig;
        this.firstCommentService = firstCommentService;
        this.secondCommentMapper = secondCommentMapper;
    }


    @PostConstruct
    public void init(){
        //初始化id计数器
        stringRedisTemplate.opsForValue().setIfAbsent(commentRedisKeyConfig.getSecondCommentIdCount().getName(),""+commonParamRulesConfig.getIdCapacity());
        //创建目录
        FileSave.createDestDir(commentParamsRulesConfig.getSecondCommentImageDest());
    }

    //插入一条二级评论
    @Override
    public void insertComment(String description, long firstCommentId, MultipartFile file){
        //检查格式
        commentParamsRulesConfig.commentDescriptionCheck(description);
        commonParamRulesConfig.commonIdCheck(firstCommentId);
        //检查冷却期
        long userId=quickGetIdAndCheckCooldown(commentRedisKeyConfig.getCommentInsertCooldown(),quickGetUserId());
        //检查该一级评论是否存在
        if (firstCommentService.getDetail(firstCommentId)==null) throw new BizException(ErrorCodeEnum.DATA_NOT_FOUND);
        //插入评论
        quickInsertComment(
                new SecondCommentDTO(IdUtil.IdGenerateByIncrease(commentRedisKeyConfig.getSecondCommentIdCount().getName(),stringRedisTemplate),
                firstCommentId,userId,null,description,null,null,false,null)
                ,file);
    }

    //插入一条商家回复,该逻辑主要与上文重复，可合并方法，但是我实在太累了
    @Override
    public void insertMerchantComment(String description, long firstCommentId, MultipartFile file){
        //检查格式
        commentParamsRulesConfig.commentDescriptionCheck(description);
        commonParamRulesConfig.commonIdCheck(firstCommentId);
        //检查冷却期
        long merchantId=quickGetIdAndCheckCooldown(commentRedisKeyConfig.getCommentInsertCooldown(),quickGetMerchantId());
        //检查该一级评论是否存在以及其商家id是否是该商家
        FirstCommentDTO firstComment = firstCommentService.getDetail(firstCommentId);
        if (firstComment==null||firstComment.getMerchantId()!=merchantId) throw new BizException(ErrorCodeEnum.DATA_NOT_FOUND);
        //插入评论
        quickInsertComment(
                new SecondCommentDTO(IdUtil.IdGenerateByIncrease(commentRedisKeyConfig.getSecondCommentIdCount().getName(),stringRedisTemplate),
                firstCommentId,merchantId,null,description,null,null,true,null)
                ,file);
    }

    //批量查询二级评论
    @Override
    public List<SecondCommentDTO> getList(int start, int need, long firstCommentId){
        //检查格式
        commonParamRulesConfig.needNumberCheck(need);
        commonParamRulesConfig.commonIdCheck(firstCommentId);
        //检查冷却
        quickCooldown(commentRedisKeyConfig.getSecondGetListCooldown(),TokenIdContext.get());
        //返回结果
        return secondCommentMapper.getList(start,need,firstCommentId);
    }

    //删除二级评论
    @Override
    public void deleteComment(long commentId){
        //检查格式
        commonParamRulesConfig.commonIdCheck(commentId);
        //检查冷却
        long tokenId=quickGetIdAndCheckCooldown(commentRedisKeyConfig.getCommentDeleteCooldown(),quickGetUserOrMerchantId());
        //尝试删除
        if (!secondCommentMapper.deleteComment(commentId,tokenId))throw new BizException(ErrorCodeEnum.DATA_NOT_FOUND);
        //删除照片
        String addr=secondCommentMapper.getImageAddrAfterDelete(commentId);
        if (addr!=null&&!addr.isEmpty())quickDeleteFile(addr);
        //这里可选择发送到MQ自减一级评论的二级评论数（根据业务一致性和用户体验性来自行抉择）
    }



    private void quickCooldown(RedisKeyData key, Object id){
        RedisUtil.checkCooldown(stringRedisTemplate,key.getRedisKey(id),key.getDuration());
    }

    private long quickGetUserId(){
        return TokenIdContext.getAndCheck(commonParamRulesConfig.getUserIdStart(),commonParamRulesConfig.getIdCapacity());
    }

    private long quickGetMerchantId(){
        return TokenIdContext.getAndCheck(commonParamRulesConfig.getMerchantIdStart(),commonParamRulesConfig.getIdCapacity());
    }

    private long quickGetUserOrMerchantId(){
        long tokenId= TokenIdContext.getAndToLong();
        commonParamRulesConfig.userOrMerchantIdCheck(tokenId);
        return tokenId;
    }

    private long quickGetIdAndCheckCooldown(RedisKeyData key, long id){
        quickCooldown(key,id);
        return id;
    }

    private String quickSave(MultipartFile file){
        return FileSave.quickCheckAndSaveFile(
                file
                ,commentParamsRulesConfig.getSecondCommentImageDest()
                ,commonParamRulesConfig.getImageSize()
                ,commonParamRulesConfig.getImageType());
    }
    private void quickDeleteFile(String addr){
        MQUtil.send(commentExchangeConfig.getExchangeName(),commentExchangeConfig.getDeleteFileCommentQueue().getRoutingKey()
                , Paths.get(commentParamsRulesConfig.getSecondCommentImageDest(),addr).toString(),rabbitTemplate);
    }
    private void quickSend(String routingKey,Object message){
        MQUtil.send(commentExchangeConfig.getExchangeName(),routingKey,message,rabbitTemplate);
    }

    private void quickInsertComment(SecondCommentDTO comment,MultipartFile file){
        String addr=null;
        //先保存文件，然后再插入
        if (file!=null&&!file.isEmpty()) {
            addr = quickSave(file);
            comment.setCommentImageAddr(addr);
        }
        //尝试插入,如果未成功则删除图片，虽然说几乎一定会成功的
        if (!secondCommentMapper.insertComment(comment)){
            if (addr!=null)quickDeleteFile(addr);
            throw new BizException(ErrorCodeEnum.SERVER_ERROR);
        }
        //发送消息到MQ自增一级评论的二级评论数
        quickSend(commentExchangeConfig.getChangeSecondCommentAmountQueue().getRoutingKey(),new ChangeAmountDTO(comment.getFirstCommentId(),1));
    }

}
