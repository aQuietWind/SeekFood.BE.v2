package com.seek.food.dto.Fund;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FundDTO {
    private Long accountId;
    private LocalDateTime createTime;
    private Long fundAmount;
    private Boolean delete;

}
