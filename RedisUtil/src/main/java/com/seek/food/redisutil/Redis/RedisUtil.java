package com.seek.food.redisutil.Redis;

import com.seek.food.configobject.RedisData.RedisKeyData;
import com.seek.food.configobject.UtilObject.Exception.BizException;
import com.seek.food.configobject.UtilObject.Exception.ErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Arrays;
import java.util.List;


@Slf4j
@Lazy
public class RedisUtil {
    public static final String cooldownValue="true";

    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public RedisUtil(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public DefaultRedisScript<Boolean> luaQuickInit(String path){
        //初始化脚本对象
        DefaultRedisScript<Boolean> luaScript= new DefaultRedisScript<>();
        luaScript.setLocation(new ClassPathResource(path));  //设置Lua脚本地址，一般放于resources/Lua下
        luaScript.setResultType(Boolean.class);      //设置脚本返回值，与泛型保持一致
        return luaScript;
    };
    //用于满足lua脚本的集合化key操作
    public List<String> toCollect(String ... items){
        return Arrays.asList(items);
    }

    //快速鉴别是否处于冷却期
    public void checkCooldown(RedisKeyData key, Object id){
        if (Boolean.FALSE.equals(stringRedisTemplate.opsForValue().setIfAbsent(
                key.getRedisKey(id)
                , cooldownValue,
                DurationUtil.getSecondDuration(key.getDurationSeconds()) ))) throw new BizException(ErrorCodeEnum.REQUEST_IN_COOLDOWN);
    }

    //快速设置
    public boolean trySetStringWithExpire(RedisKeyData key,Object id,String value){
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key.getRedisKey(id), value, DurationUtil.getSecondDuration(key.getDurationSeconds())));
    }

    //快速设置
    public boolean trySetString(RedisKeyData key,Object id,String value){
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key.getRedisKey(id), value));
    }

    //快速设置
    public void justSetStringWithExpire(RedisKeyData key,Object id,String value){
        stringRedisTemplate.opsForValue().set(key.getRedisKey(id), value,DurationUtil.getSecondDuration(key.getDurationSeconds()));
    }


    //快速获取
    public String getString(RedisKeyData key,Object id){
        return stringRedisTemplate.opsForValue().get(key.getRedisKey(id));
    }

    //快速删除
    public boolean delete(RedisKeyData key,Object id){
        return stringRedisTemplate.delete(key.getRedisKey(id));
    }

    //快速自增
    public Long increase(RedisKeyData key,Object id,int value){
        return stringRedisTemplate.opsForValue().increment(key.getRedisKey(id), value);
    }

    //快速设置过期
    public boolean expire(RedisKeyData key,Object id){
        return Boolean.TRUE.equals(stringRedisTemplate.expire(key.getRedisKey(id),DurationUtil.getSecondDuration(key.getDurationSeconds())));
    }

    //快速检查zset中某个value值是否存在
    public boolean zSetXIsExistByScore(RedisKeyData key,Object id,String value){
        return stringRedisTemplate.opsForZSet().score(key.getRedisKey(id), value)!=null;
    }








}
