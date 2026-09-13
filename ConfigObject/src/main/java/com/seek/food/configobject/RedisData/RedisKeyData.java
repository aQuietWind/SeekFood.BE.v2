package com.seek.food.configobject.RedisData;

import lombok.Data;

@Data
public class RedisKeyData {
    private String name;
    private Long durationSeconds;
    public String getRedisKey(Object key){
        if (key==null||key.equals("")) return name;
        return name+key;
    }
}
