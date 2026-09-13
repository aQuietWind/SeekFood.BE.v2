package com.seek.food.fund.Service;


import com.seek.food.dto.Fund.FundRechargeRecordDTO;

import java.util.List;

public interface FundRechargeRecordService {
    public List<FundRechargeRecordDTO> getSimpleRechargeRecord(int start, int need);
    public FundRechargeRecordDTO getDetailRechargeRecord(long recordId);
    public void recharge(int rechargeAmount,String description);
}