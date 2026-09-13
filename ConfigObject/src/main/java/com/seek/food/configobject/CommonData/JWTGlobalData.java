package com.seek.food.configobject.CommonData;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JWTGlobalData {
    private String tokenHeaderSeparator;
    private String requestHeaderTokenName;
    private String requestHeaderTokenIdName;
    private String maxStore;
}
