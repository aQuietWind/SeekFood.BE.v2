package com.seek.food.util.JWT;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JWTUtil {
    private static final Logger logger = LoggerFactory.getLogger(JWTUtil.class);
    public static final int FailResult=-1;
    public static final String IdName="tokenId";
    //生成令牌的方法
    public static String obtainJwt(Long tokenId,String sercetKey,long expireTime){
        Map<String,Long> dataMap = new HashMap<>();
        dataMap.put(IdName,tokenId);
        //根据字符串密钥来生成HS256形式的密钥
        SecretKey key= Keys.hmacShaKeyFor( sercetKey.getBytes(StandardCharsets.UTF_8) );
        Date usefulTime=new Date(System.currentTimeMillis()+expireTime);             //设置最终过期时间点
        return Jwts.builder()                  //通过链式编程生成字符串令牌
                .claims(dataMap)                    //设置Payload载荷数据
                .subject("seek_food")                  //设置主题，id，或者权限等等重要标识
                .issuedAt(new Date())               //设置签发时间
                .expiration(usefulTime)             //设置最终有效期为一个小时，后续可自己自定义
                .signWith(key)                      //设置该令牌密钥，自动识别HS256算法
                .compact();                         //根据上述设置生成一个令牌字符串
    }

    //解析并获取令牌内容的方法
    public static Long jwtCheck(String token,String sercetKey){
        SecretKey key= Keys.hmacShaKeyFor( sercetKey.getBytes(StandardCharsets.UTF_8) );    //根据字符串密钥生成HS256形式的密钥
            Jws<Claims> data = Jwts.parser()           //开启解析器建立
                    .verifyWith(key)                    //设置解析的密钥
                    .build()                            //根据上述建立一个解析器
                    .parseSignedClaims(token);          //通过解析器获取指定令牌的破解版
            return (Long) data.getPayload().get(IdName);       //返回载荷存储的userId
    }

    public static String obtainJwtByLong(long tokenId,String sercetKey,long expireTime){
        return obtainJwt(tokenId,sercetKey,expireTime);
    }

    public static TokenCheckResult jwtCheckByList(String token,String headerSeparator,HashMap<String,String> jwtHeaders){
        //分割token,并且赋值
        String[] body=token.split(headerSeparator,2);
        String headerSign=body[0];
        token=body[1];
        //查找对应的secretKey进行解析
        if (jwtHeaders.containsKey(headerSign))return new TokenCheckResult(token,jwtCheck(token,jwtHeaders.get(headerSign)));
        return new TokenCheckResult(token,FailResult);
    }






















}
