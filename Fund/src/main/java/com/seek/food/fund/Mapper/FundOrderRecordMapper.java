package com.seek.food.fund.Mapper;

import com.seek.food.dto.Fund.FundOrderRecordDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FundOrderRecordMapper {
    public void insertRecord(FundOrderRecordDTO fundOrderRecord);
    public List<FundOrderRecordDTO> getSimple(int start,int need,long accountId);
    public FundOrderRecordDTO getDetail(long recordId,long accountId);
    public Long ableRollback(long recordId, long accountId);
    //使用orderId是为了兼容性
    public Long refund(long orderId);
    public boolean ackPay(long recordId);
    public Long getRecordIdByOrderId(long orderId);
}
