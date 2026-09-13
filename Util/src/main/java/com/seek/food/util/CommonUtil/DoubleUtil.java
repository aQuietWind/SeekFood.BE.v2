package com.seek.food.util.CommonUtil;

public class DoubleUtil {

    public static double onlyXAfterPoint(int x,double number){
        String strNumber=String.valueOf(number);
        if(strNumber.length()<=strNumber.indexOf('.')+x)return number;
        //由于substring不会保留endIndex，所以必须加1位
        return Double.parseDouble(strNumber.substring(0,strNumber.indexOf('.')+x+1));
    }
}
