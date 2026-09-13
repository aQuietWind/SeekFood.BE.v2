package com.seek.food.redisutil.Redis;

import com.seek.food.configobject.RedisData.RedisKeyData;
import com.seek.food.configobject.UtilObject.Function.RunFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

@Slf4j
public class RedisBitMapUtil {


    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public RedisBitMapUtil(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    //全局BitMap设置值,offsetMin是为了适应范围型offset
    public boolean globalSetIdBit(RedisKeyData key, long id, boolean value, long idCapacity, int areaNumber){
        //最初偏移值，也就是不分区时的偏移值
        long originBitOffset=(id % idCapacity);
        //分区号数
        long area= originBitOffset/(idCapacity/areaNumber);
        //最终偏移值
        long offset=originBitOffset%areaNumber;
        //注意返回值是原来的位置状态
        Boolean originBit=stringRedisTemplate.opsForValue().setBit(key.getRedisKey(area),offset, value);
        if (originBit==null)return false;
        return value!=originBit;
    }

    //设置BitMap值成功后执行函数
    public void globalSetIdBitAndAct(RedisKeyData key, long id, boolean value, long idCapacity,int areaNumber, RunFunction runFunction){
        if (globalSetIdBit(key,id,value,idCapacity,areaNumber)) runFunction.run();
    }

    //全局BitMap获取值
    public boolean globalGetIdBit(RedisKeyData key,long id,long idCapacity,int areaNumber){
        //最初偏移值，也就是不分区时的偏移值
        long originBitOffset=(id % idCapacity);
        //分区号数
        long area= originBitOffset/(idCapacity/areaNumber);
        //最终偏移值
        long offset=originBitOffset%areaNumber;
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().getBit(key.getRedisKey(area),offset));
    }

    //个体BitMap设置值
    public boolean setBit(RedisKeyData key,Object id,boolean value){
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setBit(key.getRedisKey(id), 0, value));
    }

    //个体BitMap获取值
    public boolean getBit(RedisKeyData key,Object id){
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().getBit(key.getRedisKey(id), 0));
    }

    //全局BitMap设置值,每X个大小，就进行一次分区
    public boolean globalSetIdBitWithPerX(RedisKeyData key,long id,boolean value,long idCapacity,int X){
        //最初偏移值，也就是不分区时的偏移值
        long originBitOffset=(id % idCapacity);
        //分区号数
        long area= originBitOffset/X;
        //最终偏移值
        long offset=originBitOffset%X;
        //注意返回值是原来的位置状态
        Boolean originBit=stringRedisTemplate.opsForValue().setBit(key.getRedisKey(area),offset, value);
        if (originBit==null)return false;
        return value!=originBit;
    }

    //全局BitMap获取值
    public boolean globalGetIdBitWithPerX(RedisKeyData key,long id,long idCapacity,int X){
        //最初偏移值，也就是不分区时的偏移值（去除了首号标识）
        long originBitOffset=(id % idCapacity);
        //分区号数
        long area= originBitOffset/X;
        //最终偏移值
        long offset=originBitOffset%X;
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().getBit(key.getRedisKey(area),offset));
    }
}
