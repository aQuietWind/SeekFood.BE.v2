package com.seek.food.dto.Chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChatRoomDTO {
    private Long roomId;
    private Long orderId;
    private Long userId;
    private Long merchantId;
    private Long riderId;
    private LocalDateTime createTime;
    private LocalDateTime endingTime;
    private Boolean complete;

    public ChatRoomDTO(long orderId, long userId, long merchantId, long riderId) {
        this.orderId = orderId;
        this.userId = userId;
        this.merchantId = merchantId;
        this.riderId = riderId;
    }

    public ChatRoomDTO quickInit(long roomId,int XDays) {
        this.roomId = roomId;
        this.endingTime  = LocalDateTime.now().plusDays(XDays);
        return this;
    }
}
