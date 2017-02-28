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
public enum MemoType {
	//劳务费、归还个人借款、奖励、债权转账、产权转让、产品购销以及其他
	TYPE_OTHERS(0, "其他"),
	TYPE_SERVICE_CHARGE(1, "劳务费"),
	TYPE_DEBT(2, "归还个人借款"),
	TYPE_AWARD(3, "奖励"),
	TYPE_CREDITOR_RIGHT(4, "债权转账"),
	TYPE_PROPERTY_RIGHT(5, "产权转让"),
	TYPE_PRODUCT_MARKET(6, "产品购销");
	
	// 编号
	private int code;
	
	// 描述
	private String desc;
	
	private MemoType(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}
	
	/**
	 * 根据编号获取描述
	 * @param code 编号
	 * @return 描述
	 */
	public static String getDesc(int code) {
		for (MemoType type : MemoType.values()) {
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
