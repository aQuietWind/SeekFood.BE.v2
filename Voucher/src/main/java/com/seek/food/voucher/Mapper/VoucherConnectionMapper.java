package com.seek.food.voucher.Mapper;

import com.seek.food.dto.Voucher.VoucherConnectionDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface VoucherConnectionMapper {
    public List<VoucherConnectionDTO> getSimple(int start, int need,long userId);
    public List<VoucherConnectionDTO> getSimpleEffective(int start, int need,long userId);
    public VoucherConnectionDTO getDetail(long connectionId,long userId);
    public void rollback(long orderId);
    public void use(long orderId);
    public void insertConnection(VoucherConnectionDTO connection);
    public boolean lock(long userId,long connectionId,long orderId);
    public Double check(long userId,long connectionId,double cost);
    public boolean exist(long userId,long promotionId);
    public Long getConnectionIdByOrderId(long orderId);
}
