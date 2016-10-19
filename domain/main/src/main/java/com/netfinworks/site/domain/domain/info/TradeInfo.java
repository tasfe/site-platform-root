package com.netfinworks.site.domain.domain.info;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.common.util.money.Money;
import com.netfinworks.site.domain.enums.AcqTradeType;

/**
 *
 * <p>交易信息</p>
 * @author qinde
 * @version $Id: TradeInfo.java, v 0.1 2013-12-2 下午6:05:07 qinde Exp $
 */
public class TradeInfo implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -727646986659342059L;
    /**交易原始凭证*/             
    private String            tradeSourceVoucherNo;
    /**交易凭证*/
    private String            tradeVoucherNo;
    /**产品代码*/
    private String            bizProductCode;
    /**买方*/
    private String            buyerId;
    /**买方名称     */
    private String            buyerName;
    /**卖方*/
    private String            sellerId;
    /**卖方名称    */
    private String            sellerName;
    private String            bizNo;
    /**状态*/
    private String            status;
    /**交易类型*/
    private AcqTradeType      tradeType;
    /**交易金额*/
    private Money             tradeAmount;
    /** 付款金额 */
    private Money             payAmount;
    /** 定金金额 */
    private Money             prepaidAmount;
    /** 金币金额 */
    private Money             coinAmount;
    /**交易信息*/
    private String            tradeMemo;
    /**结束时间*/
    private Date              gmtEnd;
    /**提交时间*/
    private Date              gmtSubmit;
    /**修改时间*/
    private Date              gmtModified;
    /**退担保金额    */
    private Money             refundEnsureAmount;
    /**退预付金额    */
    private Money             refundPrepayAmount;
    /**退金币金额    */
    private Money             refundCoinAmount;
    /**退已结算金额    */
    private Money             refundSettledAmount;
    /**退支付金额    */
    private Money             refundPaidAmount;
    /** 退买家手续 */
    private Money                refundPayerFee = new Money();
    /** 支付账户 */
    private String               payerAccountNo;
    /** 支付时间 */
    private Date                 gmtPaid;
	/** 批次号 */
	private String batchNo;
	/** 原交易原始凭证号 */
	private String origTradeSourceVoucherNo;
	/** 外部批次号 */
	private String sourceBatchNo;
	
	/**  */
	private String origTradeVoucherNo;
    public Date getGmtPaid() {
		return gmtPaid;
	}

	public void setGmtPaid(Date gmtPaid) {
		this.gmtPaid = gmtPaid;
	}

	public String getPayerAccountNo() {
		return payerAccountNo;
	}

	public void setPayerAccountNo(String payerAccountNo) {
		this.payerAccountNo = payerAccountNo;
	}

	public Money getRefundPayerFee() {
		return refundPayerFee;
	}

	public void setRefundPayerFee(Money refundPayerFee) {
		this.refundPayerFee = refundPayerFee;
	}
    /** 卖家手续费 */
    private Money                payeeFee       = new Money();
    
    /** 买家手续费 */
    private Money                payerFee       = new Money();

    public Money getPayeeFee() {
		return payeeFee;
	}

	public void setPayeeFee(Money payeeFee) {
		this.payeeFee = payeeFee;
	}

	public Money getPayerFee() {
		return payerFee;
	}

	public void setPayerFee(Money payerFee) {
		this.payerFee = payerFee;
	}

	public String getTradeSourceVoucherNo() {
        return tradeSourceVoucherNo;
    }

    public void setTradeSourceVoucherNo(String tradeSourceVoucherNo) {
        this.tradeSourceVoucherNo = tradeSourceVoucherNo;
    }

    public String getTradeVoucherNo() {
        return tradeVoucherNo;
    }

    public void setTradeVoucherNo(String tradeVoucherNo) {
        this.tradeVoucherNo = tradeVoucherNo;
    }

    public String getBizProductCode() {
        return bizProductCode;
    }

    public void setBizProductCode(String bizProductCode) {
        this.bizProductCode = bizProductCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public AcqTradeType getTradeType() {
        return tradeType;
    }

    public void setTradeType(AcqTradeType tradeType) {
        this.tradeType = tradeType;
    }

    public Money getTradeAmount() {
        return tradeAmount;
    }

    public void setTradeAmount(Money tradeAmount) {
        this.tradeAmount = tradeAmount;
    }

    public String getTradeMemo() {
        return tradeMemo;
    }

    public void setTradeMemo(String tradeMemo) {
        this.tradeMemo = tradeMemo;
    }

    public Date getGmtEnd() {
        return gmtEnd;
    }

    public void setGmtEnd(Date gmtEnd) {
        this.gmtEnd = gmtEnd;
    }

    public Date getGmtSubmit() {
        return gmtSubmit;
    }

    public void setGmtSubmit(Date gmtSubmit) {
        this.gmtSubmit = gmtSubmit;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    public Money getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(Money payAmount) {
        this.payAmount = payAmount;
    }

    public String getBizNo() {
        return bizNo;
    }

    public void setBizNo(String bizNo) {
        this.bizNo = bizNo;
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

    public Money getCoinAmount() {
        return coinAmount;
    }

    public void setCoinAmount(Money coinAmount) {
        this.coinAmount = coinAmount;
    }

    public Money getPrepaidAmount() {
        return prepaidAmount;
    }

    public void setPrepaidAmount(Money prepaidAmount) {
        this.prepaidAmount = prepaidAmount;
    }

    public Money getRefundEnsureAmount() {
        return refundEnsureAmount;
    }

    public void setRefundEnsureAmount(Money refundEnsureAmount) {
        this.refundEnsureAmount = refundEnsureAmount;
    }

    public Money getRefundPrepayAmount() {
        return refundPrepayAmount;
    }

    public void setRefundPrepayAmount(Money refundPrepayAmount) {
        this.refundPrepayAmount = refundPrepayAmount;
    }

    public Money getRefundCoinAmount() {
        return refundCoinAmount;
    }

    public void setRefundCoinAmount(Money refundCoinAmount) {
        this.refundCoinAmount = refundCoinAmount;
    }

    public Money getRefundSettledAmount() {
        return refundSettledAmount;
    }

    public void setRefundSettledAmount(Money refundSettledAmount) {
        this.refundSettledAmount = refundSettledAmount;
    }

    public Money getRefundPaidAmount() {
        return refundPaidAmount;
    }

    public void setRefundPaidAmount(Money refundPaidAmount) {
        this.refundPaidAmount = refundPaidAmount;
    }

	public String getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}

	public String getOrigTradeSourceVoucherNo() {
		return origTradeSourceVoucherNo;
	}

	public void setOrigTradeSourceVoucherNo(String origTradeSourceVoucherNo) {
		this.origTradeSourceVoucherNo = origTradeSourceVoucherNo;
	}

	public String getSourceBatchNo() {
		return sourceBatchNo;
	}

	public void setSourceBatchNo(String sourceBatchNo) {
		this.sourceBatchNo = sourceBatchNo;
	}

	public String getOrigTradeVoucherNo() {
		return origTradeVoucherNo;
	}

	public void setOrigTradeVoucherNo(String origTradeVoucherNo) {
		this.origTradeVoucherNo = origTradeVoucherNo;
	}

	@Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
