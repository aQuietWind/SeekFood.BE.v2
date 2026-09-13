package com.seek.food.fund.Mapper;

import com.seek.food.dto.Fund.FundWithdrawRecordDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FundWithdrawRecordMapper {
    boolean insertWithdrawRecord(FundWithdrawRecordDTO fundWithdrawRecordDTO);
    List<FundWithdrawRecordDTO> getSimple(long accountId, int start, int need);
    FundWithdrawRecordDTO getDetail(long accountId, long recordId);
}
