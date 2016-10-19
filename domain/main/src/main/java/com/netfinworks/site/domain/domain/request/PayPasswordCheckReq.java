package com.netfinworks.site.domain.domain.request;

public class PayPasswordCheckReq extends UnionmaRequestParam {
	private static final long serialVersionUID = -3092977682563186220L;
	private String payPassword;
	private String accountId;
	private String operatorId;
	public String getPayPassword() {
		return payPassword;
	}
	public void setPayPassword(String payPassword) {
		this.payPassword = payPassword;
	}
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
	
	
}
