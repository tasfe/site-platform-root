package com.yongda.site.app.form;

import javax.validation.constraints.Pattern;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.constraints.NotBlank;

import com.netfinworks.site.core.common.constants.RegexRule;

/**
 * 支付密码
 * @author admin
 *
 */
public class PayPwdRequest{
	/*@NotBlank(message = "证件号码不能为空")
	@Pattern(regexp = RegexRule.ID_CARD_18X, message = "身份证号格式不正确（15位全数字或18位最后一位为数字或字母）")*/
	private String idcard;//证件号码
	
	@NotBlank(message = "username不能为空")
	private String username;//会员id
	 
	@NotBlank(message = "支付密码不能为空")
	@Pattern(regexp = RegexRule.PAY_PWD_VERIFY, message = "请输入7-23位数字和字母组合密码")
	private String password;
	
	@NotBlank(message = "请求超时")
	private String token;
	
	public String getIdcard() {
		return idcard;
	}

	public void setIdcard(String idcard) {
		this.idcard = idcard;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@Override
	public String toString() {
		 return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
}
