/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年7月11日
 */
package com.netfinworks.site.web.common.constant;

import org.apache.commons.lang.StringUtils;

/**
 * 联系人类型
 * @author xuwei
 * @date 2014年7月11日
 */
public enum ContactsType {
	TYPE_SYS("0", "系统账户联系人"),
	TYPE_BANK("1", "银行账户联系人");
	
	// 编号
	private String code;
	
	// 描述
	private String desc;
	
	private ContactsType(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}
	
	/**
	 * 根据编号获取描述
	 * @param code 编号
	 * @return 描述
	 */
	public static String getDesc(String code) {
		for (ContactsType type : ContactsType.values()) {
			if (type.code == code) {
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
