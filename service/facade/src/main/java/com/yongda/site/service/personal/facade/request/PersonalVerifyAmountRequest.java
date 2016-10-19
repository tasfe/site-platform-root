package com.yongda.site.service.personal.facade.request;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 金额校验
 * @author admin
 *
 */
public class PersonalVerifyAmountRequest  extends BaseRequest{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@NotBlank(message = "会员id不能为空")
	private String memberId;
	
	//@NotBlank(message = "操作员id不能为空")
	private String operatorId;
	
	@NotBlank(message = "金额不能为空")
	private String amount;

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

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}
	
	
}
