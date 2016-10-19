package com.netfinworks.site.domain.domain.info;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.common.util.money.Money;
import com.netfinworks.site.domain.enums.DealType;

/**
 * <p>企业钱包对账</p>
 * @author zhangyun.m
 * @version $Id: WalletCheckInfo.java, v 0.1 2013-12-4 上午10:09:39 HP Exp $
 */
public class WalletCheckInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**账户余额 ：日期  */
    private Date              txnTime;
    /**支付编码（支付类型）*/
    private String            payCode;
    /**账户余额 ：订单号  */
    private String            sysTraceNo;
    /**账户余额：摘要  */
    private String            summary;
    /**交易类型(2 支出 3 收入)  */
    private DealType          txnType;
    /**账户余额： 金额*/
    private Money             txnAmt;
    /**账户余额： 余额 */
    private Money             afterAmt;
	/** 产品编码 */
	private String productCode;


    public Date getTxnTime() {
        return txnTime;
    }

    public void setTxnTime(Date txnTime) {
        this.txnTime = txnTime;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public DealType getTxnType() {
        return txnType;
    }

    public void setTxnType(DealType txnType) {
        this.txnType = txnType;
    }

    public Money getTxnAmt() {
        return txnAmt;
    }

    public void setTxnAmt(Money txnAmt) {
        this.txnAmt = txnAmt;
    }

    public Money getAfterAmt() {
        return afterAmt;
    }

    public void setAfterAmt(Money afterAmt) {
        this.afterAmt = afterAmt;
    }
    
    public String getSysTraceNo() {
		return sysTraceNo;
	}

	public void setSysTraceNo(String sysTraceNo) {
		this.sysTraceNo = sysTraceNo;
	}
	
	public String getPayCode() {
		return payCode;
	}

	public void setPayCode(String payCode) {
		this.payCode = payCode;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	@Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
