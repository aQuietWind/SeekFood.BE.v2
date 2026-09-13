package com.seek.food.configobject.CommonData;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JWTRoleData {
    private String secretKey;
    private String headerSign;
    private long tokenDuration;
}
