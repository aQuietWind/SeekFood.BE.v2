package com.seek.food.comment.Consumer;

import com.seek.food.comment.Mapper.FirstCommentMapper;
import com.seek.food.config.Enum.MQNameKeyEnum;
import com.seek.food.dto.Common.ChangeAmountDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class ChangeFirstCommentLikeAmountConsumer {
    private final FirstCommentMapper firstCommentMapper;
    @Autowired
    public ChangeFirstCommentLikeAmountConsumer(FirstCommentMapper firstCommentMapper) {
        this.firstCommentMapper = firstCommentMapper;
    }


    @RabbitListener(queues = MQNameKeyEnum.Interaction_Exchange_Change_First_Comment_Like_Amount_Queue)
    public void changeFirstCommentLikeAmountQueue(ChangeAmountDTO changeAmountDTO) {
        firstCommentMapper.updateLikeAmount(changeAmountDTO.getId(),changeAmountDTO.getChangeNumber());
    }




}
