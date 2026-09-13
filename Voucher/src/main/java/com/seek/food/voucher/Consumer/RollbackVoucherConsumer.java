package com.seek.food.voucher.Consumer;

import com.seek.food.config.Enum.MQNameKeyEnum;
import com.seek.food.voucher.Mapper.VoucherConnectionMapper;
import com.seek.food.voucher.Service.VoucherConnectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class RollbackVoucherConsumer {
    private final VoucherConnectionMapper voucherConnectionMapper;
    private final VoucherConnectionService voucherConnectionService;

    @Autowired
    public RollbackVoucherConsumer(VoucherConnectionMapper voucherConnectionMapper, VoucherConnectionService voucherConnectionService) {
        this.voucherConnectionMapper = voucherConnectionMapper;
        this.voucherConnectionService = voucherConnectionService;
    }


    @RabbitListener(queues = MQNameKeyEnum.Order_Exchange_Rollback_Voucher_Queue)
    public void rollbackVoucherQueue(Long orderId){
        if (orderId == null)return;
        voucherConnectionService.rollback(orderId);
    }
}
