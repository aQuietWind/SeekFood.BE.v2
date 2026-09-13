package com.seek.food.meal.Consumer;

import com.seek.food.config.Enum.MQNameKeyEnum;
import com.seek.food.config.NacosConfig.MQ.MealExchangeConfig;
import com.seek.food.config.NacosConfig.Meal.MealParamsRulesConfig;
import com.seek.food.meal.Mapper.MealMapper;
import com.seek.food.util.MQ.MQUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Slf4j
public class DeleteAllMealConsumer {
    private final MealMapper mealMapper;
    private final MealParamsRulesConfig mealParamsRulesConfig;
    private final MealExchangeConfig mealExchangeConfig;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public DeleteAllMealConsumer( MealMapper mealMapper
    , MealParamsRulesConfig mealParamsRulesConfig, MealExchangeConfig mealExchangeConfig, RabbitTemplate rabbitTemplate) {
        this.mealMapper = mealMapper;
        this.mealParamsRulesConfig = mealParamsRulesConfig;
        this.mealExchangeConfig = mealExchangeConfig;
        this.rabbitTemplate = rabbitTemplate;
    }


    @RabbitListener(queues = MQNameKeyEnum.Merchant_Exchange_Delete_All_Meal_Queue)
    public void deleteAllMealQueue(long merchantId){
        mealMapper.deleteAllMeal(merchantId);
        MQUtil.sendWithTLL(mealExchangeConfig.getExchangeName(),mealExchangeConfig.getDeleteAllFileMealDeadLetterQueue().getRoutingKey()
        ,merchantId,rabbitTemplate,mealParamsRulesConfig.getMillsByFileDeleteDay());
    }















}
