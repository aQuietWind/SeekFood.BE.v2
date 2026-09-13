package com.seek.food.voucher.Mapper;

import com.seek.food.dto.Voucher.MerchantVoucherDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MerchantVoucherMapper {
    public void insertMerchantVoucher(MerchantVoucherDTO voucher);
    public List<MerchantVoucherDTO> getSimple(int start,int need,long merchantId);
    public List<MerchantVoucherDTO> getSimpleEffective(int start, int need, long merchantId);
    public MerchantVoucherDTO getDetail(long voucherId);
    public boolean exist(long merchantId,long voucherId);
}
