package com.yongda.site.service.personal.facade.request;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import com.netfinworks.site.core.common.constants.RegexRule;
import com.netfinworks.site.domain.enums.AuthResultStatus;

public class PersonalAddBankCardRequest extends BaseRequest{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	 /**会员Id*/
	@NotBlank(message = "memberId不能为空")
    private String            memberId;
    
    /**省份*/
   // @NotNull(message = "province_not_exist")
    private String            province;
    
    /**城市*/
    private String            city;
    
    /**银行卡号*/
    private String            BankcardId;
    
    /**银行代码*/
    @NotBlank(message = "银行代码不能为空")
    @Size(min = 1,  message = "银行代码不正确")
    private String            bankCode;
    
    /**银行名称*/
    //@NotBlank(message = "bankName_not_exist")
    private String            bankName;
    
    /**分行代码*/
   // @NotNull(message = "branchcode_not_exist")
   // @Size(min = 1,  message = "branchcode_not_exist")
    private String            branchCode;
    
    /**分行名称*/
    private String            branchName;
    
    /**联行号*/
    private String            branchNo;
    
    /**银行卡号码*/
    @NotBlank(message = "银行卡号不能为空")
    @Pattern(regexp = RegexRule.BANKCARD_NO, message = "卡号格式不正确（8-32位数字）")
    private String            bankAccountNum;
    
    /**卡属性(0对公 1对私)*/
    //@NotNull(message = "cardAttribute_not_exist")
    private String            cardAttribute;
    /**枚举
     * （qpay：大快捷，bsqpay：小快捷，umppay：联动优势，normal：普通卡，trust_collect：代扣）
     */
    private String            payAttribute;
    
    /** 户名 */
    @NotBlank(message = "户名不能为空")
    private String            realName;

	/** 分行简称 */
	private String branchShortName;
	
	/** 客户端ip*/
	private String RemoteAddr; 
	
	/** 实名认证信息*/
    private AuthResultStatus  nameVerifyStatus;
    /** 预留手机号*/
    private String mobile;
    
	public String getMemberId() {
		return memberId;
	}
	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getBankcardId() {
		return BankcardId;
	}
	public void setBankcardId(String bankcardId) {
		BankcardId = bankcardId;
	}
	public String getBankCode() {
		return bankCode;
	}
	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public String getBranchCode() {
		return branchCode;
	}
	public void setBranchCode(String branchCode) {
		this.branchCode = branchCode;
	}
	public String getBranchName() {
		return branchName;
	}
	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}
	public String getBranchNo() {
		return branchNo;
	}
	public void setBranchNo(String branchNo) {
		this.branchNo = branchNo;
	}
	public String getBankAccountNum() {
		return bankAccountNum;
	}
	public void setBankAccountNum(String bankAccountNum) {
		this.bankAccountNum = bankAccountNum;
	}
	public String getCardAttribute() {
		return cardAttribute;
	}
	public void setCardAttribute(String cardAttribute) {
		this.cardAttribute = cardAttribute;
	}
	public String getPayAttribute() {
		return payAttribute;
	}
	public void setPayAttribute(String payAttribute) {
		this.payAttribute = payAttribute;
	}
	public String getRealName() {
		return realName;
	}
	public void setRealName(String realName) {
		this.realName = realName;
	}
	public String getBranchShortName() {
		return branchShortName;
	}
	public void setBranchShortName(String branchShortName) {
		this.branchShortName = branchShortName;
	}
	public String getRemoteAddr() {
		return RemoteAddr;
	}
	public void setRemoteAddr(String remoteAddr) {
		RemoteAddr = remoteAddr;
	}
	public AuthResultStatus getNameVerifyStatus() {
		return nameVerifyStatus;
	}
	public void setNameVerifyStatus(AuthResultStatus nameVerifyStatus) {
		this.nameVerifyStatus = nameVerifyStatus;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
}
