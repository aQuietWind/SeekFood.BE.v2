package com.seek.food.order.Service;

import com.seek.food.dto.Comment.FirstCommentDTO;
import com.seek.food.dto.Order.OrderDTO;

import java.util.List;

public interface OrderService {
    public void insertOrder(long mealId,Long connectionId,int number,double lon,double lat,String deliveryAddress);
    public List<OrderDTO> getSimple(int start, int need);
    public List<OrderDTO> getSimpleState(int start, int need, int state);
    public OrderDTO  getDetail(long orderId);
    public void refund(long orderId);
    public void merchantReject(long orderId);
    public void merchantAck(long orderId);
    public void merchantMake(long orderId);
    public void riderAccept(long orderId);
    public void riderAck(long orderId);
    public void riderDelivery(long orderId);
    public void userReceive(long orderId);
    public FirstCommentDTO commentSelect(long orderId);
}
