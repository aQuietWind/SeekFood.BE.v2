package com.seek.food.dto.Voucher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoucherConnectionDTO {
    private Long connectionId;
    private Long voucherId;
    private Long userId;
    private Long promotionId;
    private Long orderId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
    private Boolean lock;
    private Boolean use;
}
