package com.seek.food.chat.Mapper;

import com.seek.food.dto.Chat.ChatRecordDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChatRecordMapper {
    public void insert(ChatRecordDTO record);
    public List<ChatRecordDTO> userGetList(int start, int need, long chatRoomId,long accountId);
    public List<ChatRecordDTO> merchantGetList(int start, int need, long chatRoomId,long accountId);
    public List<ChatRecordDTO> riderGetList(int start, int need, long chatRoomId,long accountId);
    public boolean withdraw(long recordId,long accountId);
}
