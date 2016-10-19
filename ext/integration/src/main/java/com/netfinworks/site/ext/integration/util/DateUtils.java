package com.netfinworks.site.ext.integration.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    public static final String secondFormat = "yyyy-MM-dd HH:mm:ss";
    public static final String PATTERN_FULL = "yyyyMMddHHmmss";  
    
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
   
}
