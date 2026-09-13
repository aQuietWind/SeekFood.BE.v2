package com.seek.food.dto.Meal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MealDTO {
    private Long mealId;
    private Long merchantId;
    private String mealName;
    private Double mealPrice;
    private Double mealLastPrice;
    private String mealDescription;
    private String mealShowImageAddr;
    private String mealContent;
    private Integer mealType;
    private Integer mealSalesVolume;
    private LocalDateTime nextDiscountTime;
    private LocalDateTime createTime;
    private LocalDateTime deleteTime;
    private String deleteLetterId;
    private Boolean sell;
    private Boolean lock;
    private Boolean delete;
}
