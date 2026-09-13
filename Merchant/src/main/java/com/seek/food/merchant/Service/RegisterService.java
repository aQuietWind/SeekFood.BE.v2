package com.seek.food.merchant.Service;


public interface RegisterService {
    public String getRegisterOpt(String phoneNumber);
    public void toRegister(String phoneNumber,String opt,String password);
}
