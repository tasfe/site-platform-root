package com.yongda.site.service.personal.facade.request;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.constraints.NotBlank;

/**
 * 签约推进
 * @author admin
 *
 */
public class PersonalSignAdvanceRequest extends BaseRequest{
	@NotBlank(message = "授权令牌不能为空")
    private String token;
    
	@NotBlank(message = "短信校验码不能为空")
    private String smsCode;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getSmsCode() {
		return smsCode;
	}

	public void setSmsCode(String smsCode) {
		this.smsCode = smsCode;
	}

	/**
	 * 用于日志输出  
	 * ToStringStyle.SHORT_PREFIX_STYLE   对象简称
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
    
}
