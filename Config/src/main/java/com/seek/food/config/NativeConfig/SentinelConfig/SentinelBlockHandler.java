package com.seek.food.config.NativeConfig.SentinelConfig;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seek.food.dto.Common.Result;
import com.seek.food.util.Exception.ErrorCodeEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import java.io.IOException;

//用于请求被Sentinel拒绝时返回可视化信息
@Component
// 仅普通Servlet微服务生效，WebFlux网关不会实例化这个类
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SentinelBlockHandler implements BlockExceptionHandler {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void handle(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            BlockException e) throws Exception {
        ErrorCodeEnum errorCodeEnum = switch (e) {
            case FlowException ignored -> ErrorCodeEnum.FLOW_REQUEST;
            case DegradeException ignored -> ErrorCodeEnum.DEGRADE_REQUEST;
            case ParamFlowException ignored -> ErrorCodeEnum.PARAM_FLOW_REQUEST;
            case AuthorityException ignored -> ErrorCodeEnum.UNAUTHORIZED;
            case null, default -> ErrorCodeEnum.SERVER_ERROR;
        };
        responseHandler(errorCodeEnum,httpServletResponse);
    }

    private void responseHandler(ErrorCodeEnum errorCode,HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.setContentType("application/json;charset=utf-8");
        httpServletResponse.setStatus(errorCode.getCode());
        httpServletResponse.getWriter().println(objectMapper.writeValueAsString(Result.error(errorCode)));
    }
}
