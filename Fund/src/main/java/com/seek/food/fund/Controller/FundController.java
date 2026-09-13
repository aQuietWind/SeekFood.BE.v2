package com.seek.food.fund.Controller;

import com.seek.food.dto.Common.Result;
import com.seek.food.dto.Fund.FundDTO;
import com.seek.food.fund.Enum.RequestPathEnum;
import com.seek.food.fund.Service.FundService;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(RequestPathEnum.Fund)
public class FundController {
    private final FundService fundService;
    public FundController(FundService fundService) {
        this.fundService = fundService;
    }

    //查看余额
    @GetMapping
    public Result<FundDTO> getMessage() {
        return Result.success(fundService.getFund());
    }

    //如果资金用户不存在，则可以手动创建，应对消息丢失的情况
    @PostMapping
    public Result<Void> insertFund() {
        fundService.insertFund();
        return Result.success();
    }






}