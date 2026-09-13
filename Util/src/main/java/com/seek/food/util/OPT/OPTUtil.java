package com.seek.food.util.OPT;

import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;
import com.seek.food.util.TimeUtil.DurationUtil;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class OPTUtil {
    public static String generateOPT(int n){
        StringBuilder opt = new StringBuilder();
        for (int i = 0; i < n; i++) {
            //添加一位随机数字
            opt.append(ThreadLocalRandom.current().nextInt(10));
        }
        return opt.toString();
    }

    //生成验证码并且写入redis中
    public static String generateOPTAndRecord(StringRedisTemplate stringRedisTemplate,String redisKey,long duration,int n){
        String opt=generateOPT(n);
        //写入redis并且判断是否已有验证码
        if (!Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(redisKey, opt, DurationUtil.getSecondDuration(duration))))throw new BizException(ErrorCodeEnum.OPT_SURVIVE);
        return opt;
    }

    public static void checkOPT(StringRedisTemplate stringRedisTemplate, String keyName, String opt){
        //获取redis的验证码
        String originOpt=stringRedisTemplate.opsForValue().get(keyName);
        //检验验证码
        if ( originOpt== null) throw new BizException(ErrorCodeEnum.OPT_NOT_SURVIVE);
        if (!originOpt.equals(opt)) throw new BizException(ErrorCodeEnum.OPT_NOT_SAME);
        stringRedisTemplate.delete(keyName);
    }
}
