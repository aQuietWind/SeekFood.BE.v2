package com.seek.food.chat.Controller;

import com.seek.food.chat.Enum.RequestPathEnum;
import com.seek.food.chat.Service.ChatRoomService;
import com.seek.food.dto.Chat.ChatRoomDTO;
import com.seek.food.dto.Common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping(RequestPathEnum.Chat_Room)
@RestController
public class ChatRoomController {


    private final ChatRoomService chatRoomService;

    public ChatRoomController(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }

    @GetMapping(RequestPathEnum.Chat_Room_Get_List)
    public Result<List<ChatRoomDTO>> getChatRoomList(int start, int need){
        return Result.success(chatRoomService.getChatRoomList(start,need));
    }
}
