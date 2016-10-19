/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年7月15日
 */
package com.netfinworks.site.web.action.form;
import java.io.Serializable;

import javax.validation.constraints.Pattern;

import com.netfinworks.site.core.common.constants.RegexRule;

/**
 * 批量银行转账收款信息Form
 * @author xuwei
 * @date 2014年7月15日
 */
public class TransferBankBatchForm implements Serializable {
	private static final long serialVersionUID = -3706208682295910220L;
	
	// 账户类型，0-个人账户，1-企业账户
	private int accountType;
	
	// 收款开户名
	private String accountName;
	
	// 收款开户银行
	private String bankName;
	
	// 收款银行所在省份
	private String bankProvince;
	
	// 收款银行所在市
	private String bankCity;
	
	// 收款支行名称
	private String branchBankName;
	
	// 收款银行帐号
	private String accountNo;
	
	// 转账金额（元）
	@Pattern(regexp = RegexRule.AMOUNT_2_DECINALS, message = "转账金额格式不正确（小数点前最多19位，小数点后最多2位）")
	private String transferAmount;
	
	// 收款人联系手机
	private String mobile;
	
	// 转账备注
	private String remark;
	
	//商户订单号
	private String sourceDetailNo;

	public int getAccountType() {
		return accountType;
	}

	public void setAccountType(int accountType) {
		this.accountType = accountType;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getBankProvince() {
		return bankProvince;
	}

	public void setBankProvince(String bankProvince) {
		this.bankProvince = bankProvince;
	}

	public String getBankCity() {
		return bankCity;
	}

	public void setBankCity(String bankCity) {
		this.bankCity = bankCity;
	}

	public String getBranchBankName() {
		return branchBankName;
	}

	public void setBranchBankName(String branchBankName) {
		this.branchBankName = branchBankName;
	}

	public String getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}

	public String getTransferAmount() {
		return transferAmount;
	}

	public void setTransferAmount(String transferAmount) {
		this.transferAmount = transferAmount;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getSourceDetailNo() {
		return sourceDetailNo;
	}

	public void setSourceDetailNo(String sourceDetailNo) {
		this.sourceDetailNo = sourceDetailNo;
	}
	
	
}
