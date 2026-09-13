package com.seek.food.voucher.Consumer;

import com.seek.food.config.Enum.MQNameKeyEnum;
import com.seek.food.config.NacosConfig.MQ.VoucherExchangeConfig;
import com.seek.food.util.MQ.MQUtil;
import com.seek.food.voucher.Mapper.VoucherConnectionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UseVoucherConsumer {
    private final VoucherConnectionMapper voucherConnectionMapper;
    private final VoucherExchangeConfig voucherExchangeConfig;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public UseVoucherConsumer(VoucherConnectionMapper voucherConnectionMapper, VoucherExchangeConfig voucherExchangeConfig, RabbitTemplate rabbitTemplate) {
        this.voucherConnectionMapper = voucherConnectionMapper;
        this.voucherExchangeConfig = voucherExchangeConfig;
        this.rabbitTemplate = rabbitTemplate;
    }


    @RabbitListener(queues = MQNameKeyEnum.Fund_Exchange_Use_Voucher_Queue)
    public void useVoucherQueue(long orderId){
        //使用优惠券
        voucherConnectionMapper.use(orderId);
        //发送去确认订单被下单
        MQUtil.send(voucherExchangeConfig.getExchangeName(),voucherExchangeConfig.getOrderAckQueue().getRoutingKey()
        ,orderId,rabbitTemplate);
    }
}
