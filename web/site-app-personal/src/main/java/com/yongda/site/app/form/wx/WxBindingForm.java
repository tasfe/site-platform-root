package com.yongda.site.app.form.wx;

import javax.validation.constraints.Pattern;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.constraints.NotBlank;

import com.netfinworks.site.core.common.constants.RegexRule;

/**
 * 微信绑定 /登录 
 * @author admin
 *
 */
public class WxBindingForm extends WxBaseForm{
	
	@NotBlank(message = "图形验证码不能为空")
	private String vercode;
	
	public String getVercode() {
		return vercode;
	}
	public void setVercode(String vercode) {
		this.vercode = vercode;
	}
	
}
