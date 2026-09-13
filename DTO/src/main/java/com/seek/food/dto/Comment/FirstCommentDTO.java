package com.seek.food.dto.Comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class FirstCommentDTO {
    private Long firstCommentId;
    private Long userId;
    private Long orderId;
    private Long mealId;
    private String mealName;
    private String mealContent;
    private Long merchantId;
    private String commentImageAddr;
    private String commentDescription;
    private LocalDateTime createTime;
    private Integer likeAmount;
    private Integer secondCommentAmount;
    private Boolean delete;
}
