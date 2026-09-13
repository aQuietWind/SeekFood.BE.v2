package com.seek.food.config.Data;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GatewayIpIdBlackData {
    private int caffeineMaxSize;
    private int caffeineExpireTime;
    private int counts;
    private int duration;
}
