package com.seek.food.meal.Service;

import com.seek.food.dto.Meal.MealDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MealService {
    public void insertMeal(String mealName,double mealPrice,String mealContent);
    public List<MealDTO> getSimple(long merchantId,int start,int need);
    public List<MealDTO> getSimpleByType(long merchantId,int type,int start,int need);
    public MealDTO getDetail(long mealId);
    public void updateMessage(MealDTO meal);
    public void updateShowImage(long mealId,MultipartFile file);
    public void updatePrice(long mealId,double price);
    public void updateSell(long mealId);
    public void deleteMeal(long mealId);
}
