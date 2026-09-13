package com.seek.food.fund.Mapper;

import com.seek.food.dto.Fund.FundRechargeRecordDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FundRechargeRecordMapper {
    public boolean insertRechargeRecord(FundRechargeRecordDTO fundRechargeRecordDTO);
    public List<FundRechargeRecordDTO> getSimple(long accountId,int start,int need);
    public FundRechargeRecordDTO getDetail(long accountId,long recordId);
}
