package com.seek.food.promotion.Service;

import com.seek.food.dto.Promotion.MerchantGrabPromotionDTO;

import java.util.List;

public interface MerchantGrabPromotionService {
    public void insertPromotion(MerchantGrabPromotionDTO promotion);
    public List<MerchantGrabPromotionDTO> getSimple(int start, int need, long merchantId);
    public List<MerchantGrabPromotionDTO> getSimpleEffective(int start, int need,long merchantId);
    public MerchantGrabPromotionDTO getDetail(long promotionId);
    public void getVoucher(long promotionId);
}
