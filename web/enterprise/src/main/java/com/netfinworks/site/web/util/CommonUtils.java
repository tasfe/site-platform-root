/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年9月15日
 */
package com.netfinworks.site.web.util;

import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 常用工具类
 * @author xuwei
 * @date 2014年9月15日
 */
public class CommonUtils {
	protected static final Logger logger = LoggerFactory.getLogger(CommonUtils.class);
	
	/**
     * 获取马赛克数据
     * @param data 原文
     * @return 马赛克数据
     */
    public static String getMaskData(String data) {
    	if (StringUtils.isEmpty(data) || data.length() <= 4) {
    		return data;
    	}
    	return getMaskData(data, 0, data.length() - 4);
    }
    
    /**
     * 获取马赛克数据
     * @param data 原文
     * @param start *字符开始位
     * @param end *字符结束位
     * @return 马赛克数据
     */
    public static String getMaskData(String data, int start, int end) {
    	if (start >= end) {
    		return StringUtils.EMPTY;
    	}
    	
    	if (StringUtils.isEmpty(data) || data.length() <= (end - start)) {
    		return data;
    	}
    	
    	int len = data.length();
    	return new StringBuilder(data.replaceAll(".", "*"))
			.replace(0, start, data.substring(0, start))
			.replace(end, len, data.substring(end, len)).toString();
    }
    
    /**
     * 转化对象为字符串
     * @param obj 对象
     * @return 字符串
     */
    public static String getEmptyStr(Object obj) {
    	return obj == null ? StringUtils.EMPTY : String.valueOf(obj);
    }
	
    /**
     * 构造URL参数
     * @param request Servlet请求对象
     * @return URL参数
     */
	@SuppressWarnings("unchecked")
	public static String getParamString(HttpServletRequest request) {
		StringBuilder result = new StringBuilder();
		
		try {
			Set<Entry<String, String[]>> entrySet = request.getParameterMap().entrySet();
			for (Entry<String, String[]> entry : entrySet) {
				result.append(entry.getKey()).append("=").append(entry.getValue()[0]).append("&");
			}
			if (result.length() > 0) {
				logger.info(result.toString());
				result.setLength(result.length() - 1);
				logger.info(result.toString());
			}
		} catch (Exception e) {
			logger.error("生成参数字符串失败", e);
		}
		
		return result.toString();
	}
	
	/**
	 * 正则校验
	 * @param regex 正则表达式
	 * @param value 校验值
	 * @return true-符合正则，false-不符合
	 */
	public static boolean validateRegex(String regex, String value) {
	    if (StringUtils.isEmpty(value)) {
	        return true;
	    }
	    
	    return Pattern.compile(regex).matcher(value).find();
	}
	
}
