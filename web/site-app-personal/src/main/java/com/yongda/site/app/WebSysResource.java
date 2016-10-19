/**
 *
 */
package com.yongda.site.app;

import java.io.Serializable;

/**
 * @title
 * @description
 * @usage
 * @company		上海微汇信息技术有限公司
 * @author		TQSUMMER
 * @create		2012-12-14 下午7:06:17
 */
/**
 * @author TQSUMMER
 */
public final class WebSysResource implements Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 7252174432218583428L;
	private String redirectDef;

	/**
	 * @return the redirectDef
	 */
	public String getRedirectDef() {
		return redirectDef;
	}

	/**
	 * @param redirectDef
	 *            the redirectDef to set
	 */
	public void setRedirectDef(String redirectDef) {
		this.redirectDef = redirectDef;
	}

}
