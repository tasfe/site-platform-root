package com.netfinworks.site.web.action.form;

import java.io.Serializable;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import com.netfinworks.site.core.common.constants.RegexRule;

/**
 * <p>
 * 银行卡认证信息
 * </p>
 * 
 * @author liuchen
 * @version 创建时间：2015-2-9 下午7:41:38
 */
public class BankCardForm implements Serializable {

	private static final long serialVersionUID = 2867567464984599815L;

	@NotBlank(message = "银行名称不能为空")
	private String bankName;// 银行名称
	@NotBlank(message = "银行编码不能为空")
	private String bankCode;// 银行编码
	@NotBlank(message = "支行名称不能为空")
	private String branchName;// 支行名称
	@NotBlank(message = "联行号不能为空")
	private String branchNo;// 联行号
	@NotBlank(message = "银行卡类型不能为空")
	private String cardType;// 卡类型 1-借记卡 2-信用卡
	@NotBlank(message = "银行卡属性不能为空")
	private String cardAttribute;// 卡属性 0-对公 1-对私
	@NotBlank(message = "省份名称不能为空")
	private String provName; // 省份名称
	@NotBlank(message = "城市名称不能为空")
	private String cityName; // 城市名称
	@NotBlank(message = "卡号不能为空")
	@Pattern(regexp = RegexRule.BANKCARD_NO, message = "卡号格式不正确（8-32位数字）")
	private String bankAccountNum; // 卡号

	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public String getBankCode() {
		return bankCode;
	}
	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
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
	public String getCardType() {
		return cardType;
	}
	public void setCardType(String cardType) {
		this.cardType = cardType;
	}
	public String getCardAttribute() {
		return cardAttribute;
	}
	public void setCardAttribute(String cardAttribute) {
		this.cardAttribute = cardAttribute;
	}
	public String getProvName() {
		return provName;
	}
	public void setProvName(String provName) {
		this.provName = provName;
	}
	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	public String getBankAccountNum() {
		return bankAccountNum;
	}
	public void setBankAccountNum(String bankAccountNum) {
		this.bankAccountNum = bankAccountNum;
	}



}
