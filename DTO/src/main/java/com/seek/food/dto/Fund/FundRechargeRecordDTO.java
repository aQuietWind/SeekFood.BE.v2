package com.seek.food.dto.Fund;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FundRechargeRecordDTO {
    private Long recordId;
    private Long accountId;
    private String rechargeDescription;
    private Double rechargeAmount;
    private LocalDateTime createTime;
}
