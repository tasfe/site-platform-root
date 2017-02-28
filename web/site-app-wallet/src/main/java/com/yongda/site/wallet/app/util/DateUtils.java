package com.yongda.site.wallet.app.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    private static Logger logger = LoggerFactory.getLogger("DateUtils");
    public static final String secondFormat = "yyyy-MM-dd HH:mm:ss";
    /**String 字符串转换成date格式数据****/
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

    /***
     * 判断时间前后
     * @param dt2
     * @return
     */
    public static int compareDate(Date dt2){
        Date date = new Date();
        if (date.getTime() > dt2.getTime()) {
            return 1;
        } else if (date.getTime() < dt2.getTime()) {
            return -1;
        } else {//相等
            return 0;
        }
    }

    /**
     * 获取当前时间的前几天或者后几天的日期
     * @param days
     * @return
     */
    public static Date getDateNearCurrent(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, days);
        Date date = calendar.getTime();
        return date;

    }
}
