package com.seek.food.admin.Controller;

import com.seek.food.admin.Enum.RequestPathEnum;
import com.seek.food.admin.Service.LoginService;
import com.seek.food.dto.Common.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(RequestPathEnum.Admin_Login)
@RestController
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @GetMapping
    public Result<Void> login(String name, String password, HttpServletResponse response) {
        loginService.login(name, password, response);
        return Result.success();
    }
}
