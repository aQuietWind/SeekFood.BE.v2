package com.seek.food.redisutil.Redis;

import com.seek.food.configobject.RedisData.RedisKeyData;
import com.seek.food.configobject.UtilObject.Exception.BizException;
import com.seek.food.configobject.UtilObject.Exception.ErrorCodeEnum;
import com.seek.food.configobject.UtilObject.Function.RunFunction;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

public class RedissonUtil {

    private final RedissonClient redissonClient;
    public RedissonUtil(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public void lock(RedisKeyData key, Object id, RunFunction function) {
        RLock lock = redissonClient.getLock(key.getRedisKey(id));
        if (lock.tryLock()){
            try {
                function.run();
                return;
            }finally {
                lock.unlock();
            }
        }
        throw new BizException(ErrorCodeEnum.REQUEST_CONFLICT);
    }
}
