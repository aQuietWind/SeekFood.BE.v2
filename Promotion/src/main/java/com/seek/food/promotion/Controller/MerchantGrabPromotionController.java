package com.seek.food.promotion.Controller;

import com.seek.food.dto.Common.Result;
import com.seek.food.dto.Promotion.MerchantGrabPromotionDTO;
import com.seek.food.promotion.Enum.RequestPathEnum;
import com.seek.food.promotion.Service.MerchantGrabPromotionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(RequestPathEnum.Merchant_Grab_Promotion)
@RestController
public class MerchantGrabPromotionController {

    private final MerchantGrabPromotionService merchantGrabPromotionService;

    public MerchantGrabPromotionController(MerchantGrabPromotionService merchantGrabPromotionService) {
        this.merchantGrabPromotionService = merchantGrabPromotionService;
    }

    //新增活动
    @PostMapping
    public Result<Void> insertPromotion(@RequestBody MerchantGrabPromotionDTO promotion) {
        merchantGrabPromotionService.insertPromotion(promotion);
        return Result.success();
    }

    //获取某商家的简易活动介绍
    @GetMapping(RequestPathEnum.Merchant_Grab_Promotion_Get_Simple)
    public Result<List<MerchantGrabPromotionDTO>> getSimple(int start, int need, long merchantId) {
        return Result.success(merchantGrabPromotionService.getSimple(start, need, merchantId));
    }

    //获取某商家还在有效期的简易活动介绍
    @GetMapping(RequestPathEnum.Merchant_Grab_Promotion_Get_Simple_Effective)
    public Result<List<MerchantGrabPromotionDTO>> getSimpleEffective(int start,int need,long merchantId) {
        return Result.success(merchantGrabPromotionService.getSimpleEffective(start, need, merchantId));
    }

    //获取详细的活动信息
    @GetMapping(RequestPathEnum.Merchant_Grab_Promotion_Get_Detail)
    public Result<MerchantGrabPromotionDTO> getDetail(long promotionId) {
        return Result.success(merchantGrabPromotionService.getDetail(promotionId));
    }

    //通过活动获取优惠券
    @GetMapping(RequestPathEnum.Merchant_Grab_Promotion_Get_Voucher)
    public Result<Void> getVoucher(long promotionId) {
        merchantGrabPromotionService.getVoucher(promotionId);
        return Result.success();
    }
}
