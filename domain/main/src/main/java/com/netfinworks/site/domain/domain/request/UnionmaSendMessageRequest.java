package com.netfinworks.site.domain.domain.request;

public class UnionmaSendMessageRequest extends UnionmaRequestParam {
	private String memberId; // 会员Id/N
	private String bizType; // 业务类型 /Y
	private Long validity; // 有效时间/N
	private String mobile; // 手机号 /Y
	public String getMemberId() {
		return memberId;
	}
	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}
	public String getBizType() {
		return bizType;
	}
	public void setBizType(String bizType) {
		this.bizType = bizType;
	}
	public Long getValidity() {
		return validity;
	}
	public void setValidity(Long validity) {
		this.validity = validity;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
}
