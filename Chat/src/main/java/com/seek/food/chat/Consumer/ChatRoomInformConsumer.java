package com.seek.food.chat.Consumer;

import com.seek.food.chat.WebSocketServer.ChatInformServer;
import com.seek.food.config.Enum.MQNameKeyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class ChatRoomInformConsumer {
    private final ChatInformServer chatInformServer;

    @Autowired
    public ChatRoomInformConsumer(ChatInformServer chatInformServer) {
        this.chatInformServer = chatInformServer;
    }

    @RabbitListener(queues = MQNameKeyEnum.Chat_Exchange_Chat_Inform_Queue)
    public void chatRoomInformQueue(long roomId) throws IOException {
        chatInformServer.broadcastRoomId(roomId,"有新的消息到达");
    }
















}
