package com.seek.food.fund.Service;

import com.seek.food.dto.Fund.FundOrderRefundRecordDTO;

import java.util.List;

public interface FundOrderRefundRecordService {
    public List<FundOrderRefundRecordDTO> getSimple(int start, int need);
}
