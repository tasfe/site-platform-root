package com.yongda.site.app.form.wx;

import javax.validation.constraints.Pattern;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.constraints.NotBlank;

import com.netfinworks.site.core.common.constants.RegexRule;

/**
 * 微信实体父类
 * @author admin
 *
 */
public class WxBaseForm {
	
	@NotBlank(message = "手机号不能为空")
	@Pattern(regexp = RegexRule.MOBLIE, message = "没有这样的手机号哦")
	private String username;
	
	@NotBlank(message = "手机验证码不能为空")
	@Pattern(regexp = RegexRule.MOBILE_VERCODE, message = "短信验证码格式不正确(只能输入6位纯数字)")
	private String mobileVercode;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getMobileVercode() {
		return mobileVercode;
	}
	public void setMobileVercode(String mobileVercode) {
		this.mobileVercode = mobileVercode;
	}
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
