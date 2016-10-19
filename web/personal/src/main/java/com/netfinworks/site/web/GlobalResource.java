/**
 *
 */
package com.netfinworks.site.web;

import java.io.Serializable;

/**
 * @title
 * @description
 * @usage
 * @company		上海微汇信息技术有限公司
 * @author		TQSUMMER
 * @create		2012-12-14 下午7:01:11
 */
/**
 * @author TQSUMMER
 */
public class GlobalResource implements Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 6750311236923703094L;

	private WebSysResource webSysRes;

	private WebDynamicResource webDynamicRes;

	/**
	 * @return the webSysRes
	 */
	public WebSysResource getWebSysRes() {
		return webSysRes;
	}

	/**
	 * @return the webDynamicRes
	 */
	public WebDynamicResource getWebDynamicRes() {
		return webDynamicRes;
	}

	/**
	 * @param webSysRes
	 *            the webSysRes to set
	 */
	public void setWebSysRes(WebSysResource webSysRes) {
		this.webSysRes = webSysRes;
	}

	/**
	 * @param webDynamicRes
	 *            the webDynamicRes to set
	 */
	public void setWebDynamicRes(WebDynamicResource webDynamicRes) {
		this.webDynamicRes = webDynamicRes;
	}

}
