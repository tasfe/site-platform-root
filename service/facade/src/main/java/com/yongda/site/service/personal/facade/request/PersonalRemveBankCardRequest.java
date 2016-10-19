package com.yongda.site.service.personal.facade.request;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 删除银行卡
 * @author admin
 *
 */
public class PersonalRemveBankCardRequest extends BaseRequest{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@NotBlank(message = "会员银行卡Id不能为空")
	public String bankCardId;
	
	@NotBlank(message = "会员Id不能为空")
	private String memberId;

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}
	
	public String getBankCardId() {
		return bankCardId;
	}

	public void setBankCardId(String bankCardId) {
		this.bankCardId = bankCardId;
	}
	
}
