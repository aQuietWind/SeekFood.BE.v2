package com.seek.food.user.Controller;

import com.seek.food.dto.Common.Result;
import com.seek.food.user.Enum.RequestPathEnum;
import com.seek.food.user.Service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RequestPathEnum.Register)
public class RegisterController {
    private final RegisterService registerService;
    @Autowired
    public RegisterController(RegisterService registerService) {
        this.registerService = registerService;
    }

    @GetMapping(RequestPathEnum.Register_Opt)
    public Result<String> registerGetOpt(String phoneNumber) {
        return Result.success(registerService.registerGetOpt(phoneNumber));
    }

    @PostMapping
    public Result<String> registerUser(String phoneNumber, String password, String opt) {
        registerService.registerUser(phoneNumber,password,opt);
        return Result.success(null);
    }






















}
