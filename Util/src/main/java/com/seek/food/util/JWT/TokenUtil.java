package com.seek.food.util.JWT;


import com.seek.food.util.TimeUtil.DurationUtil;
import com.seek.food.util.TimeUtil.TimeUtil;
import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;
import com.seek.food.util.Redis.RedisUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

public class TokenUtil {
    private static final DefaultRedisScript<Boolean> tokenAddScript= RedisUtil.luaQuickInit("lua/token_add.lua");

    //统一获取token
    public static String getToken(Long id, HttpServletResponse response, String secretKey, long expireMillSecondsTime
    , String requestTokenName, String headerSign, String headerSeparator){
        //登录校验成功，生成JWT Token
        String accessToken = JWTUtil.obtainJwtByLong(id,secretKey,expireMillSecondsTime);
        // 构建Servlet Cookie
        Cookie cookie = new Cookie(requestTokenName, headerSign+headerSeparator+accessToken);
        cookie.setHttpOnly(true);
        //生产环境改为true
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge((int) (expireMillSecondsTime/1000));
        response.addCookie(cookie);
        return accessToken;
    }

    //统一进行token的获取与Redis存储
    //发放登录信息
    public static void getAndRecordToken(Long tokenId, HttpServletResponse response
    , String secretKey, long duration, String requestTokenName, String headerSign, String headerSeparator
    , String redisKeyName, String maxStore, StringRedisTemplate stringRedisTemplate){
        if (tokenId==null) throw new BizException(ErrorCodeEnum.DATA_NOT_FOUND);
        //获取token，并且放在请求头上
        String token=getToken(
                tokenId
                , response
                , secretKey
                , duration
                , requestTokenName
                , headerSign
                , headerSeparator);
        stringRedisTemplate.execute(tokenAddScript         //执行脚本
                , RedisUtil.toCollect(redisKeyName+tokenId)       //KEYS参数
                ,maxStore,token, ""+ TimeUtil.getStampByNow());     //ARGV参数
    }

}
