package com.netfinworks.site.web.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * <p>日期格式</p>
 * @author zhangyun.m
 * @version $Id: FormatDate.java, v 0.1 2013-12-5 上午10:35:31 HP Exp $
 */
public class FormatForDate {
    
    
  //获取当月第一天(00:00:00)零点零分零秒开始
    public Date getFirstDayOfMonth(){  
        Calendar firstDate = Calendar.getInstance();
        firstDate.set(Calendar.DATE,1);//设为当前月的1号
        //时 
        firstDate.set(Calendar.HOUR_OF_DAY, 00); 
        //分
        firstDate.set(Calendar.MINUTE, 00); 
        //秒
        firstDate.set(Calendar.SECOND,00);
        return firstDate.getTime();  
     }
    
 // 计算当月最后一天,返回字符串
    public Date getDefaultDay(){  
        Calendar lastDate = Calendar.getInstance();
        lastDate.set(Calendar.DATE,1);//设为当前月的1号
        lastDate.add(Calendar.MONTH,1);//加一个月，变为下月的1号
        lastDate.add(Calendar.DATE,-1);//减去一天，变为当月最后一天
        return lastDate.getTime();  
     }
    
  //获得上月最后一天的日期 
    public String getPreviousMonthEnd(){ 
        String str = "";
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        Calendar lastDate = Calendar.getInstance();
        lastDate.add(Calendar.MONTH,-1);//减一个月
        lastDate.set(Calendar.DATE, 1);//把日期设置为当月第一天
        lastDate.roll(Calendar.DATE, -1);//日期回滚一天，也就是本月最后一天
        str=sdf.format(lastDate.getTime()); 
        return str; 
    }
    
    //获得上月最后一天的日期(Date)
    public Date getPreviousMonthEndD(){ 
        Calendar lastDate = Calendar.getInstance();
        lastDate.add(Calendar.MONTH,-1);//减一个月
        lastDate.set(Calendar.DATE, 1);//把日期设置为当月第一天
        lastDate.roll(Calendar.DATE, -1);//日期回滚一天，也就是本月最后一天
        return lastDate.getTime(); 
    }
    
    /** 
     * 得到某年某月的第一天 (String)
     *  
     * @param year 
     * @param month 
     * @return 
     */ 

    public String getFirstDayOfMonth(int year, int month) { 
     Calendar cal = Calendar.getInstance(); 
     cal.set(Calendar.YEAR, year); 
     cal.set(Calendar.MONTH, month-1); 
     cal.set(Calendar.DAY_OF_MONTH, cal.getMinimum(Calendar.DATE)); 
     return new SimpleDateFormat("yyyy/MM/dd").format(cal.getTime()); 
    } 
    
    /** 
     * 得到某年某月的第一天 (Date)
     *  
     * @param year 
     * @param month 
     * @return 
     */ 

    public Date getFirstDayOfMonthD(int year, int month) { 
     Calendar cal = Calendar.getInstance(); 
     cal.set(Calendar.YEAR, year); 
     cal.set(Calendar.MONTH, month-1); 
     cal.set(Calendar.DAY_OF_MONTH, cal.getMinimum(Calendar.DATE));
     //时 
     cal.set(Calendar.HOUR_OF_DAY, 00); 
     //分
     cal.set(Calendar.MINUTE, 00); 
     //秒
     cal.set(Calendar.SECOND,00);
     return cal.getTime(); 
    } 

    /** 
     * 得到某年某月的最后一天 (String)
     *  
     * @param year 
     * @param month 
     * @return 
     */ 

    public String getLastDayOfMonth(int year, int month) { 
     Calendar cal = Calendar.getInstance(); 
     cal.set(Calendar.YEAR, year); 
     cal.set(Calendar.MONTH, month-1); 
     cal.set(Calendar.DAY_OF_MONTH, 1); 
     int value = cal.getActualMaximum(Calendar.DAY_OF_MONTH); 
     cal.set(Calendar.DAY_OF_MONTH, value); 
     return new SimpleDateFormat("yyyy/MM/dd").format(cal.getTime()); 
    } 
    
    /** 
     * 得到某年某月的最后一天 (Date)
     *  
     * @param year 
     * @param month 
     * @return 
     */ 

    public Date getLastDayOfMonthD(int year, int month) { 
     Calendar cal = Calendar.getInstance(); 
     cal.set(Calendar.YEAR, year); 
     cal.set(Calendar.MONTH, month-1); 
     cal.set(Calendar.DAY_OF_MONTH, 1); 
     int value = cal.getActualMaximum(Calendar.DAY_OF_MONTH); 
     cal.set(Calendar.DAY_OF_MONTH, value); 
     //时 
     cal.set(Calendar.HOUR_OF_DAY, 23); 
     //分
     cal.set(Calendar.MINUTE, 59); 
     //秒
     cal.set(Calendar.SECOND,59);
     return cal.getTime(); 
    } 



}
