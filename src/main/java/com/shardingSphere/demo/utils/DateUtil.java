package com.shardingSphere.demo.utils;

import java.time.LocalDateTime;

public class DateUtil {
    public static String getDateStr(){
        LocalDateTime now = LocalDateTime.now();
        return now.getYear()+now.getMonthValue()+now.getDayOfMonth()+now.getHour()+now.getMinute()+now.getSecond()+"";
    }
}
