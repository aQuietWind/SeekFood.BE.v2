package com.seek.food.rider.Mapper;

import com.seek.food.dto.Rider.RiderDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginMapper {
    public RiderDTO loginByPhone(String phoneNumber);
    public RiderDTO loginByPassword(String phoneNumber,String password);
}
