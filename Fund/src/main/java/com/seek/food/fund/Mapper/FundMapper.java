package com.seek.food.fund.Mapper;

import com.seek.food.dto.Fund.FundDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FundMapper {
    public boolean insertFund(long accountId);
    public FundDTO getFund(long accountId);
    public boolean deleteFund(long accountId);
    public boolean decreaseFund(long accountId,double cost);
    public boolean increaseFund(long accountId,double rechargeAmount);
}
