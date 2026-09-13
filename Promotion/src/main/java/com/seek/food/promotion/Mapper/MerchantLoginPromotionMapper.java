package com.seek.food.promotion.Mapper;

import com.seek.food.dto.Promotion.MerchantLoginPromotionDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MerchantLoginPromotionMapper {
    public void insertPromotion(MerchantLoginPromotionDTO promotion);
    public List<MerchantLoginPromotionDTO> getSimple(int start, int need, long merchantId);
    public List<MerchantLoginPromotionDTO> getSimpleEffective(int start, int need,long merchantId);
    public MerchantLoginPromotionDTO getDetail(long promotionId);
    public boolean getVoucher(long promotionId);
}
