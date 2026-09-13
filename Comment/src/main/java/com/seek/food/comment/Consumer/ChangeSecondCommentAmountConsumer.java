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
public class ChangeSecondCommentAmountConsumer {
    private final FirstCommentMapper firstCommentMapper;
    @Autowired
    public ChangeSecondCommentAmountConsumer(FirstCommentMapper firstCommentMapper) {
        this.firstCommentMapper = firstCommentMapper;
    }


    @RabbitListener(queues = MQNameKeyEnum.Comment_Exchange_Change_Second_Comment_Amount_Queue)
    public void changeSecondCommentAmountQueue(ChangeAmountDTO changeAmountDTO) {
        firstCommentMapper.updateSecondCommentAmount(changeAmountDTO.getId(),changeAmountDTO.getChangeNumber());
    }




}
