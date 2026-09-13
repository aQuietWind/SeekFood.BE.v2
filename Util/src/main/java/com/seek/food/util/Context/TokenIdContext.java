package com.seek.food.util.Context;


import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;

import java.util.function.Function;

public class TokenIdContext {
    public static ThreadLocal<String> threadLocal = new ThreadLocal<>();
    public static String get() {
        return threadLocal.get();       //获取数据
    }
    public static void set(String tokenId) {       //Token是手写的辅助类
        threadLocal.set(tokenId);         //设置该线程的存储数据
    }
    public static void remove() {
        threadLocal.remove();           //删除该实例的数据，方便下次set
    }
    public static long getAndToLong() {
        return Long.parseLong(threadLocal.get());
    }
    public static boolean compareIsSame(long id) {
        return id==getAndToLong();
    }
    public static long getAndCheck(int idStart,long idCapacity) {
        long tokenId=getAndToLong();
        if (! ((tokenId/idCapacity)==idStart) ) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
        return tokenId;
    }
}


