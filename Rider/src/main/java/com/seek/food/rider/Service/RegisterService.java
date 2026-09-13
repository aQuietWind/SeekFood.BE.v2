package com.seek.food.rider.Service;

import com.seek.food.dto.Rider.RiderDTO;

public interface RegisterService {
    public String registerGetOpt(String phoneNumber);
    public void registerRider(RiderDTO rider, String opt);
}




