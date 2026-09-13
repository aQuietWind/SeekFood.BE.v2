package com.seek.food.order.Controller;


import com.seek.food.dto.Comment.FirstCommentDTO;
import com.seek.food.dto.Common.Result;
import com.seek.food.dto.Order.OrderDTO;
import com.seek.food.order.Enum.RequestPathEnum;
import com.seek.food.order.Service.OrderService;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(RequestPathEnum.Order)
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    //新增订单
    @PostMapping
    @GlobalTransactional
    public Result<Void> insertOrder(long mealId,Long connectionId,int number,double lon,double lat,String deliveryAddress){
        orderService.insertOrder( mealId,connectionId,number,lon,lat,deliveryAddress);
        return Result.success();
    }

    //获取订单的简易信息
    @GetMapping(RequestPathEnum.Order_Get_Simple)
    public Result<List<OrderDTO>> getSimple(int start, int need){
        return Result.success(orderService.getSimple(start,need));
    }

    //根据所处阶段获取订单的简易信息
    @GetMapping(RequestPathEnum.Order_Get_Simple_State)
    public Result<List<OrderDTO>> getSimpleState(int start,int need,int state){
        return Result.success(orderService.getSimpleState(start,need,state));
    }

    //获取订单详细信息
    @GetMapping(RequestPathEnum.Order_Get_Detail)
    public Result<OrderDTO> getDetail(long orderId){
        return Result.success(orderService.getDetail(orderId));
    }

    //用户订单退款
    @PutMapping(RequestPathEnum.Order_Refund)
    public Result<Void> refund(long orderId){
        orderService.refund(orderId);
        return Result.success();
    }

    //商家拒绝执行订单
    @PutMapping(RequestPathEnum.Order_Merchant_Reject)
    public Result<Void> merchantReject(long orderId){
        orderService.merchantReject(orderId);
        return Result.success();
    }

    //商家确定订单
    @PutMapping(RequestPathEnum.Order_Merchant_Ack)
    public Result<Void> merchantAck(long orderId){
        orderService.merchantAck(orderId);
        return Result.success();
    }

    //商家制作订单
    @PutMapping(RequestPathEnum.Order_Merchant_Make)
    public Result<Void> merchantMake(long orderId){
        orderService.merchantMake(orderId);
        return Result.success();
    }

    //骑手接受订单
    @PutMapping(RequestPathEnum.Order_Rider_Accept)
    public Result<Void> riderAccept(long orderId){
        orderService.riderAccept(orderId);
        return Result.success();
    }

    //骑手确认已经拿到订单餐品
    @PutMapping(RequestPathEnum.Order_Rider_Ack)
    public Result<Void> riderAck(long orderId){
        orderService.riderAck(orderId);
        return Result.success();
    }

    //骑手确认已经运送餐品到目标地点
    @PutMapping(RequestPathEnum.Order_Rider_Delivery)
    public Result<Void> riderDelivery(long orderId){
        orderService.riderDelivery(orderId);
        return Result.success();
    }

    //用户确认接收到订单餐品
    @PutMapping(RequestPathEnum.Order_User_Receive)
    public Result<Void> userReceive(long orderId){
        orderService.userReceive(orderId);
        return Result.success();
    }

    //用户插入评论时查询订单获取一定信息
    @GetMapping(RequestPathEnum.Order_Comment_Select)
    public Result<FirstCommentDTO> commentSelect(long orderId){
        return Result.success(orderService.commentSelect(orderId));
    }

}
