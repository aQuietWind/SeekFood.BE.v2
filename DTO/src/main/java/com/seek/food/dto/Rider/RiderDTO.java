package com.seek.food.dto.Rider;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RiderDTO {
    private Long riderId;
    private String riderName;
    private String riderCode;
    private String riderPhoneNumber;
    private String riderPassword;
    private String riderPersonImageAddr;
    private Integer riderSex;
    private LocalDateTime createTime;
    private Boolean delete;
}
