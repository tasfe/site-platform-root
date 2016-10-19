package com.yongda.site.service.personal.facade.request;

import javax.validation.constraints.Pattern;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.constraints.NotBlank;

import com.netfinworks.site.core.common.constants.RegexRule;

/**
 * 身份证
 * @author admin
 *
 */
public class PersonalIdentityRequest extends BaseRequest{
	@NotBlank(message = "真实姓名不能为空")
	private String  realname;//真实姓名
	
	@NotBlank(message = "证件类型不能为空")
	private String cardtype;//证件类型
	
	@NotBlank(message = "证件号码不能为空")
	@Pattern(regexp = RegexRule.ID_CARD_18X, message = "身份证号格式不正确（15位全数字或18位最后一位为数字或字母）")
	private String idcard;//证件号码
	
	@NotBlank(message = "会员id不能为空")
	private String memberId;//会员id
	//@NotBlank(message = "操作员id不能为空")
	private String operatorId; //操作员
	
	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}

	public String getCardtype() {
		return cardtype;
	}

	public void setCardtype(String cardtype) {
		this.cardtype = cardtype;
	}

	public String getIdcard() {
		return idcard;
	}

	public void setIdcard(String idcard) {
		this.idcard = idcard;
	}

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public String getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}

	@Override
	public String toString() {
		 return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
}
