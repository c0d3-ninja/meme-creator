package com.thugdroid.memeking.utils;

public class NumberUtils {

    public static String getFormattedCount(double count){
        String countStr = String.valueOf(count);
        String[] countArr = countStr.split("\\.");
        if(countArr.length==0){
            return "";
        }
        if(countArr.length==1){
            return countArr[0];
        }
        int secondPart = Integer.parseInt(countArr[1].substring(0,1));
        if(secondPart==0){
            return countArr[0];
        }
        return (countArr[0]+"."+secondPart);
    }

    public static String getCountString(int count){
        if(count<1000){
            return (String.valueOf(count));
        }
        double myCount=((double)count)/1000;
        if(myCount<1000){
            return (getFormattedCount(myCount)+"K+");
        }
        myCount=myCount/1000;
        if(myCount<1000){
            return (getFormattedCount(myCount)+"M+");
        }
        count=count/1000;
        if(count<1000){
            return (getFormattedCount(myCount)+"B+");
        }
        return "";
    }

}
