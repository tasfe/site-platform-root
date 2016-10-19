package com.yongda.site.app.form;

import javax.validation.constraints.Pattern;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.constraints.NotBlank;

import com.netfinworks.site.core.common.constants.RegexRule;

/**
 * 身份校验
 * @author admin
 *
 */
public class checkIdentityRequest{
	@NotBlank(message = "真实姓名不能为空")
	private String  realname;//真实姓名
	
	@NotBlank(message = "证件号码不能为空")
	@Pattern(regexp = RegexRule.ID_CARD_18X, message = "身份证号格式不正确")
	private String idcard;//证件号码
	
	@NotBlank(message = "用户不能为空")
	private String username;//会员id
	
	@NotBlank(message = "请求超时")
	private String token;
	
	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}

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
