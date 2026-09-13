package com.seek.food.util.TimeUtil;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeUtil {
    private TimeUtil() {}
    public static final DateTimeFormatter Default_Time_Format=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter Zip_Time_Format=DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    public static final DateTimeFormatter Zip_Date_Format=DateTimeFormatter.ofPattern("yyyyMMdd");
    public static LocalDateTime getNowAfterHours(int number) {
        return LocalDateTime.now().plusHours(number);
    }
    public static long getStampBy(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
    public static long getPlusHoursStampByNow(int number) {
        return LocalDateTime.now().plusHours(number).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
    public static long getStampByNow() {
        return System.currentTimeMillis();
    }
    public static String getNowByFormat(DateTimeFormatter format) {
        return LocalDateTime.now().format(format);
    }

}
