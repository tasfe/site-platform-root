/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年8月5日
 */
package com.netfinworks.site.web.action.form;
import javax.validation.constraints.Pattern;

import com.netfinworks.site.core.common.constants.RegexRule;

/**
 * 批量代发工资Form
 * @author xuwei
 * @date 2014年8月5日
 */
public class PayoffBatchForm {
	/** 序号 */
	private int orderNo;
	
	/** 代发工资类型：0-发到永达互联网金融账户，1-发到银行卡 */
	private int payoffType;
	
	/** 向银行卡转账 */
	private TransferBankBatchForm bankTransfer;
	
	/** 永达互联网金融转账 */
	private TransferKjtBatchForm kjtTransfer;
	
	/** 服务费 */
	@Pattern(regexp = RegexRule.AMOUNT_2_DECINALS, message = "服务费格式不正确（小数点前最多19位，小数点后最多2位）")
	private String serviceCharge;
	
	/** 转账金额 */
	@Pattern(regexp = RegexRule.AMOUNT_2_DECINALS, message = "转账金额格式不正确（小数点前最多19位，小数点后最多2位）")
	private String transMoney;
	
	/** 总金额 */
	@Pattern(regexp = RegexRule.AMOUNT_2_DECINALS, message = "总金额格式不正确（小数点前最多19位，小数点后最多2位）")
	private String totalMoney;
	
	// 转账总数
	private int transferCount;

	// 转账成功总数
	private int transferSuccessCount;
	
	// 收取服务费笔数
	private int feeCount;
	
	// 转账成功金额
	private String transferSuccessAmount;
	
	// 转账失败总数
	private int transferFailCount;
	
	// 转账失败金额
	private String transferFailAmount;

	// 错误信息
	private String errorInfo;
		
	public int getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(int orderNo) {
		this.orderNo = orderNo;
	}

	public int getPayoffType() {
		return payoffType;
	}

	public void setPayoffType(int payoffType) {
		this.payoffType = payoffType;
	}

	public String getServiceCharge() {
		return serviceCharge;
	}

	public void setServiceCharge(String serviceCharge) {
		this.serviceCharge = serviceCharge;
	}

	public String getTransMoney() {
		return transMoney;
	}

	public void setTransMoney(String transMoney) {
		this.transMoney = transMoney;
	}

	public String getTotalMoney() {
		return totalMoney;
	}

	public void setTotalMoney(String totalMoney) {
		this.totalMoney = totalMoney;
	}

	public TransferBankBatchForm getBankTransfer() {
		return bankTransfer;
	}

	public void setBankTransfer(TransferBankBatchForm bankTransfer) {
		this.bankTransfer = bankTransfer;
	}

	public TransferKjtBatchForm getKjtTransfer() {
		return kjtTransfer;
	}

	public void setKjtTransfer(TransferKjtBatchForm kjtTransfer) {
		this.kjtTransfer = kjtTransfer;
	}

	public int getTransferCount() {
		return transferCount;
	}

	public void setTransferCount(int transferCount) {
		this.transferCount = transferCount;
	}

	public int getFeeCount() {
		return feeCount;
	}

	public void setFeeCount(int feeCount) {
		this.feeCount = feeCount;
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

	public String getErrorInfo() {
		return errorInfo;
	}

	public void setErrorInfo(String errorInfo) {
		this.errorInfo = errorInfo;
	}
	
}
