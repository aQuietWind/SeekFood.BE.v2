package com.seek.food.rider.Controller;


import com.seek.food.dto.Common.Result;
import com.seek.food.dto.Rider.RiderDTO;
import com.seek.food.rider.Enum.RequestPathEnum;
import com.seek.food.rider.Service.LoginService;
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
    public Result<String> loginGetOpt(String phoneNumber) {
        return Result.success(loginService.loginGetOpt(phoneNumber));
    }


    @GetMapping
    public Result<RiderDTO> login(String phoneNumber, String opt, HttpServletResponse response) {
        return Result.success(loginService.login(phoneNumber,opt,response));
    }

    @GetMapping(RequestPathEnum.Login_Password)
    public Result<RiderDTO> loginPassword(String phoneNumber, String password, HttpServletResponse response) {
        return Result.success(loginService.loginByPassword(phoneNumber,password,response));
    }

    @GetMapping(RequestPathEnum.Login_Refresh)
    public Result loginRefresh(HttpServletResponse response) {
        loginService.loginRefresh(response);
        return Result.success();
    }











}
