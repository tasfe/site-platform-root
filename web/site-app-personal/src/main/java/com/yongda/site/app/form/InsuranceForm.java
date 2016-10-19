package com.yongda.site.app.form;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

public class InsuranceForm implements Serializable{

	private static final long serialVersionUID = -4960200021742983049L;
	
    /**保险公司*/
    @NotNull(message = "保险公司名称不能为空")
    private String            company;
    
    @NotNull(message = "保单号不能为空")
    private String            policyNo;
    
    @NotNull(message = "被保人身份证号不能为空")
    private String            cardNo;
    
    @NotNull(message = "被保人姓名不能为空")
    private String            insuredName;
    
    /** 被保人手机号 */
    private String			  insruedMobile;
    
    /** 发动机编号 */
    private String			  carEngineId;

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getPolicyNo() {
		return policyNo;
	}

	public void setPolicyNo(String policyNo) {
		this.policyNo = policyNo;
	}

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	public String getInsuredName() {
		return insuredName;
	}

	public void setInsuredName(String insuredName) {
		this.insuredName = insuredName;
	}

	public String getInsruedMobile() {
		return insruedMobile;
	}

	public void setInsruedMobile(String insruedMobile) {
		this.insruedMobile = insruedMobile;
	}

	public String getCarEngineId() {
		return carEngineId;
	}

	public void setCarEngineId(String carEngineId) {
		this.carEngineId = carEngineId;
	}
}
