/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年7月11日
 */
package com.netfinworks.site.web.action.form;
import java.io.Serializable;

import javax.validation.constraints.Pattern;
import com.netfinworks.site.core.common.constants.RegexRule;

/**
 * 银行转账页面输入Form
 * @author xuwei
 * @date 2014年7月11日
 */
public class BankTransferForm implements Serializable {
	private static final long serialVersionUID = 1143145292411583072L;

	// 账户类型，0-个人账户，1-企业账户
	private int accountType;

	// 收款方银行开户名
	private String recvAcctName;
	
	// 目标账户
	private String accountNo;
	
	// 确认目标账户
	private String confirmAccountNo;
	
	// 银行名称
	private String bankName;
	
	// 联行号
	private String branchNo;
	
	// 支行信息
	private String branchName;
	
	// 支行简称
	private String branchShortName;
	
	// 转账金额
	@Pattern(regexp = RegexRule.AMOUNT_2_DECINALS, message = "转账金额格式不正确（小数点前最多19位，小数点后最多2位）")
	private String transMoney;
	
	// 服务费
	@Pattern(regexp = RegexRule.AMOUNT_2_DECINALS, message = "服务费格式不正确（小数点前最多19位，小数点后最多2位）")
	private String serviceCharge;
	
	// 总额
	@Pattern(regexp = RegexRule.AMOUNT_2_DECINALS, message = "总金额格式不正确（小数点前最多19位，小数点后最多2位）")
	private String totalMoney;
	
	// 账户可用余额
	private String avaBalance;
	
	// 备注类型1-工资，2-还款，0-其他
	private int remarkType;
	
	// 操作员登录名
	private String operLoginName;
	
	// 短信通知到的手机
	private String mobile;
	
	// 是否发送短信0-不发送，1-发送
	private int sendNoteMsg;
	
	// 省份
	private String province;
	
	// 城市
	private String city;
	
	// 备注
	private String remark;

	// 转账总数
	private int transferCount;

	// 转账成功总数
	private int transferSuccessCount;
	
	// 转账成功金额
	private String transferSuccessAmount;
	
	// 转账失败总数
	private int transferFailCount;
	
	// 转账失败金额
	private String transferFailAmount;
		
	public int getSendNoteMsg() {
		return sendNoteMsg;
	}

	public void setSendNoteMsg(int sendNoteMsg) {
		this.sendNoteMsg = sendNoteMsg;
	}

	public int getAccountType() {
		return accountType;
	}

	public void setAccountType(int accountType) {
		this.accountType = accountType;
	}

	public String getBranchNo() {
		return branchNo;
	}

	public void setBranchNo(String branchNo) {
		this.branchNo = branchNo;
	}

	public String getRecvAcctName() {
		return recvAcctName;
	}

	public void setRecvAcctName(String recvAcctName) {
		this.recvAcctName = recvAcctName;
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

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public String getTransMoney() {
		return transMoney;
	}

	public void setTransMoney(String transMoney) {
		this.transMoney = transMoney;
	}

	public String getServiceCharge() {
		return serviceCharge;
	}

	public void setServiceCharge(String serviceCharge) {
		this.serviceCharge = serviceCharge;
	}

	public String getTotalMoney() {
		return totalMoney;
	}

	public void setTotalMoney(String totalMoney) {
		this.totalMoney = totalMoney;
	}

	public int getRemarkType() {
		return remarkType;
	}

	public void setRemarkType(int remarkType) {
		this.remarkType = remarkType;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getOperLoginName() {
		return operLoginName;
	}

	public void setOperLoginName(String operLoginName) {
		this.operLoginName = operLoginName;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getAvaBalance() {
		return avaBalance;
	}

	public void setAvaBalance(String avaBalance) {
		this.avaBalance = avaBalance;
	}

	public int getTransferCount() {
		return transferCount;
	}

	public void setTransferCount(int transferCount) {
		this.transferCount = transferCount;
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

	public int getTransferSuccessCount() {
		return transferSuccessCount;
	}

	public void setTransferSuccessCount(int transferSuccessCount) {
		this.transferSuccessCount = transferSuccessCount;
	}

	public String getTransferSuccessAmount() {
		return transferSuccessAmount;
	}

	public void setTransferSuccessAmount(String transferSuccessAmount) {
		this.transferSuccessAmount = transferSuccessAmount;
	}

	public int getTransferFailCount() {
		return transferFailCount;
	}

	public void setTransferFailCount(int transferFailCount) {
		this.transferFailCount = transferFailCount;
	}

	public String getTransferFailAmount() {
		return transferFailAmount;
	}

	public void setTransferFailAmount(String transferFailAmount) {
		this.transferFailAmount = transferFailAmount;
	}

	public String getBranchShortName() {
		return branchShortName;
	}

	public void setBranchShortName(String branchShortName) {
		this.branchShortName = branchShortName;
	}

}
