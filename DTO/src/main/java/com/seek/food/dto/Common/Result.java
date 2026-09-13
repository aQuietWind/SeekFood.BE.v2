package com.seek.food.dto.Common;


import com.seek.food.util.Exception.ErrorCodeEnum;
import jakarta.servlet.http.HttpServletResponse;

public class Result<T> {
    private  int code;
    private String msg;
    private T data;
    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public Result() {
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }
    public static <T> Result<T> success() {
        return new Result<>(200, "success",null);
    }
    public static <T> Result<T> error(int code,String msg) {
        return new Result<T>(code, msg, null);
    }
    public static <T> Result<T> error(ErrorCodeEnum errorCodeEnum) {
        return new Result(errorCodeEnum.getCode(),errorCodeEnum.getDefaultMsg(),null);
    }
    public static <T> Result<T> error(ErrorCodeEnum errorCodeEnum, HttpServletResponse response) {
        response.setStatus(errorCodeEnum.getHttpStatus().value());
        return new Result(errorCodeEnum.getCode(),errorCodeEnum.getDefaultMsg(),null);
    }


    /**
     * 获取
     * @return code
     */
    public int getCode() {
        return code;
    }

    /**
     * 设置
     * @param code
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * 获取
     * @return msg
     */
    public String getMsg() {
        return msg;
    }

    /**
     * 设置
     * @param msg
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * 获取
     * @return data
     */
    public T getData() {
        return data;
    }

    /**
     * 设置
     * @param data
     */
    public void setData(T data) {
        this.data = data;
    }

    public String toString() {
        return "Result{code = " + code + ", msg = " + msg + ", data = " + data + "}";
    }
}
