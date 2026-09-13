package com.seek.food.chat.Service;

import com.seek.food.dto.Chat.ChatRecordDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ChatRecordService {
    public void insert(String description, MultipartFile file, long chatRoomId);
    public List<ChatRecordDTO> getList(int start, int need, long chatRoomId);
    public void withdraw(long chatRecordId);
}
