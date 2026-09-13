package com.seek.food.order.Consumer;

import com.seek.food.config.Enum.MQNameKeyEnum;
import com.seek.food.config.NacosConfig.MQ.OrderExchangeConfig;
import com.seek.food.order.Mapper.OrderMapper;
import com.seek.food.util.MQ.MQUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderAckConsumer {


    private final OrderMapper orderMapper;

    @Autowired
    public OrderAckConsumer(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    @RabbitListener(queues = MQNameKeyEnum.Voucher_Exchange_Register_Order_Ack_Queue)
    public void orderAckQueue(long orderId){
        orderMapper.ack(orderId);
    }




















}
