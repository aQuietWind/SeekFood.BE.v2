package com.seek.food.dto.Voucher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoucherConnectionMQDTO {
    private Long voucherId;
    private Long userId;
    private Long promotionId;
}
