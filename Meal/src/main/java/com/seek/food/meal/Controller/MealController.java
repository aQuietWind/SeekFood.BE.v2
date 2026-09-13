package com.seek.food.meal.Controller;

import com.seek.food.dto.Common.Result;
import com.seek.food.dto.Meal.MealDTO;
import com.seek.food.meal.Enum.RequestPathEnum;
import com.seek.food.meal.Service.MealService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(RequestPathEnum.Meal)
public class MealController {
    private final MealService mealService;
    public MealController(MealService mealService) {
        this.mealService = mealService;
    }

    //新增餐品
    @PostMapping(RequestPathEnum.Meal_Insert)
    public Result<Void> insertMeal(String mealName,double mealPrice,String mealContent) {
        mealService.insertMeal(mealName,mealPrice,mealContent);
        return Result.success();
    }

    //获取预览的餐品信息
    @GetMapping(RequestPathEnum.Meal_Get_Simple)
    public Result<List<MealDTO>> getSimple(long merchantId,int start,int need) {
        return Result.success(mealService.getSimple(merchantId,start,need));
    }

    //根据类型获取预览信息
    @GetMapping(RequestPathEnum.Meal_Get_Simple_By_Type)
    public Result<List<MealDTO>> getSimpleByType(long merchantId,int type,int start,int need) {
        return Result.success(mealService.getSimpleByType(merchantId,type,start,need));
    }

    //获取餐品详细信息
    @GetMapping(RequestPathEnum.Meal_Get_Detail)
    public Result<MealDTO> getDetail(long mealId) {
        return Result.success(mealService.getDetail(mealId));
    }

    //更改餐品常规的信息
    @PutMapping(RequestPathEnum.Meal_Update_Message)
    public Result<Void> updateMessage(@RequestBody MealDTO meal) {
        mealService.updateMessage(meal);
        return Result.success();
    }

    //更改展示图片
    @PutMapping(RequestPathEnum.Meal_Update_Show_Image)
    public Result<Void> updateShowImage(long mealId,@RequestBody MultipartFile file) {
        mealService.updateShowImage(mealId,file);
        return Result.success();
    }

    //更改价格
    @PutMapping(RequestPathEnum.Meal_Update_Price)
    public Result<Void> updatePrice(long mealId,double price) {
        mealService.updatePrice(mealId,price);
        return Result.success();
    }

    //更改出售状态
    @PutMapping(RequestPathEnum.Meal_Update_Sell)
    public Result<Void> updateSell(long mealId) {
        mealService.updateSell(mealId);
        return Result.success();
    }

    //删除餐品
    @DeleteMapping(RequestPathEnum.Meal_Delete)
    public Result<Void> deleteMeal(long mealId) {
        mealService.deleteMeal(mealId);
        return Result.success();
    }





}