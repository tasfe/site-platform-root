/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年7月11日
 */
package com.yongda.site.wallet.app.common.constant;

import org.apache.commons.lang.StringUtils;

/**
 * 审核状态
 * @author xuwei
 * @date 2014年7月11日
 */
public enum BizModuleType {
	MODULE_RECHARGE("recharge", "充值"),
	MODULE_WITHDRAW("cash", "提现"),
	MODULE_TRANSFER_KJT("transfer", "永达互联网金融转账"),
	MODULE_TRANSFER_BANK("bTransfer", "银行转账"),
	MODULE_PAYOFF("payoff", "代发工资");
	
	// 编号
	private String code;
	
	// 描述
	private String desc;
	
	private BizModuleType(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}
	
	/**
	 * 根据编号获取描述
	 * @param code 编号
	 * @return 描述
	 */
	public static String getDesc(String code) {
		for (BizModuleType type : BizModuleType.values()) {
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
