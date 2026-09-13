package com.seek.food.chat.Service.Impl;

import com.seek.food.chat.Mapper.ChatRoomMapper;
import com.seek.food.chat.Service.ChatRoomService;
import com.seek.food.config.NacosConfig.Chat.ChatRedisKeyConfig;
import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.dto.Chat.ChatRoomDTO;
import com.seek.food.util.Context.TokenIdContext;
import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;
import com.seek.food.util.Redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RefreshScope
public class ChatRoomServiceImpl implements ChatRoomService {
    private final StringRedisTemplate stringRedisTemplate;
    private final ChatRoomMapper chatRoomMapper;
    private final ChatRedisKeyConfig chatRedisKeyConfig;
    private final CommonParamRulesConfig commonParamRulesConfig;

    public ChatRoomServiceImpl(StringRedisTemplate stringRedisTemplate, ChatRoomMapper chatRoomMapper
            , ChatRedisKeyConfig chatRedisKeyConfig, CommonParamRulesConfig commonParamRulesConfig) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.chatRoomMapper = chatRoomMapper;
        this.chatRedisKeyConfig = chatRedisKeyConfig;
        this.commonParamRulesConfig = commonParamRulesConfig;
    }

    //获取聊天室列表
    public List<ChatRoomDTO> getChatRoomList(int start, int need){
        //检测参数
        commonParamRulesConfig.needNumberCheck(need);
        //获取id
        long accountId= TokenIdContext.getAndToLong();
        //检测冷却
        RedisUtil.checkCooldown(stringRedisTemplate,chatRedisKeyConfig.getChatRoomGetListCooldown().getRedisKey(accountId)
                ,chatRedisKeyConfig.getChatRoomGetListCooldown().getDuration());
        //返回结果
        long idStart=accountId/commonParamRulesConfig.getIdCapacity();
        if (idStart==commonParamRulesConfig.getUserIdStart())return chatRoomMapper.userGetList(start,need,accountId);
        else if (idStart==commonParamRulesConfig.getMerchantIdStart())return chatRoomMapper.merchantGetList(start,need,accountId);
        else if (idStart==commonParamRulesConfig.getRiderIdStart())return chatRoomMapper.riderGetList(start,need,accountId);
        else return new ArrayList<>();
    }

    //获取单个聊天室
    public void checkIdAndRoom(long roomId,long accountId){
        if (!chatRoomMapper.check(roomId,accountId)) throw new BizException(ErrorCodeEnum.CONDITION_NOT_PASS);
    }




















}
