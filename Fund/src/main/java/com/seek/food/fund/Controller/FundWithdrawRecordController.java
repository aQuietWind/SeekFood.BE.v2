package com.seek.food.fund.Controller;

import com.seek.food.dto.Common.Result;
import com.seek.food.dto.Fund.FundWithdrawRecordDTO;
import com.seek.food.fund.Enum.RequestPathEnum;
import com.seek.food.fund.Service.FundWithdrawRecordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping(RequestPathEnum.Fund_Withdraw_Record)
public class FundWithdrawRecordController {

    private final FundWithdrawRecordService fundWithdrawRecordService;

    public FundWithdrawRecordController(FundWithdrawRecordService fundWithdrawRecordService) {
        this.fundWithdrawRecordService = fundWithdrawRecordService;
    }

    @GetMapping(RequestPathEnum.Fund_Withdraw_Get_Simple)
    public Result<List<FundWithdrawRecordDTO>> getSimpleWithdrawRecord(int start, int need){
        return Result.success(fundWithdrawRecordService.getSimpleWithdrawRecord(start,need));
    }


    @GetMapping(RequestPathEnum.Fund_Withdraw_Get_Detail)
    public Result<FundWithdrawRecordDTO> getDetailWithdrawRecord(long recordId){
        return Result.success(fundWithdrawRecordService.getDetailWithdrawRecord(recordId));
    }


    @PutMapping
    public Result<Void> withdraw(int withdrawAmount,String description){
        fundWithdrawRecordService.withdraw(withdrawAmount,description);
        return Result.success();
    }


}