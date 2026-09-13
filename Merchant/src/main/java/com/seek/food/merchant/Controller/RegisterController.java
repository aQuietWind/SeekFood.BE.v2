package com.seek.food.merchant.Controller;

import com.seek.food.dto.Common.Result;
import com.seek.food.merchant.Enum.RequestPathEnum;
import com.seek.food.merchant.Service.RegisterService;
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
    private  RegisterController(RegisterService registerService) {
        this.registerService = registerService;
    }
    @GetMapping(RequestPathEnum.Register_Opt)
    public Result<String> getRegisterOpt(String phoneNumber){
        return Result.success(registerService.getRegisterOpt(phoneNumber));
    }
    @PostMapping
    public Result<Void> toRegister(String phoneNumber,String opt,String password){
        registerService.toRegister(phoneNumber,opt,password);
        return Result.success();
    }
}
