package com.seek.food.voucher.Service;

import com.seek.food.dto.Voucher.MerchantVoucherDTO;

import java.util.List;

public interface MerchantVoucherService {
    public void insertMerchantVoucher(MerchantVoucherDTO voucher);
    public List<MerchantVoucherDTO> getSimple(int start, int need);
    public List<MerchantVoucherDTO> getSimpleEffective(int start, int need);
    public MerchantVoucherDTO getDetail(long voucherId);
    public Boolean exist(long voucherId);
}
