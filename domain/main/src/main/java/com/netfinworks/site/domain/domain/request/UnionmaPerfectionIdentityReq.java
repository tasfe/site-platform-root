package com.netfinworks.site.domain.domain.request;

public class UnionmaPerfectionIdentityReq extends UnionmaRequestParam {
	private String memberId; // 会员Id
	private String loginPassword; // 登录密码
	private String ensureLoginPassword; // 确认登录密码
	private String payPassword; // 支付密码
	private String ensurePayPassword; // 确认支付密码
	
	private String realName; // 实际姓名
	private String idCardNum; // 身份证号
	private String ensureIdCardNum; // 确认身份证号
	private String idCardType; // 身份证类型
	public String getMemberId() {
		return memberId;
	}
	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}
	public String getLoginPassword() {
		return loginPassword;
	}
	public void setLoginPassword(String loginPassword) {
		this.loginPassword = loginPassword;
	}
	public String getEnsureLoginPassword() {
		return ensureLoginPassword;
	}
	public void setEnsureLoginPassword(String ensureLoginPassword) {
		this.ensureLoginPassword = ensureLoginPassword;
	}
	public String getPayPassword() {
		return payPassword;
	}
	public void setPayPassword(String payPassword) {
		this.payPassword = payPassword;
	}
	public String getEnsurePayPassword() {
		return ensurePayPassword;
	}
	public void setEnsurePayPassword(String ensurePayPassword) {
		this.ensurePayPassword = ensurePayPassword;
	}
	public String getRealName() {
		return realName;
	}
	public void setRealName(String realName) {
		this.realName = realName;
	}
	public String getIdCardNum() {
		return idCardNum;
	}
	public void setIdCardNum(String idCardNum) {
		this.idCardNum = idCardNum;
	}
	public String getEnsureIdCardNum() {
		return ensureIdCardNum;
	}
	public void setEnsureIdCardNum(String ensureIdCardNum) {
		this.ensureIdCardNum = ensureIdCardNum;
	}
	public String getIdCardType() {
		return idCardType;
	}
	public void setIdCardType(String idCardType) {
		this.idCardType = idCardType;
	}
	
}
