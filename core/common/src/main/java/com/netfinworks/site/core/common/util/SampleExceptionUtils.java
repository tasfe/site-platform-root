package com.netfinworks.site.core.common.util;
/** 
 * 该类主要是对于部分异常需要抛给用户的
 * 例如一些 状态验证等 这里就在异常信息里简单加个标示,来标示该异常提示是否需要给客户提示
 * 如果要完整的处理整个系统异常,提示等 需要从根异常开始重新定义需要耗费较多时间目前这个可以适用
 * @author tangL
 * @date 2014年12月26日
 * @since 1.6
 */ 
public class SampleExceptionUtils {
	private static final String TIP_SIGN = "@@";
	private static final String TIP_TEMPLATE_MSG = TIP_SIGN + "%s";	
	
	/**
	 * 添加异常标示，表明该异常可以抛给提示客户
	 * @param msg
	 * @return
	 */
	public static String addTip(String msg) {
		String rMsg = isNotEmpty(msg) ? msg : "";
		return String.format(TIP_TEMPLATE_MSG, rMsg);
	}
	
	/**
	 * 判断异常信息是否可以提示给客户
	 * @param msg
	 * @return
	 */
	public static boolean isTip(String msg) {
		if (isNotEmpty(msg))
			return msg.indexOf(TIP_SIGN) != -1;
		return false;
	}
	/**
	 * 判断异常信息是否可以提示给客户
	 * @param tx
	 * @return
	 */
	public static boolean isTip(Throwable tx) {
		if (tx != null)
			return isTip(tx.getMessage());
		return false;
	}
	/**
	 * 重置提示信息, 主要是替换TIP_SIGN
	 * @param msg
	 * @return
	 */
	public static String restMsg(String msg) {
		return msg.replaceAll(TIP_SIGN, "");
	}
	/**
	 * 向后添加错误信息
	 * @param msg 提示信息
	 * @param errorMsg 错误信息 
	 * @return
	 */
	public static String addAfterIfTipMsg(String msg, String errorMsg) {
		if (isTip(errorMsg) && isNotEmpty(msg))
			return String.format(msg + ",%s",  errorMsg) ;
		return msg;
	}
	
	/**
	 * 向前添加错误信息
	 * @param msg 提示信息
	 * @param errorMsg 错误信息 
	 * @return
	 */
	public static String addBeforeIfTipMsg(String msg, String errorMsg) {
		if (isTip(errorMsg) && isNotEmpty(msg))
			return String.format("%s, " + msg,  errorMsg) ;
		return msg;
	}
	/**
	 * 判断string 不为空 这里比较简单, 为了减少依赖没有使用第三方包
	 * @param s
	 * @return
	 */
	private static boolean isNotEmpty(String s) {
		return s == null ? false : !(s.trim().equals(""));
	}
	
}
