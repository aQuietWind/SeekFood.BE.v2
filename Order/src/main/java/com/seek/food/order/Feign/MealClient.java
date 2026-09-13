package com.seek.food.order.Feign;

import com.seek.food.dto.Common.Result;
import com.seek.food.dto.Meal.MealDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

//可改为动态指定名称，但是需要在Config模块手写一个枚举类
@FeignClient("meal")
public interface MealClient {
    @GetMapping("/meal/detail")
    Result<MealDTO> mealGetDetail(@RequestParam("mealId") long mealId);
}
