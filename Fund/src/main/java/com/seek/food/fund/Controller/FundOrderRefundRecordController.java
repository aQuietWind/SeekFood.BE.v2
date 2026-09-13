package com.seek.food.fund.Controller;

import com.seek.food.dto.Common.Result;
import com.seek.food.dto.Fund.FundOrderRecordDTO;
import com.seek.food.dto.Fund.FundOrderRefundRecordDTO;
import com.seek.food.fund.Enum.RequestPathEnum;
import com.seek.food.fund.Service.FundOrderRecordService;
import com.seek.food.fund.Service.FundOrderRefundRecordService;
import com.seek.food.fund.Service.FundService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping(RequestPathEnum.Fund_Order_Refund_Record)
public class FundOrderRefundRecordController {


    private final FundOrderRefundRecordService fundOrderRefundRecordService;

    public FundOrderRefundRecordController(FundOrderRefundRecordService fundOrderRecordService) {
        this.fundOrderRefundRecordService = fundOrderRecordService;
    }

    @GetMapping(RequestPathEnum.Fund_Order_Refund_Record_Get_Simple)
    public Result<List<FundOrderRefundRecordDTO>> getSimple(int start , int need){
        return Result.success(fundOrderRefundRecordService.getSimple(start, need));
    }



}