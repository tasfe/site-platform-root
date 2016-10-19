package com.netfinworks.site.domain.domain.trade;

import java.io.Serializable;
import java.util.Date;

import com.netfinworks.common.util.money.Money;
/**
 * 
 * <p>转账INFO</p>
 * @author Guan Xiaoxu
 * @version $Id: TransferInfo.java, v 0.1 2013-12-16 下午6:22:07 Guanxiaoxu Exp $
 */
public class TransferInfo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    /** 交易原始凭证号--即商户订单号 */
    private String               tradeSourceVoucherNo;

	/**
     * 订单号
     */
    private String            orderId;
    /**
     * 转账时间
     */
    private Date              transferTime;
    /**
     * 状态
     */
    private String            state;
    /**
     * 转账金额
     */
    private Money             transferAmount;
    /**
     * 买方Id
     */
    private String            buyId;
    private String            buyerName;
    /**
     * 卖方Id
     */
    private String            sellId;
    private String            sellerName;
    /**
     * 
     * 身份类型 1:卖方   2：买方
     */
    private String            plamId;
    
    private String            memberId;

    /**
     * zhaozhenqiang 增
     * */
    /**
     * 交易类型
     * */
    private String tradeType;
    
    /** 交易备注*/
    private String tradeMemo;
    
    /** 支付时间*/
    private Date gmtPaid;

    /** 卖家手续费 */
    private Money                payeeFee       = new Money();
    
    /** 买家手续费 */
    private Money                payerFee       = new Money();
    
	/** 批次号 */
	private String batchNo;
	
	/** 产品编码 */
    private String               bizProductCode;
    
    /** 扩展信息 */
	private String extention;
	
	/**外部批次号里 */
	private String sourceBatchNo;

    public Money getPayeeFee() {
		return payeeFee;
	}

	public void setPayeeFee(Money payeeFee) {
		this.payeeFee = payeeFee;
	}

	public Date getGmtPaid() {
		return gmtPaid;
	}

	public void setGmtPaid(Date gmtPaid) {
		this.gmtPaid = gmtPaid;
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

	public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Date getTransferTime() {
        return transferTime;
    }

    public void setTransferTime(Date transferTime) {
        this.transferTime = transferTime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Money getTransferAmount() {
        return transferAmount;
    }

    public void setTransferAmount(Money transferAmount) {
        this.transferAmount = transferAmount;
    }

    public String getBuyId() {
        return buyId;
    }

    public void setBuyId(String buyId) {
        this.buyId = buyId;
    }

    public String getSellId() {
        return sellId;
    }

    public void setSellId(String sellId) {
        this.sellId = sellId;
    }

    public String getPlamId() {
        return plamId;
    }

    public void setPlamId(String plamId) {
        this.plamId = plamId;
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

    /**
     * @return the memberId
     */
    public String getMemberId() {
        return memberId;
    }

    /**
     * @param memberId the memberId to set
     */
    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

	public Money getPayerFee() {
		return payerFee;
	}

	public void setPayerFee(Money payerFee) {
		this.payerFee = payerFee;
	}

	public String getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}
	
	public String getTradeSourceVoucherNo() {
		return tradeSourceVoucherNo;
	}

	public void setTradeSourceVoucherNo(String tradeSourceVoucherNo) {
		this.tradeSourceVoucherNo = tradeSourceVoucherNo;
	}

    public String getBizProductCode() {
        return bizProductCode;
    }

    public void setBizProductCode(String bizProductCode) {
        this.bizProductCode = bizProductCode;
    }

	public String getExtention() {
		return extention;
	}

	public void setExtention(String extention) {
		this.extention = extention;
	}

	public String getSourceBatchNo() {
		return sourceBatchNo;
	}

	public void setSourceBatchNo(String sourceBatchNo) {
		this.sourceBatchNo = sourceBatchNo;
	}
	
	
}
