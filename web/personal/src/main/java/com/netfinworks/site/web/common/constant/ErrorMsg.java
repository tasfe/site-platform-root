/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年7月21日
 */
package com.netfinworks.site.web.common.constant;

import org.apache.commons.lang.StringUtils;

import com.netfinworks.site.core.common.constants.CommonConstant;

/**
 * 功能权限类型
 * @author xuwei
 * @date 2014年7月21日
 */
public enum ErrorMsg {
	ERROR_EXCEL_FORMAT("ERROR_EXCEL_FORMAT", "请上传以xls、xlsx格式结尾的文件!"),
	ERROR_TRANSFER_COUNT("ERROR_EXCEL_COUNT", "表格仅支持" + CommonConstant.MAX_IMPORT_COUNT + "笔转账"),
	ERROR_PAYOFF_COUNT("ERROR_PAYOFF_COUNT", "代发工资一次最多支持发放" + CommonConstant.MAX_IMPORT_COUNT + "笔工资");
	
	// 编号
	private String code;
	
	// 描述
	private String desc;
	
	private ErrorMsg(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}
	
	/**
	 * 根据编号获取描述
	 * @param code 编号
	 * @return 描述
	 */
	public static String getDesc(int code) {
		for (ErrorMsg type : ErrorMsg.values()) {
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
