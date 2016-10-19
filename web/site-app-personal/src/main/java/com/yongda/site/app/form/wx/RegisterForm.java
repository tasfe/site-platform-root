package com.yongda.site.app.form.wx;

import javax.validation.constraints.Pattern;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.constraints.NotBlank;

import com.netfinworks.site.core.common.constants.RegexRule;

/**
 * 注册
 * @author admin
 *
 */
public class RegisterForm extends WxBaseForm{
	
	/*@NotBlank(message = "登录密码不能为空")
	@Pattern(regexp = RegexRule.PWD_VERIFY, message = "请输入6-20位数字和字母组合密码")*/
	private String loginPassword;
	
	@NotBlank(message = "图形验证码不能为空")
	private String vercode;
	
	public String getVercode() {
		return vercode;
	}
	public void setVercode(String vercode) {
		this.vercode = vercode;
	}
	
	public String getLoginPassword() {
		return loginPassword;
	}


	public void setLoginPassword(String loginPassword) {
		this.loginPassword = loginPassword;
	}


	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
	
}
