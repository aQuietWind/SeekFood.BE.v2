package com.seek.food.dto.Order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
//注解属性只能硬编码，没办法,不过也可以写到Config模块的枚举类中统一管理
@Document(indexName = "rider_order")
public class RiderOrderEsDTO {
    @Id
    private Long orderId;
    private Long merchantId;
    private String deliveryAddress;
    private String deliveryLatLon;
    private String mealContent;
    private Integer number;
    private Double riderCost;
    @Field("is_accept")
    private Boolean accept;

    public RiderOrderEsDTO(OrderDTO order) {
        this.orderId = order.getOrderId();
        this.merchantId = order.getMerchantId();
        this.deliveryAddress = order.getDeliveryAddress();
        this.deliveryLatLon = order.getDeliveryLat()+","+order.getDeliveryLon();
        this.mealContent = order.getMealContent();
        this.number = order.getNumber();
        this.riderCost = order.getRiderCost();
        this.accept = false;
    }
}
