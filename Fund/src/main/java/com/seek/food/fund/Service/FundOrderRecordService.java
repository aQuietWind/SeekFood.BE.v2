package com.seek.food.fund.Service;

import com.seek.food.dto.Fund.FundOrderRecordDTO;

import java.util.List;

public interface FundOrderRecordService {
    public List<FundOrderRecordDTO> getSimple(int start, int need);
    public FundOrderRecordDTO getDetail(long recordId);
    public void pay(long recordId);
    public void rollback(long recordId);
    public void ackRefund(long orderId);
}
