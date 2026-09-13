package com.seek.food.redisutil.Redis;

import java.time.Duration;

public class DurationUtil {
    public static Duration getSecondDuration(long duration) {
        return Duration.ofSeconds(duration);
    }
}
