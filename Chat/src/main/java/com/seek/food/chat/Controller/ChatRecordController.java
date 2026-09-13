package com.seek.food.chat.Controller;

import com.seek.food.chat.Enum.RequestPathEnum;
import com.seek.food.chat.Service.ChatRecordService;
import com.seek.food.dto.Chat.ChatRecordDTO;
import com.seek.food.dto.Common.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(RequestPathEnum.Chat_Record)
public class ChatRecordController {

    private final ChatRecordService chatRecordService;

    public ChatRecordController(ChatRecordService chatRecordService) {
        this.chatRecordService = chatRecordService;
    }

    //插入聊天
    @PostMapping
    public Result<Void> insert(String description,@RequestBody MultipartFile file, long chatRoomId){
        chatRecordService.insert(description,file,chatRoomId);
        return Result.success();
    }

    //查询聊天记录
    @GetMapping(RequestPathEnum.Chat_Record_Get_List)
    public Result<List<ChatRecordDTO>> getList(int start,int need,long chatRoomId){
        return Result.success(chatRecordService.getList(start,need,chatRoomId));
    }

    //撤回记录
    @PutMapping(RequestPathEnum.Chat_Record_Withdraw)
    public Result<Void> withdraw(long chatRecordId){
        chatRecordService.withdraw(chatRecordId);
        return Result.success();
    }

}
