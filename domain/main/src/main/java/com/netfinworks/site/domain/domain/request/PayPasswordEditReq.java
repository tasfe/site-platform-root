package com.netfinworks.site.domain.domain.request;

public class PayPasswordEditReq extends UnionmaRequestParam {
	
	private static final long serialVersionUID = 8740080303219851777L;
	
	private String accountId; // 账号Id
	private String operatorId; // 操作员Id
	private String oldPayPassword; // 旧支付密码
	private String newPayPassword; // 新支付密码
	private String ensureNewPayPassword; // 确认新支付密码
	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	public String getOperatorId() {
		return operatorId;
	}
	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}
	public String getOldPayPassword() {
		return oldPayPassword;
	}
	public void setOldPayPassword(String oldPayPassword) {
		this.oldPayPassword = oldPayPassword;
	}
	public String getNewPayPassword() {
		return newPayPassword;
	}
	public void setNewPayPassword(String newPayPassword) {
		this.newPayPassword = newPayPassword;
	}
	public String getEnsureNewPayPassword() {
		return ensureNewPayPassword;
	}
	public void setEnsureNewPayPassword(String ensureNewPayPassword) {
		this.ensureNewPayPassword = ensureNewPayPassword;
	}
}
