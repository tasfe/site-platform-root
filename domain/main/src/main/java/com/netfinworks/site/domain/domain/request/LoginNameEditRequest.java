package com.netfinworks.site.domain.domain.request;
/*
 * 登录名修改请求
 * zhaozq
 * 20150625
 */

public class LoginNameEditRequest extends UnionmaRequestParam {
	private static final long serialVersionUID = 1044886704638357172L;
	
	private String memberId; // 会员Id
	private String operatorId; // 操作员Id
	private String loginName; // 登录名
	private String oldName; // 原登陆名
	private String loginNameType; // 登录名类别
	
	public LoginNameEditRequest() {}
	public LoginNameEditRequest(String memberId, String operatorId, String loginName, String oldName, String loginNameType) {
		super();
		this.memberId = memberId;
		this.operatorId = operatorId;
		this.loginName = loginName;
		this.oldName = oldName;
		this.loginNameType = loginNameType;
	}
	
	public String getMemberId() {
		return memberId;
	}
	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}
	public String getOperatorId() {
		return operatorId;
	}
	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}
	public String getLoginName() {
		return loginName;
	}
	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}
	public String getLoginNameType() {
		return loginNameType;
	}
	public void setLoginNameType(String loginNameType) {
		this.loginNameType = loginNameType;
	}
	public String getOldName() {
		return oldName;
	}
	public void setOldName(String oldName) {
		this.oldName = oldName;
	}
}
