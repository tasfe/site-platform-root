/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年7月11日
 */
package com.netfinworks.site.web.util;

import java.util.List;

/**
 * @author xuwei
 * @date 2014年7月11日
 */
public class ObjectUtils {
	public static boolean isListEmpty(List<?> list) {
		if (list == null || list.size() == 0) {
			return true;
		}
		return false;
	}
	
	public static boolean isListNotEmpty(List<?> list) {
		return !isListEmpty(list);
	}
}
