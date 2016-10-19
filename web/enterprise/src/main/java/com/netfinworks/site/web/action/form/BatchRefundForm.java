package com.netfinworks.site.web.action.form;

import com.netfinworks.common.util.money.Money;

public class BatchRefundForm {
	/** 原交易凭证号 */
	private String tradeVoucherNo;
	/** 原商户订单号 */
	private String serialNumber;
	/** 交易金额 */
	private Money tradeMoney;
	/** 交易服务费*/
	private Money tradeFee;
	/** 退款金额 */
	private Money refundMoney;
	/** 退还服务费*/
	private Money refundFee;
	/** 备注*/
	private String memo;
	/** 是否可退款*/
	private String success;
	/** 不可退款备注说明*/
	private String remarks;
	/**订单号*/
	private String bizNo;
	/**退款商户订单号*/
	private String refundNumber;
	
	public String getTradeVoucherNo() {
		return tradeVoucherNo;
	}
	public void setTradeVoucherNo(String tradeVoucherNo) {
		this.tradeVoucherNo = tradeVoucherNo;
	}
	public String getSerialNumber() {
		return serialNumber;
	}
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	public Money getTradeMoney() {
		return tradeMoney;
	}
	public void setTradeMoney(Money tradeMoney) {
		this.tradeMoney = tradeMoney;
	}
	public Money getTradeFee() {
		return tradeFee;
	}
	public void setTradeFee(Money tradeFee) {
		this.tradeFee = tradeFee;
	}
	public Money getRefundMoney() {
		return refundMoney;
	}
	public void setRefundMoney(Money refundMoney) {
		this.refundMoney = refundMoney;
	}
	public Money getRefundFee() {
		return refundFee;
	}
	public void setRefundFee(Money refundFee) {
		this.refundFee = refundFee;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	public String getSuccess() {
		return success;
	}
	public void setSuccess(String success) {
		this.success = success;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public String getBizNo() {
		return bizNo;
	}
	public void setBizNo(String bizNo) {
		this.bizNo = bizNo;
	}
	public String getRefundNumber() {
		return refundNumber;
	}
	public void setRefundNumber(String refundNumber) {
		this.refundNumber = refundNumber;
	}	
}
