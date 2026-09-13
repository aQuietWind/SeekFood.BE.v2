package com.seek.food.config.Data;

import lombok.Data;

@Data
public class RedisStreamData {
    private String name;
    private String keyName;
    private RedisConsumerData consumer;
}
