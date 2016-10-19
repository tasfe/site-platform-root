package com.netfinworks.site.domain.domain.trade;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.common.util.money.Money;

/**
 *
 * <p>出款查询</p>
 * @author qinde
 * @version $Id: FundoutQuery.java, v 0.1 2013-12-5 上午9:46:00 qinde Exp $
 */
public class FundoutQuery extends QueryBase implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 8271318005425488935L;
    private String            memberId;
    /**产品代码     */
    private String            productCode;
    /** 交易订单号 **/
	private String            fundoutOrderNo;
    /**     */
    private String            paymentOrderNo;
    /**账户号     */
    private String            accountNo;
    /**     */
    private Money             amountFrom;
    /**     */
    private Money             amountTo;
    /**卡号     */
    private String            cardNo;
    /**状态     */
    private String            status;
    /**银行代码     */
    private String            bankCode;
    /**公司或个人     */
    private String            companyOrPersonal;
    /**开始时间     */
    private Date              orderTimeStart;
    /**结束时间     */
    private Date              orderTimeEnd;
    
    /** 存在外部订单号 **/
	private boolean 		  hasOutOrderNo;
	/** 存在批次号 **/
	private boolean 		  hasBatchOrderNo;
	/** 不存在批次号 **/
	private boolean 		  hasNoBatchOrderNo;
	/** 批次号 **/
	private String batchOrderNo;
	/** 外部商户批次号 **/
	private String sourceBatchNo;
	/** 外部商户订单号 **/
	private String outOrderNo;

	/** 退票开始时间 **/
	private Date refundTimeStart;
	/** 退票结束时间 **/
	private Date refundTimeEnd;
	
	private Date resultTimeStart;
	private Date resultTimeEnd;

    public boolean isHasOutOrderNo() {
		return hasOutOrderNo;
	}

	public void setHasOutOrderNo(boolean hasOutOrderNo) {
		this.hasOutOrderNo = hasOutOrderNo;
	}

	public boolean isHasBatchOrderNo() {
		return hasBatchOrderNo;
	}

	public void setHasBatchOrderNo(boolean hasBatchOrderNo) {
		this.hasBatchOrderNo = hasBatchOrderNo;
	}

	public boolean isHasNoBatchOrderNo() {
		return hasNoBatchOrderNo;
	}

	public void setHasNoBatchOrderNo(boolean hasNoBatchOrderNo) {
		this.hasNoBatchOrderNo = hasNoBatchOrderNo;
	}

	public String getFundoutOrderNo() {
		return fundoutOrderNo;
	}

	public void setFundoutOrderNo(String fundoutOrderNo) {
		this.fundoutOrderNo = fundoutOrderNo;
	}

	public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getPaymentOrderNo() {
        return paymentOrderNo;
    }

    public void setPaymentOrderNo(String paymentOrderNo) {
        this.paymentOrderNo = paymentOrderNo;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public Money getAmountFrom() {
        return amountFrom;
    }

    public void setAmountFrom(Money amountFrom) {
        this.amountFrom = amountFrom;
    }

    public Money getAmountTo() {
        return amountTo;
    }

    public void setAmountTo(Money amountTo) {
        this.amountTo = amountTo;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getCompanyOrPersonal() {
        return companyOrPersonal;
    }

    public void setCompanyOrPersonal(String companyOrPersonal) {
        this.companyOrPersonal = companyOrPersonal;
    }

    public Date getOrderTimeStart() {
        return orderTimeStart;
    }

    public void setOrderTimeStart(Date orderTimeStart) {
        this.orderTimeStart = orderTimeStart;
    }

    public Date getOrderTimeEnd() {
        return orderTimeEnd;
    }

    public void setOrderTimeEnd(Date orderTimeEnd) {
        this.orderTimeEnd = orderTimeEnd;
    }

	public String getBatchOrderNo() {
		return batchOrderNo;
	}

	public void setBatchOrderNo(String batchOrderNo) {
		this.batchOrderNo = batchOrderNo;
	}

	public Date getRefundTimeStart() {
		return refundTimeStart;
	}

	public void setRefundTimeStart(Date refundTimeStart) {
		this.refundTimeStart = refundTimeStart;
	}

	public Date getRefundTimeEnd() {
		return refundTimeEnd;
	}

	public void setRefundTimeEnd(Date refundTimeEnd) {
		this.refundTimeEnd = refundTimeEnd;
	}
	
	

	public String getSourceBatchNo() {
		return sourceBatchNo;
	}

	public void setSourceBatchNo(String sourceBatchNo) {
		this.sourceBatchNo = sourceBatchNo;
	}
	
	

	public String getOutOrderNo() {
		return outOrderNo;
	}

	public void setOutOrderNo(String outOrderNo) {
		this.outOrderNo = outOrderNo;
	}
	
	

	public Date getResultTimeStart() {
		return resultTimeStart;
	}

	public void setResultTimeStart(Date resultTimeStart) {
		this.resultTimeStart = resultTimeStart;
	}

	public Date getResultTimeEnd() {
		return resultTimeEnd;
	}

	public void setResultTimeEnd(Date resultTimeEnd) {
		this.resultTimeEnd = resultTimeEnd;
	}

	@Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
