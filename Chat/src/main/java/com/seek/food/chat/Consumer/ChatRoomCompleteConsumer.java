package com.seek.food.chat.Consumer;

import com.seek.food.chat.Mapper.ChatRoomMapper;
import com.seek.food.config.Enum.MQNameKeyEnum;
import com.seek.food.config.NacosConfig.Chat.ChatParamsRulesConfig;
import com.seek.food.config.NacosConfig.Chat.ChatRedisKeyConfig;
import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.dto.Chat.ChatRoomDTO;
import com.seek.food.util.CommonUtil.IdUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ChatRoomCompleteConsumer {
    private final ChatRoomMapper chatRoomMapper;

    @Autowired
    public ChatRoomCompleteConsumer(ChatRoomMapper chatRoomMapper) {
        this.chatRoomMapper = chatRoomMapper;
    }

    @RabbitListener(queues = MQNameKeyEnum.Order_Exchange_Chat_Room_Complete_Queue)
    public void chatRoomCompleteQueue(long orderId) {
        chatRoomMapper.complete(orderId);
    }
















}
