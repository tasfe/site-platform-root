package com.yongda.site.domain.domain.message;

import com.netfinworks.site.core.common.util.PageQuery;

public class PersonalMsgQuery {
	private PageQuery queryBase;
	
	private String  appId;
    private String  msgType;
    private String  title;
    private Boolean isRead;
    private String status;
    private String templateId;
    private String memberId;
	
	public PageQuery getQueryBase() {
		return queryBase;
	}
	public void setQueryBase(PageQuery queryBase) {
		this.queryBase = queryBase;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getMsgType() {
		return msgType;
	}
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Boolean getIsRead() {
		return isRead;
	}
	public void setIsRead(Boolean isRead) {
		this.isRead = isRead;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getTemplateId() {
		return templateId;
	}
	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}
	public String getMemberId() {
		return memberId;
	}
	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}
}
