package com.seek.food.merchant.Service;


import com.seek.food.dto.Merchant.MerchantDTO;
import jakarta.servlet.http.HttpServletResponse;

public interface LoginService {
    public String getLoginOpt(String phoneNumber);
    public MerchantDTO login(String phoneNumber, String opt,HttpServletResponse httpServletResponse);
    public MerchantDTO loginByPassword(String phoneNumber, String password, HttpServletResponse httpServletResponse);
    public void loginRefresh(HttpServletResponse httpServletResponse);
}
