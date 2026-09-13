package com.seek.food.dto.Voucher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MerchantVoucherDTO {
    private Long voucherId;
    private Long merchantId;
    private String voucherName;
    private String voucherDescription;
    private Double discountCost;
    private Double minCost;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
}
