/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年7月21日
 */
package com.yongda.site.app.common.constant;

import org.apache.commons.lang.StringUtils;

/**
 * 按交易类型划分的产品编码
 * @author xuwei
 * @date 2014年7月21日
 */
public enum TradeProductType {
	TIMELY_TRADE("20201", "及时到账"),
	COLLATERAL_TRADE("20202", "担保交易"),
	RECHARGE("10101", "充值"),
	CASHING("10210", "提现"),
	PAY_TO_CARD("10220", "付款到卡"),
	MEMBER_TRANSFER("10310", "会员转账");
	
	// 编号
	private String code;
	
	// 描述
	private String desc;
	
	private TradeProductType(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}
	
	/**
	 * 根据编号获取描述
	 * @param code 编号
	 * @return 描述
	 */
	public static String getDesc(int code) {
		for (TradeProductType type : TradeProductType.values()) {
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
