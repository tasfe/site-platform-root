package com.netfinworks.site.domain.domain.response;

public class UnionmaBindMobileEditResponse extends UnionmaBaseResponse {
	/**
	 * 业务名称
	 */
	private String 			bizType;
	
	/**
	 * 绑定的手机号码（隐藏后）
	 */
	private String 			mobile;

	public String getBizType() {
		return bizType;
	}

	public void setBizType(String bizType) {
		this.bizType = bizType;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
	
}
