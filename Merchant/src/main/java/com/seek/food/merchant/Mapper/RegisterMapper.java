package com.seek.food.merchant.Mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RegisterMapper {
    public void insertMerchant(long merchantId,String phoneNumber,String password);
}
