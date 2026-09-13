package com.seek.food.merchant.Consumer;

import com.seek.food.config.Enum.MQNameKeyEnum;
import com.seek.food.dto.Common.ChangeAmountDTO;
import com.seek.food.merchant.Mapper.MerchantMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class ChangeMerchantCollectAmountConsumer {
    private final MerchantMapper merchantMapper;
    @Autowired
    public ChangeMerchantCollectAmountConsumer(MerchantMapper merchantMapper) {
        this.merchantMapper = merchantMapper;
    }


    @RabbitListener(queues = MQNameKeyEnum.Interaction_Exchange_Change_Merchant_Collect_Amount_Queue)
    public void changeMerchantCollectAmountQueue(ChangeAmountDTO changeAmountDTO) {
        merchantMapper.updateCollectAmount(changeAmountDTO.getId(),changeAmountDTO.getChangeNumber());
    }




}
