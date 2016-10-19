package com.netfinworks.site.domain.domain.trade;

import com.netfinworks.common.util.money.Money;

public class TradeRefundRequset {
	//需要的退款金额
	private Money             refundAmount= new Money();;
	//生成退款交易凭证号
	private String            tradeVoucherNo;
	//生成退款交易原始凭证号
	private String            tradeSourceVoucherNo;
	//备注
	private String remarks;
	//终端类型
	private String accessChannel ;
	public String getAccessChannel() {
		return accessChannel;
	}
	public void setAccessChannel(String accessChannel) {
		this.accessChannel = accessChannel;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public Money getRefundAmount() {
		return refundAmount;
	}
	public void setRefundAmount(Money refundAmount) {
		this.refundAmount = refundAmount;
	}
	public String getTradeVoucherNo() {
		return tradeVoucherNo;
	}
	public void setTradeVoucherNo(String tradeVoucherNo) {
		this.tradeVoucherNo = tradeVoucherNo;
	}
	public String getTradeSourceVoucherNo() {
		return tradeSourceVoucherNo;
	}
	public void setTradeSourceVoucherNo(String tradeSourceVoucherNo) {
		this.tradeSourceVoucherNo = tradeSourceVoucherNo;
	}
}
