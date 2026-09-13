package com.seek.food.dto.Comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class SecondCommentDTO {
    private Long secondCommentId;
    private Long firstCommentId;
    private Long accountId;
    private String commentImageAddr;
    private String commentDescription;
    private LocalDateTime createTime;
    private Integer likeAmount;
    private Boolean merchantComment;
    private Boolean delete;
}
