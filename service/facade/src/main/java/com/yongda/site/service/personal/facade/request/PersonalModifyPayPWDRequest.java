package com.yongda.site.service.personal.facade.request;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

public class PersonalModifyPayPWDRequest extends BaseRequest {
	@NotBlank(message="oldPasswd 不能为空")
	@Size(min=7,max=23,message="oldPasswd 必须为7-23位")
	private String oldPasswd;
	@NotBlank(message="newPasswd 不能为空")
	@Size(min=7,max=23,message="newPasswd 必须为7-23位")
	private String newPasswd;
	@NotBlank(message="renewPasswd 不能为空")
	@Size(min=7,max=23,message="renewPasswd 必须为7-23位")
	private String renewPasswd;
	public String getOldPasswd() {
		return oldPasswd;
	}
	public void setOldPasswd(String oldPasswd) {
		this.oldPasswd = oldPasswd;
	}
	public String getNewPasswd() {
		return newPasswd;
	}
	public void setNewPasswd(String newPasswd) {
		this.newPasswd = newPasswd;
	}
	public String getRenewPasswd() {
		return renewPasswd;
	}
	public void setRenewPasswd(String renewPasswd) {
		this.renewPasswd = renewPasswd;
	}
	
	

}
