package com.netfinworks.site.domain.domain.request;

public class loginPasswordEditReq extends UnionmaRequestParam {
	private String operatorId; // 操作员Id
	private String oldPassword; // 旧密码
	private String newPassword; // 新密码
	private String ensureNewPassword; // 确认密码
	private String memberType; // 会员类型 
	private String loginName; // 登录名
	public String getOperatorId() {
		return operatorId;
	}
	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}
	public String getOldPassword() {
		return oldPassword;
	}
	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}
	public String getNewPassword() {
		return newPassword;
	}
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	public String getEnsureNewPassword() {
		return ensureNewPassword;
	}
	public void setEnsureNewPassword(String ensureNewPassword) {
		this.ensureNewPassword = ensureNewPassword;
	}
	public String getMemberType() {
		return memberType;
	}
	public void setMemberType(String memberType) {
		this.memberType = memberType;
	}
	public String getLoginName() {
		return loginName;
	}
	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}
	
	
}
