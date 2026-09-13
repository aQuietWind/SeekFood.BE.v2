package com.seek.food.fund.Service;

import com.seek.food.dto.Fund.FundWithdrawRecordDTO;

import java.util.List;

public interface FundWithdrawRecordService {
    public List<FundWithdrawRecordDTO> getSimpleWithdrawRecord(int start, int need);
    public FundWithdrawRecordDTO getDetailWithdrawRecord(long recordId);
    public void withdraw(int rechargeAmount,String description);
}
