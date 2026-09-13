package com.seek.food.config.Data;

import lombok.Data;

@Data
public class RedisKeyData {
    private String name;
    private Long duration;
    public String getRedisKey(Object key){
        return name + key;
    }
}
