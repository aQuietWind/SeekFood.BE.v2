package com.seek.food.config.Data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JWTData {
    private String secretKey;
    private String headerSign;
    private long tokenDuration;
}
