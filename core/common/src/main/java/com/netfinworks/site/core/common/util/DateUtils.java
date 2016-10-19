package com.netfinworks.site.core.common.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    public static final String secondFormat = "yyyy-MM-dd HH:mm:ss";
    public static final String webFormat = "yyyy-MM-dd";
    public static Date parseDate(String Date) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(secondFormat);

        if ((Date == null) || (Date.length() != secondFormat.length())) {
            throw new ParseException("length is not right", 0);
        }
        return dateFormat.parse(Date);
    }
    
    public static String dateToString(Date date, String type) {  
        String str = null;  
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");  
        if (type.equals("SHORT")) {  
            // 07-1-18  
            format = DateFormat.getDateInstance(DateFormat.SHORT);  
            str = format.format(date);  
        } else if (type.equals("MEDIUM")) {  
            // 2007-1-18  
            format = DateFormat.getDateInstance(DateFormat.MEDIUM);  
            str = format.format(date);  
        } else if (type.equals("FULL")) {  
            // 2007年1月18日 星期四  
            format = DateFormat.getDateInstance(DateFormat.FULL);  
            str = format.format(date);  
        }  
        return str;  
    }  
    
    public static String formatDateToString(Date date, String format) {
    	 DateFormat df = new SimpleDateFormat(format); 
    	 return df.format(date);
    }
    
    public static Date formatStringToDate(String date, String format) {
   	 DateFormat df = new SimpleDateFormat(format); 
   	 try {
		return df.parse(date);
	} catch (ParseException e) {
		e.printStackTrace();
	}
   	 return null;
   }
    
    public static Date getYesterday(){
    	Calendar a = Calendar.getInstance();
    	a.add(Calendar.DATE, -1);
    	return a.getTime();
    }

    public static void main(String[] args) throws ParseException {
    	Character c = new Character('a');
    	Number num = (short)c.charValue();
        System.out.println(new Integer(num.intValue()));
    }
}
