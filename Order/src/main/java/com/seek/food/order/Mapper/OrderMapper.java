package com.seek.food.order.Mapper;

import com.seek.food.dto.Comment.FirstCommentDTO;
import com.seek.food.dto.Order.OrderDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderMapper {
    public void insertOrder(OrderDTO order);
    public boolean lock(long orderId);
    public boolean ack(long orderId);
    public List<OrderDTO> getSimple(int start,int need,long tokenId);
    public List<OrderDTO> getSimpleState(int start,int need,int state,long tokenId);
    public OrderDTO getDetail(long orderId);
    public Boolean refund(long orderId,long userId);
    public Boolean merchantReject(long orderId,long merchantId);
    public Boolean merchantAck(long orderId,long merchantId);
    public Boolean merchantMake(long orderId,long merchantId);
    public Boolean riderAccept(long orderId,long riderId);
    public Boolean riderAck(long orderId,long riderId);
    public Boolean riderDelivery(long orderId,long riderId);
    public Boolean userReceive(long orderId,long userId);
    public FirstCommentDTO commentSelect(long orderId, long userId);
}
