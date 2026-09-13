package com.seek.food.merchant.Controller;

import com.seek.food.dto.Common.Result;
import com.seek.food.dto.Merchant.MerchantDTO;
import com.seek.food.merchant.Enum.RequestPathEnum;
import com.seek.food.merchant.Service.LoginService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RequestPathEnum.Login)
public class LoginController {
    private final LoginService loginService;
    @Autowired
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }
    @GetMapping(RequestPathEnum.Login_Opt)
    public Result<String> getLoginOpt(String phoneNumber){
        return Result.success(loginService.getLoginOpt(phoneNumber));
    }
    @GetMapping
    public Result<MerchantDTO> login(String phoneNumber, String opt, HttpServletResponse httpServletResponse){
        return Result.success(loginService.login(phoneNumber,opt,httpServletResponse));
    }

    @GetMapping(RequestPathEnum.Login_Password)
    public Result<MerchantDTO> loginByPassword(String phoneNumber,String password,HttpServletResponse httpServletResponse){
        return Result.success(loginService.loginByPassword(phoneNumber,password,httpServletResponse));
    }
    @GetMapping(RequestPathEnum.Login_Refresh)
    public Result<MerchantDTO> loginRefresh(HttpServletResponse httpServletResponse){
        loginService.loginRefresh(httpServletResponse);
        return Result.success();
    }
}
