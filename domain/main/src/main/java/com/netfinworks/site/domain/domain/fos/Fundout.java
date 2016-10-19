package com.netfinworks.site.domain.domain.fos;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.common.util.money.Money;

/**
 *
 * <p>出款</p>
 * @author qinde
 * @version $Id: FundoutInfo.java, v 0.1 2013-12-4 下午8:08:21 qinde Exp $
 */
public class Fundout implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -3552366042557435944L;
    /*     */private String  fundoutOrderNo;
    /*     */private String  paymentOrderNo;
    /*     */private String  memberId;
    /*     */private String  accountNo;
    /*     */private String  productCode;
    /*     */private Money   amount;
    /*     */private Money   fee;
    /*     */private String  cardNo;
    /*     */private String  cardType;
    /*     */private String  status;
    /*     */private String  name;
    /*     */private String  bankCode;
    /*     */private String  bankName;
    /*     */private String  branchName;
    /*     */private String  bankLineNo;
    /*     */private String  prov;
    /*     */private String  city;
    /*     */private String  companyOrPersonal;
    /*到卡备注*/private String  purpose;
    /*     */private String  fundoutGrade;
    /*     */private Date    orderTime;
    /*     */private Date    successTime;
    /*     */private String  extension;

	/*     */private String batchOrderNo;
	/*     */private String refundOrderNo;
	/*     */private Date refundTime;
	/*     */private String productName;
	         private String sourceBatchNo;
	         private String outOrderNo;

    public String getFundoutOrderNo() {
        return fundoutOrderNo;
    }

    public void setFundoutOrderNo(String fundoutOrderNo) {
        this.fundoutOrderNo = fundoutOrderNo;
    }

    public String getPaymentOrderNo() {
        return paymentOrderNo;
    }

    public void setPaymentOrderNo(String paymentOrderNo) {
        this.paymentOrderNo = paymentOrderNo;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public Money getAmount() {
        return amount;
    }

    public void setAmount(Money amount) {
        this.amount = amount;
    }

    public Money getFee() {
        return fee;
    }

    public void setFee(Money fee) {
        this.fee = fee;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBankLineNo() {
        return bankLineNo;
    }

    public void setBankLineNo(String bankLineNo) {
        this.bankLineNo = bankLineNo;
    }

    public String getProv() {
        return prov;
    }

    public void setProv(String prov) {
        this.prov = prov;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCompanyOrPersonal() {
        return companyOrPersonal;
    }

    public void setCompanyOrPersonal(String companyOrPersonal) {
        this.companyOrPersonal = companyOrPersonal;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getFundoutGrade() {
        return fundoutGrade;
    }

    public void setFundoutGrade(String fundoutGrade) {
        this.fundoutGrade = fundoutGrade;
    }

    public Date getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Date orderTime) {
        this.orderTime = orderTime;
    }

    public Date getSuccessTime() {
        return successTime;
    }

    public void setSuccessTime(Date successTime) {
        this.successTime = successTime;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

	public String getBatchOrderNo() {
		return batchOrderNo;
	}

	public void setBatchOrderNo(String batchOrderNo) {
		this.batchOrderNo = batchOrderNo;
	}

	public String getRefundOrderNo() {
		return refundOrderNo;
	}

	public void setRefundOrderNo(String refundOrderNo) {
		this.refundOrderNo = refundOrderNo;
	}

	public Date getRefundTime() {
		return refundTime;
	}

	public void setRefundTime(Date refundTime) {
		this.refundTime = refundTime;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
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

	@Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
