package com.yongda.site.service.personal.facade.request;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.constraints.NotBlank;

import com.netfinworks.site.service.facade.model.WxNotifyData;

public class WxMessageRequest{
	@NotBlank(message = "memberId不能为空")
	private String memberId;
	
	@NotBlank(message = "templateid不能为空")
	private String templateId;
	
	private WxNotifyData templateData;
	
	private String openid;//微信Openid
	
	
	
	public String getMemberId() {
		return memberId;
	}



	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}



	public String getTemplateId() {
		return templateId;
	}



	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}



	public WxNotifyData getTemplateData() {
		return templateData;
	}



	public void setTemplateData(WxNotifyData templateData) {
		this.templateData = templateData;
	}



	public String getOpenid() {
		return openid;
	}



	public void setOpenid(String openid) {
		this.openid = openid;
	}



	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
}
