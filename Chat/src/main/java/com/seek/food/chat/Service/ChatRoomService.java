package com.seek.food.chat.Service;

import com.seek.food.dto.Chat.ChatRoomDTO;

import java.util.List;

public interface ChatRoomService {
    public List<ChatRoomDTO> getChatRoomList(int start, int need);
    public void checkIdAndRoom(long roomId,long accountId);
}
