/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年7月17日
 */
package com.netfinworks.site.web.action.form;

import java.io.Serializable;

/**
 * 银行账户联系人Form
 * @author xuwei
 * @date 2014年7月17日
 */
public class BankContactsForm implements Serializable {
	private static final long serialVersionUID = 3640924444451207157L;

	/** 联系人ID */
	private String contactId;
	
	/** 户名 */
	private String accountName;
	
	/** 开户支行 */
	private String branchName;
	
	/** 行号 */
	private String branchCode;
	
	/** 银行账号 */
	private String accountNo;
	
	/** 确认银行账号 */
	private String confirmAccountNo;
	
	/** 银行编码 */
	private String bankCode;
	
	/** 银行名称 */
	private String bankName;
	
	/** 省份 */
	private String province;
	
	/** 城市 */
	private String city;
	
	/** 备注 */
	private String bankRemark;
	
	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public String getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}

	public String getConfirmAccountNo() {
		return confirmAccountNo;
	}

	public void setConfirmAccountNo(String confirmAccountNo) {
		this.confirmAccountNo = confirmAccountNo;
	}

	public String getBankRemark() {
		return bankRemark;
	}

	public void setBankRemark(String bankRemark) {
		this.bankRemark = bankRemark;
	}

	public String getBranchCode() {
		return branchCode;
	}

	public void setBranchCode(String branchCode) {
		this.branchCode = branchCode;
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
	
}
