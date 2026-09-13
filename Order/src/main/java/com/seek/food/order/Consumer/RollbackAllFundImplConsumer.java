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
public class RollbackAllFundImplConsumer {


    private final OrderMapper orderMapper;
    private final OrderExchangeConfig orderExchangeConfig;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public RollbackAllFundImplConsumer(OrderMapper orderMapper, OrderExchangeConfig orderExchangeConfig, RabbitTemplate rabbitTemplate) {
        this.orderMapper = orderMapper;
        this.orderExchangeConfig = orderExchangeConfig;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = MQNameKeyEnum.Dead_Letter_Exchange_Rollback_All_Fund_Impl_Queue)
    public void rollbackAllFundImplQueue(long orderId){
        if (!orderMapper.lock(orderId)) return;
        //发送消息进行全局回滚
        MQUtil.send(orderExchangeConfig.getExchangeName(),orderExchangeConfig.getRollbackFundQueue().getRoutingKey(),orderId
        ,rabbitTemplate);
    }




















}
