package com.seek.food.meal.Consumer;

import com.seek.food.config.Enum.MQNameKeyEnum;
import com.seek.food.dto.Common.ChangeAmountDTO;
import com.seek.food.meal.Mapper.MealMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class ChangeMealSalesVolumeConsumer {
    private final MealMapper mealMapper;
    @Autowired
    public ChangeMealSalesVolumeConsumer(MealMapper mealMapper) {
        this.mealMapper = mealMapper;
    }


    @RabbitListener(queues = MQNameKeyEnum.Order_Exchange_Change_Meal_Sales_Volume_Queue)
    public void changeMealSalesVolumeQueue(ChangeAmountDTO changeAmountDTO) {
        mealMapper.updateSalesVolume(changeAmountDTO.getId(),changeAmountDTO.getChangeNumber());
    }




}
