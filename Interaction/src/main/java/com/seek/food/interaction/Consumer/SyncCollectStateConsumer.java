package com.seek.food.interaction.Consumer;

import com.seek.food.config.Enum.MQNameKeyEnum;
import com.seek.food.dto.Common.SyncStateDTO;
import com.seek.food.interaction.Mapper.CollectMapper;
import com.seek.food.interaction.Mapper.LikeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class SyncCollectStateConsumer {
    private final CollectMapper collectMapper;

    @Autowired
    public SyncCollectStateConsumer(CollectMapper collectMapper) {
        this.collectMapper = collectMapper;
    }


    @RabbitListener(queues = MQNameKeyEnum.Interaction_Exchange_Sync_Collect_State_Queue)
    public void syncCollectStateQueue(SyncStateDTO syncStateDTO) {
        collectMapper.syncCollect(syncStateDTO);
    }




}
