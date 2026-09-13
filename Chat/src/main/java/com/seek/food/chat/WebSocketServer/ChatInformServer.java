package com.seek.food.chat.WebSocketServer;

import com.seek.food.chat.Enum.WebSocketRequestParamsEnum;
import com.seek.food.chat.Service.ChatRoomService;
import com.seek.food.config.Data.RedisKeyData;
import com.seek.food.config.NacosConfig.Chat.ChatRedisKeyConfig;
import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.Common.JWTConfig;
import com.seek.food.util.Redis.RedisUtil;
import jakarta.websocket.*;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ChatInformServer implements WebSocketHandler {

    // 存放所有在线会话
    private static final ConcurrentHashMap<Long, ConcurrentHashMap<Long,WebSocketSession>> Session_Map = new ConcurrentHashMap<>();
    private final CommonParamRulesConfig commonParamRulesConfig;
    private final ChatRoomService chatRoomService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ChatRedisKeyConfig chatRedisKeyConfig;
    private final JWTConfig jwtConfig;

    @Autowired
    public ChatInformServer(CommonParamRulesConfig commonParamRulesConfig, ChatRoomService chatRoomService, StringRedisTemplate stringRedisTemplate, ChatRedisKeyConfig chatRedisKeyConfig, JWTConfig jwtConfig) {
        this.commonParamRulesConfig = commonParamRulesConfig;
        this.chatRoomService = chatRoomService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.chatRedisKeyConfig = chatRedisKeyConfig;
        this.jwtConfig = jwtConfig;
    }

    // 连接建立成功
    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception{
        Long roomId=quickGetRoomIdFirst(session);
        //检查该请求参数
        commonParamRulesConfig.commonIdCheck(roomId);
        //获取该请求账户的Id
        long tokenId=quickGetIdAndCheckCooldown(chatRedisKeyConfig.getChatRoomWebsocketCooldown(),quickGetTokenId(session));
        //检查该账户是否有权限监听该合格聊天室
        chatRoomService.checkIdAndRoom(roomId, tokenId);
        //放置该session
        quickSaveSession(session,roomId,tokenId);
    }

    // 连接关闭
    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus closeStatus) throws Exception {
        //删除该会话
        quickRemoveSession(session);
    }

    //异常回调
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("websocket会话异常,tokenId:{},roomId:{}",quickGetTokenId(session),quickGetRoomIdFirst(session),exception);
    }
    @Override
    public boolean supportsPartialMessages() {return false;}
    /** 收到消息时的回调 */
    @Override
    public void handleMessage(@NonNull WebSocketSession session, @NonNull WebSocketMessage<?> message) throws Exception {}

    //对指定的聊天室id广播消息
    public void broadcastRoomId(long roomId,String msg) throws IOException {
        ConcurrentHashMap<Long,WebSocketSession> map=Session_Map.get(roomId);
        for (Map.Entry<Long, WebSocketSession> entry : map.entrySet()) {
            //发送消息
            if (entry.getValue().isOpen()) entry.getValue().sendMessage(new TextMessage(msg));
        }
    }

    private long quickGetIdAndCheckCooldown(RedisKeyData key, long id){
        RedisUtil.checkCooldown(stringRedisTemplate,key.getRedisKey(id),key.getDuration());
        return id;
    }
    private Long quickGetRoomIdFirst(WebSocketSession session){
        return Long.valueOf(getQueryParam(Objects.requireNonNull(session.getUri()).normalize(),WebSocketRequestParamsEnum.Room_Id));
    }
    private Long quickGetTokenId(WebSocketSession session){
        return Long.parseLong(Objects.requireNonNull(session.getHandshakeHeaders().getFirst(jwtConfig.getHeaderTokenName())));
    }
    public static String getQueryParam(URI uri, String paramName) {
        if (uri == null) return null;
        UriComponents components = UriComponentsBuilder.fromUri(uri).build();
        return components.getQueryParams().getFirst(paramName);
    }
    private void quickRemoveSession(WebSocketSession session){
        Long roomId= (Long) session.getAttributes().get(WebSocketRequestParamsEnum.Room_Id);
        if (roomId!=null)Session_Map.get(roomId).remove(quickGetTokenId(session),session);
    }
    private void quickSaveSession(WebSocketSession session,long roomId,long tokenId){
        //先放置，方便后续拿取
        session.getAttributes().put(WebSocketRequestParamsEnum.Room_Id, roomId);
        Session_Map.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>());
        Session_Map.get(roomId).put(tokenId,session);
    }
}
