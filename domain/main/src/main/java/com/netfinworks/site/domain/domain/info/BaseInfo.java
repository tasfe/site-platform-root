package com.netfinworks.site.domain.domain.info;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.common.util.money.Money;

public class BaseInfo implements Serializable {
	private static final long serialVersionUID = -314713962588242444L;
	/** 流水号 */
	private String serialNumber;
	/** 交易凭证号 */
	private String tradeVoucherNo;
	/** 定单号 */
	private String bizNo;
	/** 产品编码 */
	private String orderNumber;
	/** 最后修改时间 */
	private Date gmtModified;
	/** 交易类型 */
	private String tradeType;
	/** 订单金额(商户订单款) */
	private Money orderMoney;
	/** 结算金额 */
	private Money settledAmount;
	/** 金币金额 */
	private Money coinAmount;
	/** 订单类型 */
	private String orderType;
	/** 订单状态（数字表示） */
	private String orderState;
	/** 实收金额(付款金额) */
	private Money payAmount;
	/** 交易备注 */
	private String tradeMemo;
	/** 分账信息集 */
	private List<CoSplitParameter> splitParameter = new ArrayList<CoSplitParameter>();
	/** 买家名称 */
	private String buyerName;
	/** 买家ID */
	private String buyerId;
	/** 卖家名称 */
	private String sellerName;
	/** 卖家ID */
	private String sellerId;
	/** 会员ID */
	private String memberId;
	/** 交易提交时间 */
	private Date gmtSubmit;
	/** 分账： 佣金金额 */
	private Money splitRebateAmount;
	/** 分账： 金币金额 */
	private Money splitCoinAmount;
	/** 交易状态 （汉字表示） */
	private String tradeState;
	/** 支付时间zhaozhenqiang */
	private Date gmtpaid;
	 /** 卖家手续费 */
    private Money                payeeFee       = new Money();
    /** 退即时结算金额 */
    private Money refundInstSettledAmount;
    
    private String origTradeSourceVoucherNo;
    
	/** 产品编码 */
	private String bizProductCode;

	/** 支付渠道 */
	private String payChannel;

	/** 退款金额*/
	private Money refundAmount;
	/** 商户批次号 */
	private String sourceBatchNo;
	/***/
	private String extention;
	
	/** 批次号 */
    private String               batchNo;
	
	public String getPayChannel() {
		return payChannel;
	}

	public void setPayChannel(String payChannel) {
		this.payChannel = payChannel;
	}

	public String getBizProductCode() {
		return bizProductCode;
	}

	public void setBizProductCode(String bizProductCode) {
		this.bizProductCode = bizProductCode;
	}

	public String getOrigTradeSourceVoucherNo() {
		return origTradeSourceVoucherNo;
	}

	public void setOrigTradeSourceVoucherNo(String origTradeSourceVoucherNo) {
		this.origTradeSourceVoucherNo = origTradeSourceVoucherNo;
	}

	public Money getRefundInstSettledAmount() {
		return refundInstSettledAmount;
	}

	public void setRefundInstSettledAmount(Money refundInstSettledAmount) {
		this.refundInstSettledAmount = refundInstSettledAmount;
	}

	public Money getPayeeFee() {
		return payeeFee;
	}

	public void setPayeeFee(Money payeeFee) {
		this.payeeFee = payeeFee;
	}

	public Date getGmtpaid() {
		return gmtpaid;
	}

	public void setGmtpaid(Date gmtpaid) {
		this.gmtpaid = gmtpaid;
	}

	public List<CoSplitParameter> getSplitParameter() {
		return splitParameter;
	}

	public void setSplitParameter(List<CoSplitParameter> splitParameter) {
		this.splitParameter = splitParameter;
	}

	public BaseInfo() {
		super();
	}

