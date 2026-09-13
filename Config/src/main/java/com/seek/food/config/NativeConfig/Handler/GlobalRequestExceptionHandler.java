package com.seek.food.config.NativeConfig.Handler;

import com.seek.food.dto.Common.Result;
import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static feign.utils.ExceptionUtils.getRootCause;

// 仅普通Servlet微服务生效，WebFlux网关不会实例化这个类
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@RestControllerAdvice
public class GlobalRequestExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalRequestExceptionHandler.class);

    // 捕获自定义业务异常（最高优先级）
    @ExceptionHandler(BizException.class)
    public Result<?> handleBizException(BizException e, HttpServletResponse response) {
        ErrorCodeEnum codeEnum = e.getErrorCode();
        return Result.error(codeEnum,response);
    }

    // 参数校验异常
    @ExceptionHandler({IllegalArgumentException.class,IllegalStateException.class, MethodArgumentTypeMismatchException.class})
    public Result<?> handleParamError(Exception e, HttpServletResponse response) {
        return Result.error(ErrorCodeEnum.PARAM_ERROR,response);
    }

    //路径不合法异常
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<?> handlePathException(Exception e, HttpServletResponse response) {
        return Result.error(ErrorCodeEnum.BAD_REQUEST_PATH,response);
    }

    //数据插入冲突异常
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<?> handleUniqueKeyError(DuplicateKeyException e, HttpServletResponse response) {
        return Result.error(ErrorCodeEnum.DATA_SURVIVE,response);
    }

    //方法模式不合法异常
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<?> handleMethodException(Exception e, HttpServletResponse response) {
        return Result.error(ErrorCodeEnum.METHOD_NOT_ALLOW,response);
    }

    // 系统异常
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeError(Exception e, HttpServletResponse response){
        // 判断是不是Seata包装出来的异常,这是Seata自动封装异常的坑，会影响正常的异常捕获
        if (e.getMessage() != null && e.getMessage().contains("try to proceed invocation error")) {
            Throwable root = getRootCause(e);
            if(root instanceof BizException realEx){
                //交由业务异常处理方法
                return handleBizException(realEx,response);
            }
        }
        // 打印完整堆栈日志，用于线上排查
        log.error("服务发生运行时系统异常",e);
        return Result.error(ErrorCodeEnum.SERVER_ERROR,response);
    }
    // 系统未知兜底异常（500）
    @ExceptionHandler(Exception.class)
    public Result<?> handleServerError(Exception e, HttpServletResponse response) {
        // 打印完整堆栈日志，用于线上排查
        log.error("服务发生未知系统异常",e);
        return Result.error(ErrorCodeEnum.SERVER_ERROR,response);
    }






}