package com.seek.food.chat.Service.Impl;

import com.seek.food.chat.Mapper.ChatRecordMapper;
import com.seek.food.chat.Service.ChatRecordService;
import com.seek.food.chat.Service.ChatRoomService;
import com.seek.food.config.Data.RedisKeyData;
import com.seek.food.config.NacosConfig.Chat.ChatParamsRulesConfig;
import com.seek.food.config.NacosConfig.Chat.ChatRedisKeyConfig;
import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.MQ.ChatExchangeConfig;
import com.seek.food.dto.Chat.ChatRecordDTO;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@Slf4j
@RefreshScope
public class ChatRecordServiceImpl implements ChatRecordService {


    private final StringRedisTemplate stringRedisTemplate;
    private final ChatRedisKeyConfig chatRedisKeyConfig;
    private final CommonParamRulesConfig commonParamRulesConfig;
    private final ChatParamsRulesConfig chatParamsRulesConfig;
    private final ChatExchangeConfig chatExchangeConfig;
    private final RabbitTemplate rabbitTemplate;
    private final ChatRecordMapper chatRecordMapper;
    private final ChatRoomService chatRoomService;

    @Autowired
    public ChatRecordServiceImpl(StringRedisTemplate stringRedisTemplate, ChatRedisKeyConfig chatRedisKeyConfig, CommonParamRulesConfig commonParamRulesConfig, ChatParamsRulesConfig chatParamsRulesConfig, ChatExchangeConfig chatExchangeConfig, RabbitTemplate rabbitTemplate, ChatRecordMapper chatRecordMapper, ChatRoomService chatRoomService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.chatRedisKeyConfig = chatRedisKeyConfig;
        this.commonParamRulesConfig = commonParamRulesConfig;
        this.chatParamsRulesConfig = chatParamsRulesConfig;
        this.chatExchangeConfig = chatExchangeConfig;
        this.rabbitTemplate = rabbitTemplate;
        this.chatRecordMapper = chatRecordMapper;
        this.chatRoomService = chatRoomService;
    }

    @PostConstruct
    public void init() {
        stringRedisTemplate.opsForValue().setIfAbsent(chatRedisKeyConfig.getChatRecordIdCount().getName(),""+commonParamRulesConfig.getIdCapacity());
        FileSave.createDestDir(chatParamsRulesConfig.getChatRecordImageDest());
    }
    //插入聊天记录
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insert(String description, MultipartFile file, long chatRoomId){
        //校验参数
        chatParamsRulesConfig.chatDescriptionCheck(description);
        commonParamRulesConfig.commonIdCheck(chatRoomId);
        //获取tokenId,并且检测冷却
        long tokenId=quickGetIdAndCheckCooldown(chatRedisKeyConfig.getChatRecordInsertCooldown(), TokenIdContext.getAndToLong());
        //检查是否存在该关联
        chatRoomService.checkIdAndRoom(chatRoomId,tokenId);
        //先保存文件
        String addr=null;
        if (file!=null&&!file.isEmpty())addr=quickSaveRecordImage(file);
        //延时删除该图片
        if (addr!=null)quickDeleteFileDelay(Paths.get(chatParamsRulesConfig.getChatRecordImageDest(),addr));
        //初始化聊天记录
        ChatRecordDTO record=new ChatRecordDTO( IdUtil.IdGenerateByIncrease(chatRedisKeyConfig.getChatRecordIdCount().getName(),stringRedisTemplate)
                , chatRoomId, tokenId, null, description, addr
                , null, chatParamsRulesConfig.getWithdrawDeadline(), null);
        //根据id类型选择相应的sql语句,type就是各个角色的id开头标识，如果追求更高的严格性，可以提取到配置类进行统一规范
        chatRecordMapper.insert(record.setTypeAndReturn(commonParamRulesConfig.getIdStart(tokenId)));
        //通知WebSocket有新的聊天记录
        quickSend(chatExchangeConfig.getChatInformQueue().getRoutingKey(),chatRoomId);
    }

    //批量查询聊天记录
    @Override
    public List<ChatRecordDTO> getList(int start, int need, long chatRoomId){
        //检查参数
        commonParamRulesConfig.commonIdCheck(chatRoomId);
        commonParamRulesConfig.needNumberCheck(need);
        //检查冷却并且获取tokenId
        long tokenId=quickGetIdAndCheckCooldown(chatRedisKeyConfig.getChatRecordGetListCooldown(), TokenIdContext.getAndToLong());
        //进行id类型判别选择SQL语句,因为作为批量查询，如果在SQL语句用or进行比较会极大的降低SQL性能
        int idStart=commonParamRulesConfig.getIdStart(tokenId);
        if (idStart==commonParamRulesConfig.getUserIdStart())return chatRecordMapper.userGetList(start,need,chatRoomId,tokenId);
        else if (idStart==commonParamRulesConfig.getMerchantIdStart())return chatRecordMapper.merchantGetList(start,need,chatRoomId,tokenId);
        else if (idStart==commonParamRulesConfig.getRiderIdStart())return chatRecordMapper.riderGetList(start,need,chatRoomId,tokenId);
        else return null;
    }

    //撤回聊天记录
    @Override
    public void withdraw(long chatRecordId){
        //检查参数
        commonParamRulesConfig.commonIdCheck(chatRecordId);
        //检查冷却并且获取tokenId
        long tokenId=quickGetIdAndCheckCooldown(chatRedisKeyConfig.getChatRecordWithdrawCooldown(), TokenIdContext.getAndToLong());
        //尝试撤回
        if (!chatRecordMapper.withdraw(chatRecordId,tokenId)) throw new BizException(ErrorCodeEnum.DATA_NOT_FOUND);
        //下面可以选择立即删除该聊天记录所附带的图片,也可以选择等待一开始的死信队列自动删除图片,我懒，所以就不选择立即清除磁盘空间了
        //但是要注意，立即删除会导致原本的死信队列消费者消费空地址,不过好在,我已经对FileRemove工具类的删除空文件时进行不报错处理了
    }



    private void quickCooldown(RedisKeyData key, Object id){
        RedisUtil.checkCooldown(stringRedisTemplate,key.getRedisKey(id),key.getDuration());
    }
    private long quickGetIdAndCheckCooldown(RedisKeyData key, long id){
        quickCooldown(key,id);
        return id;
    }

    private String quickSaveRecordImage(MultipartFile file){
        return FileSave.quickCheckAndSaveFile(file,chatParamsRulesConfig.getChatRecordImageDest(),commonParamRulesConfig.getImageSize()
                ,commonParamRulesConfig.getImageType());
    }

    private void quickDeleteFileDelay(Path path){
        MQUtil.sendWithTLL(chatExchangeConfig.getExchangeName(),chatExchangeConfig.getDeleteFileChatDeadLetterQueue().getRoutingKey()
                ,path.toString(),rabbitTemplate,chatParamsRulesConfig.getImageDeleteMillis());
    }

    private void quickSend(String routingKey,Object message){
        MQUtil.send(chatExchangeConfig.getExchangeName(),routingKey,message,rabbitTemplate);
    }
}
