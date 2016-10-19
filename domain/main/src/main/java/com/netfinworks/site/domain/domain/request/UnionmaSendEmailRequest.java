package com.netfinworks.site.domain.domain.request;

import com.netfinworks.site.domain.enums.SendType;


public class UnionmaSendEmailRequest extends UnionmaRequestParam {
	private String memberId; // 会员id
	private String email;       // email
	private String userName;  // 用户名
	private String activeUrl; // 激活url
	private String token;//缓存中随机值
	private SendType sendType;//邮件发送形式
	private String bizType;//业务名称
	public String getMemberId() {
		return memberId;
	}
	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getActiveUrl() {
		return activeUrl;
	}
	public void setActiveUrl(String activeUrl) {
		this.activeUrl = activeUrl;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public SendType getSendType() {
		return sendType;
	}
	public void setSendType(SendType sendType) {
		this.sendType = sendType;
	}
	public String getBizType() {
		return bizType;
	}
	public void setBizType(String bizType) {
		this.bizType = bizType;
	}
	
	
}
