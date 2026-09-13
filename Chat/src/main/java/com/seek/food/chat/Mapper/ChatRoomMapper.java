package com.seek.food.chat.Mapper;

import com.seek.food.dto.Chat.ChatRoomDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChatRoomMapper {
    public void insert(ChatRoomDTO room);
    public List<ChatRoomDTO> userGetList(int start, int need,long accountId);
    public List<ChatRoomDTO> merchantGetList(int start, int need,long accountId);
    public List<ChatRoomDTO> riderGetList(int start, int need,long accountId);
    public void complete(long orderId);
    public boolean check(long roomId,long accountId);
}
