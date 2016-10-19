package com.yongda.site.service.personal.facade.request;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;


public class PersonalShortcutSigRequest extends BaseRequest{
	@NotBlank(message = "用户ID不能为空")
    @Size(max = 19, message = "用户ID不能超过20位")
	protected String memberId;
	
	/*@NotNull(message = "用户平台类型不能为空")
	protected String platformType;*/
	
	/*@NotNull(message = "支付类型不能为空")
    protected PayMode payMode;*/
	
	@NotBlank(message = "目标机构不能为空")
    protected String targetInst;
	
	@NotBlank(message = "目标机构名称不能为空")
    protected String targetInstName;
	
	@NotBlank(message = "卡类型不能为空")
    protected String dbcr;
    
	@NotBlank(message = "卡号不能为空")
    @Size(min = 6, max = 19, message = "银行卡号要求6-19位")
	private String cardNo;
    
   /* @NotNull(message = "证件类型不能为空")
    private String certType;*/
    
	@NotBlank(message = "证件号不能为空")
    private String certNo;
    
	@NotBlank(message = "持卡人姓名不能为空")
    private String name;
    
	@NotBlank(message = "银行预留手机号不能为空")
    @Size(min = 11, max = 11, message = "手机号要求11位")
    private String phoneNo;
    
    /*@NotNull(message = "短信下发方式不能为空")
    private String smsSendType;*/
    
    private String validDate;
    
    private String cvv2;
    
    @NotBlank(message = "请求号不能为空")
	protected String requestId;
	
    //@NotBlank(message = "平台方ID不能为空")
    //@Size(min = 1, max = 12, message = "平台方ID长度校验失败,要求1-12位")
    private String    partnerId;
    
    private String    operatorId;
	
	private String acessInfo;

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	
	public String getTargetInst() {
		return targetInst;
	}

	public void setTargetInst(String targetInst) {
		this.targetInst = targetInst;
	}

	public String getTargetInstName() {
		return targetInstName;
	}

	public void setTargetInstName(String targetInstName) {
		this.targetInstName = targetInstName;
	}

	public String getDbcr() {
		return dbcr;
	}

	public void setDbcr(String dbcr) {
		this.dbcr = dbcr;
	}

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}


	public String getCertNo() {
		return certNo;
	}

	public void setCertNo(String certNo) {
		this.certNo = certNo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}


	public String getValidDate() {
		return validDate;
	}

	public void setValidDate(String validDate) {
		this.validDate = validDate;
	}

	public String getCvv2() {
		return cvv2;
	}

	public void setCvv2(String cvv2) {
		this.cvv2 = cvv2;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getPartnerId() {
		return partnerId;
	}

	public void setPartnerId(String partnerId) {
		this.partnerId = partnerId;
	}

	public String getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}

	public String getAcessInfo() {
		return acessInfo;
	}

	public void setAcessInfo(String acessInfo) {
		this.acessInfo = acessInfo;
	}
	
}
