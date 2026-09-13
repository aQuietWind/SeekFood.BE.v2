package com.seek.food.merchant.Mapper;

import com.seek.food.dto.Merchant.MerchantDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginMapper {
    public MerchantDTO login(String phoneNumber);
    public MerchantDTO loginByPassword(String phoneNumber,String password);
}
