package com.seek.food.fund.Controller;

import com.seek.food.dto.Common.Result;
import com.seek.food.dto.Fund.FundRechargeRecordDTO;
import com.seek.food.fund.Enum.RequestPathEnum;
import com.seek.food.fund.Service.FundRechargeRecordService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping(RequestPathEnum.Fund_Recharge_Record)
public class FundRechargeRecordController {

    private final FundRechargeRecordService fundRechargeRecordService;

    public FundRechargeRecordController(FundRechargeRecordService fundRechargeRecordService) {
        this.fundRechargeRecordService = fundRechargeRecordService;
    }

    @GetMapping(RequestPathEnum.Fund_Recharge_Get_Simple)
    public Result<List<FundRechargeRecordDTO>> getSimpleRechargeRecord(int start, int need){
        return Result.success(fundRechargeRecordService.getSimpleRechargeRecord(start,need));
    }


    @GetMapping(RequestPathEnum.Fund_Recharge_Get_Detail)
    public Result<FundRechargeRecordDTO> getDetailRechargeRecord(long recordId){
        return Result.success(fundRechargeRecordService.getDetailRechargeRecord(recordId));
    }


    @PutMapping
    public Result<Void> recharge(int rechargeAmount,String description){
        fundRechargeRecordService.recharge(rechargeAmount,description);
        return Result.success();
    }
































}