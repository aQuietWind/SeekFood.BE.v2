package com.seek.food.fund.Mapper;

import com.seek.food.dto.Fund.FundOrderRefundRecordDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FundOrderRefundRecordMapper {
    public List<FundOrderRefundRecordDTO> getSimple(int start, int need, long accountId);
}
