package com.seek.food.rider.Mapper;

import com.seek.food.dto.Rider.RiderDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RegisterMapper {
    public void registerRider(RiderDTO rider);
}
