package com.yongda.site.app.form.dc;

import java.io.Serializable;

import javax.validation.constraints.Pattern;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.constraints.NotBlank;

import com.netfinworks.site.core.common.constants.RegexRule;

/**
 * 点车成金注册请求实体类
 * @author admin
 *
 */
public class DcRegisterRequest implements Serializable{
	
	private static final long serialVersionUID = -7459621608663966275L;

	@NotBlank(message = "手机号不能为空")
	@Pattern(regexp = RegexRule.MOBLIE, message = "没有这样的手机号哦")
	private String login_name;//手机号 登录账号
	
	@NotBlank(message = "手机验证码不能为空")
	@Pattern(regexp = RegexRule.MOBILE_VERCODE, message = "短信验证码格式不正确(只能输入6位纯数字)")
	private String code;//手机验证码
	
	@NotBlank(message = "登录密码不能为空")
	@Pattern(regexp = RegexRule.PAY_PWD_VERIFY, message = "请输入7-23位数字,字母或特殊符号相结合的登录密码")
	private String login_pwd;//登录密码
	
	private String invit_code;//邀请码 可为空


	public String getLogin_name() {
		return login_name;
	}



	public void setLogin_name(String login_name) {
		this.login_name = login_name;
	}



	public String getCode() {
		return code;
	}



	public void setCode(String code) {
		this.code = code;
	}



	public String getLogin_pwd() {
		return login_pwd;
	}



	public void setLogin_pwd(String login_pwd) {
		this.login_pwd = login_pwd;
	}



	public String getInvit_code() {
		return invit_code;
	}



	public void setInvit_code(String invit_code) {
		this.invit_code = invit_code;
	}



	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
	
}
