package com.yongda.site.domain.domain.message;

import java.util.Date;

public class PersonalMessage {
	private String appId;
	private String msgOrderNo;
	private String msgId;
	private String subject;
	private String content;
	private Date gmtCreate;
	private boolean isRead;
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getMsgOrderNo() {
		return msgOrderNo;
	}
	public void setMsgOrderNo(String msgOrderNo) {
		this.msgOrderNo = msgOrderNo;
	}
	public String getMsgId() {
		return msgId;
	}
	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Date getGmtCreate() {
		return gmtCreate;
	}
	public void setGmtCreate(Date gmtCreate) {
		this.gmtCreate = gmtCreate;
	}
	public boolean isRead() {
		return isRead;
	}
	public void setRead(boolean isRead) {
		this.isRead = isRead;
	}
}
