package com.netfinworks.site.domain.domain.trade;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 
 * <p>支付流水</p>
 * @author Guan Xiaoxu
 * @version $Id: PayWaterInfo.java, v 0.1 2013-12-10 下午6:16:20 Guanxiaoxu Exp $
 */
public class PayWaterInfo extends BillBaseInfo {

    /**
     * 
     */
    private static final long serialVersionUID = 8441306890125106263L;
    /**
     * 付款方账号
     */
    private String            payerAccount;
    /**
     * 收款方账号
     */
    private String            payeeAccount;
    /**
     * 交易提交时间
     */
    private Date              tradeSubmitTime;
    /**
     * 交易时间
     */
    private Date              tradeTime;
    /**
     * 支付状态
     */
    private String            paymentState;
    /**
     * 交易类型
     */
    private String            tradeType;
    /**
     * 订单编号
     * @return
     */
    private String            orderCode;

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getPayerAccount() {
        return payerAccount;
    }

    public void setPayerAccount(String payerAccount) {
        this.payerAccount = payerAccount;
    }

    public String getPayeeAccount() {
        return payeeAccount;
    }

    public void setPayeeAccount(String payeeAccount) {
        this.payeeAccount = payeeAccount;
    }

    public Date getTradeSubmitTime() {
        return tradeSubmitTime;
    }

    public void setTradeSubmitTime(Date tradeSubmitTime) {
        this.tradeSubmitTime = tradeSubmitTime;
    }

    public Date getTradeTime() {
        return tradeTime;
    }

    public void setTradeTime(Date tradeTime) {
        this.tradeTime = tradeTime;
    }

    public String getPaymentState() {
        return paymentState;
    }

    public void setPaymentState(String paymentState) {
        this.paymentState = paymentState;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
