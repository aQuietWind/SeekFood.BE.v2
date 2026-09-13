package com.seek.food.dto.Chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRecordDTO {
    private Long recordId;
    private Long chatRoomId;
    private Long accountId;
    private Integer accountType;
    private String chatDescription;
    private String chatShowImageAddr;
    private LocalDateTime createTime;
    private LocalDateTime withdrawDeadline;
    private Boolean withdraw;

    public ChatRecordDTO setTypeAndReturn(int type){
        this.accountType=type;
        return this;
    }
}
