package com.thugdroid.memeking.utils;

public class TimeUtils {

    public static  double getDifferenceInHrs(Long time1,Long time2){
        if(time1==null || time2==null){
            return 0;
        }
        double diff = Math.abs(time1-time2);

        return diff/getHrsToMillis(1) ;
    }
    public static Long getDifferenceInDays(Long millis1,Long millis2){
        if(millis1==null || millis2==null){
            return Long.valueOf(0);
        }
        Long diff = Math.abs(millis2-millis1);
        return (diff/(1000*60*60*24));
    }
    public static long getHrsToMillis(int hr){
        return  hr * 3600000;
    }
}
