package com.seek.food.comment.Consumer;

import com.seek.food.comment.Mapper.SecondCommentMapper;
import com.seek.food.config.Enum.MQNameKeyEnum;
import com.seek.food.dto.Common.ChangeAmountDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class ChangeSecondCommentLikeAmountConsumer {
    private final SecondCommentMapper secondCommentMapper;

    @Autowired
    public ChangeSecondCommentLikeAmountConsumer(SecondCommentMapper secondCommentMapper) {
        this.secondCommentMapper = secondCommentMapper;
    }


    @RabbitListener(queues = MQNameKeyEnum.Interaction_Exchange_Change_Second_Comment_Like_Amount_Queue)
    public void changeSecondCommentLikeAmountQueue(ChangeAmountDTO changeAmountDTO) {
        secondCommentMapper.updateLikeAmount(changeAmountDTO.getId(),changeAmountDTO.getChangeNumber());
    }




}