	public BaseInfo(String serialNumber, String orderNumber, Date gmtModified,
			String tradeType, Money orderMoney, String orderType,
			String orderState, Money payAmount, String tradeMemo, String bizNo,
			String buyerName, String sellerName, String buyerId,
			String sellerId, String memberId, Date gmtSubmit, Money coinAmount,
			Money splitRebateAmount, Money splitCoinAmount,
			Money settledAmount, String tradeState,String tradeVoucherNo,Money refundAmount) {
		super();
		this.serialNumber = serialNumber;
		this.orderNumber = orderNumber;
		this.gmtModified = gmtModified;
		this.tradeType = tradeType;
		this.orderMoney = orderMoney;
		this.orderType = orderType;
		this.orderState = orderState;
		this.payAmount = payAmount;
		this.tradeMemo = tradeMemo;
		this.bizNo = bizNo;
		this.buyerName = buyerName;
		this.sellerName = sellerName;
		this.buyerId = buyerId;
		this.sellerId = sellerId;
		this.memberId = memberId;
		this.gmtSubmit = gmtSubmit;
		this.coinAmount = coinAmount;
		this.splitCoinAmount = splitCoinAmount;
		this.splitRebateAmount = splitRebateAmount;
		this.settledAmount = settledAmount;
		this.tradeType = tradeType;
		this.tradeVoucherNo = tradeVoucherNo;
		this.refundAmount=refundAmount;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}

	public Date getGmtModified() {
		return gmtModified;
	}

	public void setGmtModified(Date gmtModified) {
		this.gmtModified = gmtModified;
	}

	public Money getOrderMoney() {
		return orderMoney;
	}

	public void setOrderMoney(Money orderMoney) {
		this.orderMoney = orderMoney;
	}

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public String getOrderState() {
		return orderState;
	}

	public void setOrderState(String orderState) {
		this.orderState = orderState;
	}

	public Money getPayAmount() {
		return payAmount;
	}

	public void setPayAmount(Money payAmount) {
		this.payAmount = payAmount;
	}

	public String getTradeType() {
		return tradeType;
	}

	public void setTradeType(String tradeType) {
		this.tradeType = tradeType;
	}

	public String getTradeMemo() {
		return tradeMemo;
	}

	public void setTradeMemo(String tradeMemo) {
		this.tradeMemo = tradeMemo;
	}

	public String getBizNo() {
		return bizNo;
	}

	public void setBizNo(String bizNo) {
		this.bizNo = bizNo;
	}

	public String getBuyerName() {
		return buyerName;
	}

	public void setBuyerName(String buyerName) {
		this.buyerName = buyerName;
	}

	public String getSellerName() {
		return sellerName;
	}

	public void setSellerName(String sellerName) {
		this.sellerName = sellerName;
	}

	public String getBuyerId() {
		return buyerId;
	}

	public void setBuyerId(String buyerId) {
		this.buyerId = buyerId;
	}

	public String getSellerId() {
		return sellerId;
	}

	public void setSellerId(String sellerId) {
		this.sellerId = sellerId;
	}

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public Date getGmtSubmit() {
		return gmtSubmit;
	}

	public void setGmtSubmit(Date gmtSubmit) {
		this.gmtSubmit = gmtSubmit;
	}

	public Money getCoinAmount() {
		return coinAmount;
	}

	public void setCoinAmount(Money coinAmount) {
		this.coinAmount = coinAmount;
	}

	public Money getSplitRebateAmount() {
		return splitRebateAmount;
	}

	public void setSplitRebateAmount(Money splitRebateAmount) {
		this.splitRebateAmount = splitRebateAmount;
	}

	public Money getSplitCoinAmount() {
		return splitCoinAmount;
	}

	public void setSplitCoinAmount(Money splitCoinAmount) {
		this.splitCoinAmount = splitCoinAmount;
	}

	public Money getSettledAmount() {
		return settledAmount;
	}

	public void setSettledAmount(Money settledAmount) {
		this.settledAmount = settledAmount;
	}

	public String getTradeState() {
		return tradeState;
	}

	public void setTradeState(String tradeState) {
		this.tradeState = tradeState;
	}	

	public String getTradeVoucherNo() {
        return tradeVoucherNo;
    }

    public void setTradeVoucherNo(String tradeVoucherNo) {
        this.tradeVoucherNo = tradeVoucherNo;
    }

	public Money getRefundAmount() {
		return refundAmount;
	}

	public void setRefundAmount(Money refundAmount) {
		this.refundAmount = refundAmount;
	}
	

	public String getSourceBatchNo() {
		return sourceBatchNo;
	}

	public void setSourceBatchNo(String sourceBatchNo) {
		this.sourceBatchNo = sourceBatchNo;
	}

	public String getExtention() {
		return extention;
	}

	public void setExtention(String extention) {
		this.extention = extention;
	}

	public String getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
