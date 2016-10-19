package com.netfinworks.site.core.common.util;

/**
 * 
 * @author tangL
 * @date 2015-02-06
 *
 */
public class ValidUtils {
    public static void valid (boolean expression, String msg) {
        if (expression)
            throw new java.lang.RuntimeException(SampleExceptionUtils.addTip(msg));
    }
    public static void valid (boolean expression, String msg, Object ...v) {
        if (expression)
            throw new java.lang.RuntimeException(SampleExceptionUtils.addTip(String.format(msg, v)));
    }
    public static boolean isValidException(java.lang.Throwable e) {
        return SampleExceptionUtils.isTip(e);
    }
    public static String restMsg(String msg) {
    	 return SampleExceptionUtils.restMsg(msg);
    }
}
