package com.netfinworks.site.domain.domain.request;

public class UnionmaBindMobileEditRequest extends UnionmaRequestParam {
	/**
	 * 会员编号
	 */
	private String memberId;
	/**
	 * 新的绑定手机号码
	 */
	private String newPhoneNumber;

	public String getMemberId() {
		return memberId;
	}
	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}
	public String getNewPhoneNumber() {
		return newPhoneNumber;
	}
	public void setNewPhoneNumber(String newPhoneNumber) {
		this.newPhoneNumber = newPhoneNumber;
	}
}
