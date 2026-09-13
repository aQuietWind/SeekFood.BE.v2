package com.seek.food.rider.Controller;

import com.seek.food.dto.Common.Result;
import com.seek.food.dto.Rider.RiderDTO;
import com.seek.food.rider.Enum.RequestPathEnum;
import com.seek.food.rider.Service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public Result<Void> registerRider(@RequestBody RiderDTO rider, String opt) {
        registerService.registerRider(rider,opt);
        return Result.success(null);
    }






















}
