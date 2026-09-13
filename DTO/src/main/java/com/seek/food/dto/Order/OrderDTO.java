package com.seek.food.dto.Order;


import com.seek.food.dto.Meal.MealDTO;
import com.seek.food.util.CommonUtil.DoubleUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    private Long orderId;
    private Long userId;
    private Long merchantId;
    private Long riderId;
    private Long voucherConnectionId;
    private Long mealId;
    private String mealName;
    private Double mealPrice;
    private String mealDescription;
    private String mealShowImageAddr;
    private String mealContent;
    private Integer mealType;
    private Integer number;
    private String deliveryAddress;
    private Double deliveryLon;
    private Double deliveryLat;
    private Double originCost;
    private Double discountCost;
    private Double riderCost;
    private Double totalCost;
    private LocalDateTime createTime;
    private LocalDateTime completeTime;
    private Integer stateNow;
    private Boolean refund;
    private Boolean lock;
    private Boolean ack;
    private Boolean complete;


    public static OrderDTO quickGet(long orderId,long userId,MealDTO meal,Long voucherConnectionId,int number
            ,double lon,double lat,String deliveryAddress,double discountCost,double riderCost) {
        OrderDTO order=quickGetFromMeal(meal);
        order.setOrderId(orderId);
        order.setUserId(userId);
        order.setVoucherConnectionId(voucherConnectionId);
        order.setNumber(number);
        order.setDeliveryLon(lon);
        order.setDeliveryLat(lat);
        order.setDeliveryAddress(deliveryAddress);
        order.setDiscountCost(only2AfterPoint(discountCost));
        order.setRiderCost(only2AfterPoint(riderCost));
        order.setOriginCost(only2AfterPoint(meal.getMealPrice() * number + riderCost));
        order.setTotalCost(only2AfterPoint(order.getOriginCost()-discountCost));
        return order;
    }

    public static OrderDTO quickGetFromMeal(MealDTO meal) {
        OrderDTO order=new OrderDTO();
        order.setMerchantId(meal.getMerchantId());
        order.setMealId(meal.getMealId());
        order.setMealName(meal.getMealName());
        order.setMealPrice(meal.getMealPrice());
        order.setMealDescription(meal.getMealDescription());
        order.setMealShowImageAddr(meal.getMealShowImageAddr());
        order.setMealContent(meal.getMealContent());
        order.setMealType(meal.getMealType());
        return order;
    }

    public static double only2AfterPoint(double number) {
        return DoubleUtil.onlyXAfterPoint(2, number);
    }
}
