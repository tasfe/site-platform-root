/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年7月11日
 */
package com.yongda.site.app.common.constant;

import org.apache.commons.lang.StringUtils;

/**
 * 提现类型
 * @author xuwei
 * @date 2014年7月11日
 */
public enum CashingType {
	TYPE_T1(0, "普通提现（次日到账，非工作日顺延）"),
	TYPE_2H(1, "快速提现（2小时到账）");
	
	// 编号
	private int code;
	
	// 描述
	private String desc;
	
	private CashingType(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}
	
	/**
	 * 根据编号获取描述
	 * @param code 编号
	 * @return 描述
	 */
	public static String getDesc(int code) {
		for (CashingType type : CashingType.values()) {
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
