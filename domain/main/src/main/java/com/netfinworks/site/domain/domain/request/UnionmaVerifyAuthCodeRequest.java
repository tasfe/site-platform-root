package com.netfinworks.site.domain.domain.request;

public class UnionmaVerifyAuthCodeRequest extends UnionmaRequestParam {
	/**
	 * 会员id/Y
	 */
	private String memberId;

	
	/**
	 * 手机号码或者邮箱/Y
	 */
	private String verifyValue;
	
	/**
	 * 验证码/Y
	 */
	private String authCode;
	
	
	/**
	 * 业务名称/Y
	 */
	private String bizType;


	public String getMemberId() {
		return memberId;
	}


	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}


	public String getVerifyValue() {
		return verifyValue;
	}


	public void setVerifyValue(String verifyValue) {
		this.verifyValue = verifyValue;
	}


	public String getAuthCode() {
		return authCode;
	}


	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}


	public String getBizType() {
		return bizType;
	}


	public void setBizType(String bizType) {
		this.bizType = bizType;
	}
	
	
}
