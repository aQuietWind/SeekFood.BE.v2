package com.seek.food.util.JWT;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenCheckResult {
    String token;
    long resultId;
}