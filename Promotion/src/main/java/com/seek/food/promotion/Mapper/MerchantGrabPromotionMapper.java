package com.seek.food.promotion.Mapper;

import com.seek.food.dto.Promotion.MerchantGrabPromotionDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MerchantGrabPromotionMapper {
    public void insertPromotion(MerchantGrabPromotionDTO promotion);
    public List<MerchantGrabPromotionDTO> getSimple(int start, int need, long merchantId);
    public List<MerchantGrabPromotionDTO> getSimpleEffective(int start, int need,long merchantId);
    public MerchantGrabPromotionDTO getDetail(long promotionId);
    public boolean getVoucher(long promotionId);
}
