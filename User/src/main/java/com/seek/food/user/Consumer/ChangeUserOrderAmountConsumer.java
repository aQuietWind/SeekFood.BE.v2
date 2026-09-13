package com.seek.food.user.Consumer;

import com.seek.food.config.Enum.MQNameKeyEnum;
import com.seek.food.dto.Common.ChangeAmountDTO;
import com.seek.food.user.Mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class ChangeUserOrderAmountConsumer {
    private final UserMapper userMapper;
    @Autowired
    public ChangeUserOrderAmountConsumer(UserMapper userMapper) {
        this.userMapper = userMapper;
    }


    @RabbitListener(queues = MQNameKeyEnum.Order_Exchange_Change_User_Order_Amount_Queue)
    public void changeUserOrderAmountQueue(ChangeAmountDTO changeAmountDTO) {
        userMapper.updateOrderAmount(changeAmountDTO.getId(),changeAmountDTO.getChangeNumber());
    }




}
