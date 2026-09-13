package com.seek.food.promotion.Service;

import com.seek.food.dto.Promotion.MerchantLoginPromotionDTO;

import java.util.List;

public interface MerchantLoginPromotionService {
    public void insertPromotion(MerchantLoginPromotionDTO promotion);
    public List<MerchantLoginPromotionDTO> getSimple(int start, int need,long merchantId);
    public List<MerchantLoginPromotionDTO> getSimpleEffective(int start, int need,long merchantId);
    public MerchantLoginPromotionDTO getDetail(long promotionId);
    public void getVoucher(long promotionId);

}
