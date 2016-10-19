package com.yongda.site.app.form.wx;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import com.netfinworks.site.core.common.constants.RegexRule;

public class MobileMessageForm {
	@NotBlank(message = "手机号不能为空")
	@Pattern(regexp = RegexRule.MOBLIE, message = "没有这样的手机号哦")
	private String username;
	
	/**类型   登陆（login） 注册（register）*****/
	@NotBlank(message = "类型不能为空")
	private String type;
	
	@NotBlank(message = "图片验证码不能为空")
	private String vercode;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getVercode() {
		return vercode;
	}

	public void setVercode(String vercode) {
		this.vercode = vercode;
	}
	
	
}
