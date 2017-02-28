/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年7月11日
 */
package com.yongda.site.wallet.app.common.constant;

import org.apache.commons.lang.StringUtils;

/**
 * 转账类型
 * @author xuwei
 * @date 2014年7月11日
 */
public enum PayoffType {
	PAYOFF_TO_KJT(0, "发到永达互联网金融账户"),
	PAYOFF_TO_BANK(1, "银行账户");
	
	// 编号
	private int code;
	
	// 描述
	private String desc;
	
	private PayoffType(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}
	
	/**
	 * 根据编号获取描述
	 * @param code 编号
	 * @return 描述
	 */
	public static String getDesc(int code) {
		for (PayoffType type : PayoffType.values()) {
			if (type.code == code) {
				return type.getDesc();
			}
		}
		return StringUtils.EMPTY;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
	
}
