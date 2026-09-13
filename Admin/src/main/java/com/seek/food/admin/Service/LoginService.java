package com.seek.food.admin.Service;

import jakarta.servlet.http.HttpServletResponse;

public interface LoginService {
    public void login(String name, String password, HttpServletResponse response);
}
