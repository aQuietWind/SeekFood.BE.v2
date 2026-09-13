package com.seek.food.chat.Configuration;

import com.seek.food.chat.Enum.RequestPathEnum;
import com.seek.food.chat.WebSocketServer.ChatInformServer;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatInformServer chatInformServer;

    public WebSocketConfig(ChatInformServer chatInformServer) {
        this.chatInformServer = chatInformServer;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatInformServer, RequestPathEnum.Chat_Room_WebSocket)
                .setAllowedOrigins("*");
    }
}