package com.seek.food.promotion.Controller;

import com.seek.food.dto.Common.Result;
import com.seek.food.dto.Promotion.MerchantLoginPromotionDTO;
import com.seek.food.promotion.Enum.RequestPathEnum;
import com.seek.food.promotion.Service.MerchantLoginPromotionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(RequestPathEnum.Merchant_Login_Promotion)
@RestController
public class MerchantLoginPromotionController {

    private final MerchantLoginPromotionService merchantLoginPromotionService;

    public MerchantLoginPromotionController(MerchantLoginPromotionService merchantLoginPromotionService) {
        this.merchantLoginPromotionService = merchantLoginPromotionService;
    }

    //新增活动
    @PostMapping
    public Result<Void> insertPromotion(@RequestBody MerchantLoginPromotionDTO promotion) {
        merchantLoginPromotionService.insertPromotion(promotion);
        return Result.success();
    }

    //获取某商家的简易活动介绍
    @GetMapping(RequestPathEnum.Merchant_Login_Promotion_Get_Simple)
    public Result<List<MerchantLoginPromotionDTO>> getSimple(int start,int need,long merchantId) {
        return Result.success(merchantLoginPromotionService.getSimple(start, need, merchantId));
    }

    //获取某商家还在有效期的简易活动介绍
    @GetMapping(RequestPathEnum.Merchant_Login_Promotion_Get_Simple_Effective)
    public Result<List<MerchantLoginPromotionDTO>> getSimpleEffective(int start,int need,long merchantId) {
        return Result.success(merchantLoginPromotionService.getSimpleEffective(start, need, merchantId));
    }

    //获取详细的活动信息
    @GetMapping(RequestPathEnum.Merchant_Login_Promotion_Get_Detail)
    public Result<MerchantLoginPromotionDTO> getDetail(long promotionId) {
        return Result.success(merchantLoginPromotionService.getDetail(promotionId));
    }

    //通过活动获取优惠券
    @GetMapping(RequestPathEnum.Merchant_Login_Promotion_Get_Voucher)
    public Result<Void> getVoucher(long promotionId) {
        merchantLoginPromotionService.getVoucher(promotionId);
        return Result.success();
    }















}
