package com.seek.food.dto.Admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SuggestionDTO {
    private Long suggestionId;
    private Long accountId;
    private String suggestionImageAddr;
    private String suggestionDescription;
    private LocalDateTime createTime;
    private Boolean ack;
}
