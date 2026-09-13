package com.seek.food.util.Exception;

import org.springframework.http.HttpStatus;

public enum ErrorCodeEnum {
    //请求自身异常
    PARAM_ERROR(14101,HttpStatus.BAD_REQUEST, "参数错误"),
    DATA_NOT_FOUND(14102,HttpStatus.NOT_FOUND, "未查询到目标数据"),
    METHOD_NOT_ALLOW(14103,HttpStatus.METHOD_NOT_ALLOWED,  "请求方法不允许"),
    BAD_REQUEST_PATH(14104,HttpStatus.NOT_FOUND,"请求路径无法达到"),
    DATA_SURVIVE(14105,HttpStatus.CONFLICT,"目标数据已存在"),
    CONDITION_NOT_PASS(14106,HttpStatus.BAD_REQUEST,"该请求不符合条件"),
    DATA_NOT_RIGHT(14107,HttpStatus.BAD_REQUEST,"目标数据不正常"),
    REQUEST_NOT_QUALIFIED(14108,HttpStatus.BAD_REQUEST,"该请求没有资格"),
    DATA_IS_EXPIRE(14109,HttpStatus.BAD_REQUEST,"请求数据已过期"),
    //验证码异常,
    OPT_SURVIVE(14201,HttpStatus.LOCKED,"验证码已存在"),
    OPT_NOT_SURVIVE(14202,HttpStatus.NOT_FOUND,"验证码不存在"),
    OPT_NOT_SAME(14203,HttpStatus.BAD_REQUEST,"验证码不一致"),
    //文件异常,
    TOO_BIG_FILE(14301,HttpStatus.PAYLOAD_TOO_LARGE,"文件过大"),
    ERROR_FILE_TYPE(14302,HttpStatus.BAD_REQUEST,"文件格式错误"),
    FILE_NOT_EXIST(14303,HttpStatus.NOT_FOUND,"目标文件不存在"),
    FILE_DELETE_ERROR(14304,HttpStatus.INTERNAL_SERVER_ERROR,"文件删除失败"),
    FILE_IS_EMPTY(14305,HttpStatus.BAD_REQUEST,"该文件为空"),
    ERROR_FILE_NAME(14306,HttpStatus.BAD_REQUEST,"错误文件名"),
    //请求异常,
    UNAUTHORIZED(14401,HttpStatus.UNAUTHORIZED, "身份验证失败"),
    ACCOUNT_FORBIDDEN(14402,HttpStatus.FORBIDDEN, "账号已被封禁"),
    REQUEST_IN_COOLDOWN(14403,HttpStatus.TOO_MANY_REQUESTS,"请求冷却中"),
    FLOW_REQUEST(14404,HttpStatus.TOO_MANY_REQUESTS, "请求被限流"),
    PARAM_FLOW_REQUEST(14405,HttpStatus.TOO_MANY_REQUESTS, "请求被热点参数限流"),
    DEGRADE_REQUEST(14406,HttpStatus.TOO_MANY_REQUESTS, "请求被熔断降级"),

    //服务异常
    SERVER_ERROR(15101,HttpStatus.INTERNAL_SERVER_ERROR, "服务器内部异常"),
    DOWNSTREAM_UNAVAILABLE(15102,HttpStatus.BAD_GATEWAY, "下游服务暂时不可用"),
    SERVICE_TIMEOUT(15103,HttpStatus.GATEWAY_TIMEOUT,"下游服务请求超时"),
    SERVICE_SHUTDOWN(15104, HttpStatus.SERVICE_UNAVAILABLE, "服务暂不可用，请稍后重试");

    //http标准码
    private final HttpStatus httpStatus;
    //业务错误码（对外返回给前端）
    private final Integer code;
    //默认提示文案
    private final String defaultMsg;

    ErrorCodeEnum(Integer code,HttpStatus httpStatus,  String defaultMsg) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultMsg = defaultMsg;
    }

    // getter
    public Integer getCode() { return code; }
    public String getDefaultMsg() { return defaultMsg; }
    public HttpStatus getHttpStatus() { return httpStatus; }
}
