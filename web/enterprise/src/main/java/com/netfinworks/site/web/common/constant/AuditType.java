/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年7月11日
 */
package com.netfinworks.site.web.common.constant;

import org.apache.commons.lang.StringUtils;

/**
 * 审核类型
 * @author xuwei
 * @date 2014年7月11日
 */
public enum AuditType {
	AUDIT_WITHDRAW("withdraw", "提现审核"), 
	AUDIT_TRANSFER_KJT("transfer_kjt", "转账审核"), 
	AUDIT_TRANSFER_BANK("transfer_bank", "转账审核"), 
	AUDIT_TRANSFER("transfer", "批量转账到账户"), 
	AUDIT_PAY_TO_CARD("pay_to_card", "批量转账到卡"), 
	AUDIT_REFUND("refund", "退款审核"), 
	AUDIT_PAYOFF_KJT("payoff_kjt", "代发工资到账户"), 
	AUDIT_PAYOFF_BANK("payoff_bank", "代发工资到卡"),
	;
	
	// 编号
	private String code;
	
	// 描述
	private String desc;
	
	private AuditType(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}
	
	/**
	 * 根据编号获取描述
	 * @param code 编号
	 * @return 描述
	 */
	public static String getDesc(String code) {
		for (AuditType type : AuditType.values()) {
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
