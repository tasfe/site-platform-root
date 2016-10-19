package com.netfinworks.site.domain.domain.request;

public class UnionmaRegisterRequest extends UnionmaRequestParam {
	private String memberId; // 会员Id
	private String registerType; // 注册类别
	private String loginName;// 登录名
	private String personIdentiy; // 人员身份 1	个人会员  2.企业会员  3.商户
	public String getMemberId() {
		return memberId;
	}
	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}
	public String getRegisterType() {
		return registerType;
	}
	public void setRegisterType(String registerType) {
		this.registerType = registerType;
	}
	public String getLoginName() {
		return loginName;
	}
	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}
	public String getPersonIdentiy() {
		return personIdentiy;
	}
	public void setPersonIdentiy(String personIdentiy) {
		this.personIdentiy = personIdentiy;
	}
	
}
