package com.yongda.site.app.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    public static final String secondFormat = "yyyy-MM-dd HH:mm:ss";
    public static Date parseDate(String Date) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(secondFormat);

        if ((Date == null) || (Date.length() != secondFormat.length())) {
            throw new ParseException("length is not right", 0);
        }
        return dateFormat.parse(Date);
    }
    public static String getDateString(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        return dateFormat.format(calendar.getTime());
    }
    
    /**
     * 获取现在时间,指定返回时间格式
     * @param pattern
     * @return
     */
    public static String getStringDate(String pattern) {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        String dateString = formatter.format(currentTime);
        return dateString;
    }
    
    /**
     * 获取现在时间的前一天,指定返回时间格式
     * @param pattern
     * @return
     */
    public static String getBeforeDate(String pattern){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);    //得到前一天
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        String  dateString= formatter.format(calendar.getTime());
        return dateString;
    }
    
    public static void main(String[] args) throws ParseException {
        System.out.println(DateUtils.parseDate("2013-12-09 00:00:01"));
    }
}
