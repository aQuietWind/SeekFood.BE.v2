package com.seek.food.rider.Service;

import com.seek.food.dto.Rider.RiderDTO;
import com.seek.food.dto.User.UserDTO;
import jakarta.servlet.http.HttpServletResponse;

public interface LoginService {
    public String loginGetOpt(String phoneNumber);
    public RiderDTO login(String phoneNumber, String opt, HttpServletResponse response);
    public RiderDTO loginByPassword(String phoneNumber, String password, HttpServletResponse response);
    public void loginRefresh(HttpServletResponse response);
}
