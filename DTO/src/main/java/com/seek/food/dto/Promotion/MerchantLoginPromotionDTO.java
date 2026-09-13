package com.seek.food.dto.Promotion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class MerchantLoginPromotionDTO {
    private Long promotionId;
    private Long merchantId;
    private Long voucherId;
    private String promotionTitle;
    private String promotionDescription;
    private String promotionNotice;
    private Long participantAmount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
}
