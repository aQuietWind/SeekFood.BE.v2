package com.seek.food.util.Exception;

public class BizException extends RuntimeException{
    // 绑定错误码枚举
    private final ErrorCodeEnum errorCode;

    // 传错误枚举，使用默认文案
    public BizException(ErrorCodeEnum errorCode) {
        this.errorCode = errorCode;
    }

    public ErrorCodeEnum getErrorCode() {
        return errorCode;
    }
}
