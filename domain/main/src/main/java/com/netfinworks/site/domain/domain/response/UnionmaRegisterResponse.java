package com.netfinworks.site.domain.domain.response;

public class UnionmaRegisterResponse extends UnionmaBaseResponse {
	private String operatorId;
	private String accountId;
	private String memberId;
	private String hryId;
	public String getOperatorId() {
		return operatorId;
	}
	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}
	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	public String getMemberId() {
		return memberId;
	}
	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}
	public String getHryId() {
		return hryId;
	}
	public void setHryId(String hryId) {
		this.hryId = hryId;
	}
	
}
