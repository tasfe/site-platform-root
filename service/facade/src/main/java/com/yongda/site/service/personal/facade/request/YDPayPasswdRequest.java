package com.yongda.site.service.personal.facade.request;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

public class YDPayPasswdRequest extends BaseRequest{
    /**支付密码*/
	@NotBlank(message="password 不能为空")
	@Size(min=7,max=23,message="password 必须为7-23位")
    private String            password;
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	

}
