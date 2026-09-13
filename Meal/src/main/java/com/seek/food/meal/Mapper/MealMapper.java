package com.seek.food.meal.Mapper;

import com.seek.food.dto.Meal.MealDTO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MealMapper {
    public void insertMeal(long mealId,String mealName,double mealPrice,String mealContent,long merchantId);
    public List<MealDTO> getSimple(long merchantId, int start, int need);
    public List<MealDTO> getSimpleByType(long merchantId,int type,int start,int need);
    public MealDTO getDetail(long mealId);
    public boolean updateMealMessage(MealDTO meal);
    public boolean updateShowImage(long mealId, String addr,String oldAddr,long merchantId);
    public boolean deleteAllMeal(long merchantId);
    public String getShowImageAddr(long mealId);
    public String getShowImageAddrAfterDelete(long mealId);
    public List<String> getAllShowImageAddrAfterDelete(long merchantId);
    public boolean updatePrice(long mealId,double price,long merchantId);
    public boolean updateSell(long mealId,long merchantId);
    public boolean deleteMeal(long mealId,long merchantId);
    public boolean updateSalesVolume(long mealId,int changeNumber);
}
