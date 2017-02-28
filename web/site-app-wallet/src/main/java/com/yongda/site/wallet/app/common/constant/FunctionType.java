/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年7月21日
 */
package com.yongda.site.wallet.app.common.constant;

import org.apache.commons.lang.StringUtils;

/**
 * 功能权限类型
 * @author xuwei
 * @date 2014年7月21日
 */
public enum FunctionType {
	EW_MY_APPROVE("EW_MY_APPROVE", "我的待审核"),
	EW_RECHARGE("EW_RECHARGE", "充值"),
	EW_WITHDRAW("EW_TRANSFER", "提现"),
	EW_TRANSFER("EW_TRANSFER", "转帐"),
	EW_WITHDRAW_AUDIT("EW_WITHDRAW_AUDIT", "提现审核"),
	EW_TRANSFER_AUDIT("EW_TRANSFER_AUDIT","转账审核"),
	EW_REFUND_AUDIT("EW_REFUND_AUDIT","退款审核");
	// 编号
	private String code;
	
	// 描述
	private String desc;
	
	private FunctionType(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}
	
	/**
	 * 根据编号获取描述
	 * @param code 编号
	 * @return 描述
	 */
	public static String getDesc(int code) {
		for (FunctionType type : FunctionType.values()) {
			if (type.code.equals(code)) {
				return type.getDesc();
			}
		}
		return StringUtils.EMPTY;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
}
