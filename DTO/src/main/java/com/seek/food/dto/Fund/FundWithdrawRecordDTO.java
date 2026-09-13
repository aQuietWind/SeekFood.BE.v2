package com.seek.food.dto.Fund;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FundWithdrawRecordDTO {
    private Long recordId;
    private Long accountId;
    private String withdrawDescription;
    private Double withdrawAmount;
    private LocalDateTime createTime;
}
