package com.seek.food.fund.Service;

import com.seek.food.dto.Fund.FundDTO;

public interface FundService {
    public FundDTO getFund();
    public void insertFund();
    public void decreaseFund(double amount,long accountId);
    public void increaseFund(double amount,long accountId);
}
