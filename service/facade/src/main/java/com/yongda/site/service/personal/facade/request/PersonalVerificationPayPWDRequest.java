package com.yongda.site.service.personal.facade.request;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import com.yongda.site.core.common.annotation.HttpField;

public class PersonalVerificationPayPWDRequest extends BaseRequest {
	@NotBlank(message="passwd 不能为空")
	@Size(min=7,max=23,message="passwd 必须为7-23位")
	@HttpField(name="password")
	private String passwd;
	
	

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}



	
	
	
}
