/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年8月9日
 */
package com.yongda.site.app.util;

import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 常用工具类
 * @author xuwei
 * @date 2014年8月9日
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
}
