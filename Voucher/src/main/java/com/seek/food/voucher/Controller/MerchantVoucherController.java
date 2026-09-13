package com.seek.food.voucher.Controller;

import com.seek.food.dto.Common.Result;
import com.seek.food.dto.Voucher.MerchantVoucherDTO;
import com.seek.food.voucher.Enum.RequestPathEnum;
import com.seek.food.voucher.Service.MerchantVoucherService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(RequestPathEnum.Merchant_Voucher)
public class MerchantVoucherController {

    private final MerchantVoucherService merchantVoucherService;

    public MerchantVoucherController(MerchantVoucherService merchantVoucherService) {
        this.merchantVoucherService = merchantVoucherService;
    }

    //新增优惠券
    @PostMapping
    public Result<Void> insertMerchantVoucher(@RequestBody MerchantVoucherDTO voucher) {
        merchantVoucherService.insertMerchantVoucher(voucher);
        return Result.success();
    }

    //获取简易信息
    @GetMapping(RequestPathEnum.Merchant_Voucher_Get_Simple)
    public Result<List<MerchantVoucherDTO>> getSimple(int start, int need) {
        return Result.success(merchantVoucherService.getSimple(start, need));
    }

    //获取还处于有效期中的商家优惠券简易信息
    @GetMapping(RequestPathEnum.Merchant_Voucher_Get_Simple_Effective)
    public Result<List<MerchantVoucherDTO>> getSimpleEffective(int start, int need) {
        return Result.success(merchantVoucherService.getSimpleEffective(start, need));
    }

    //获取还处于有效期中的商家优惠券简易信息
    @GetMapping(RequestPathEnum.Merchant_Voucher_Get_Detail)
    public Result<MerchantVoucherDTO> getSimpleEffective(long voucherId) {
        return Result.success(merchantVoucherService.getDetail(voucherId));
    }

    //通过Feign检测某商家是否真的有某优惠券
    @GetMapping(RequestPathEnum.Merchant_Voucher_Exist)
    public Result<Boolean> exist(long voucherId) {
        return Result.success(merchantVoucherService.exist(voucherId));
    }















}
