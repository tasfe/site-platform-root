package com.netfinworks.site.domain.domain.trade;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.common.util.money.Money;

/**
 *
 * <p>订单下载显示基类</p>
 * @author Guan Xiaoxu
 * @version $Id: BillBaseInfo.java, v 0.1 2013-12-10 下午5:54:55 Guanxiaoxu Exp $
 */
public class BillBaseInfo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -4401723888762659118L;
    /**
     * 钱包交易订单号
     */
    private String            walletTradeId;
    /**
     * 商户订单id
     */
    private String            merchantOrderId;
    /**
     * 商品名称
     */
    private String            commodityName;
    /**
     * 订金金额
     */
    private Money            depositAmount;
    /**
     * 付款金币
     */
    private Money            paymentGold;
    /**
     * 付款金额
     */
    private Money            paymentAmount;
    /** 结算金额(实际成交金额) */
    private Money            settledAmount;

    public String getWalletTradeId() {
        return walletTradeId;
    }

    public void setWalletTradeId(String walletTradeId) {
        this.walletTradeId = walletTradeId;
    }

    public String getMerchantOrderId() {
        return merchantOrderId;
    }

    public void setMerchantOrderId(String merchantOrderId) {
        this.merchantOrderId = merchantOrderId;
    }

    public String getCommodityName() {
        return commodityName;
    }

    public void setCommodityName(String commodityName) {
        this.commodityName = commodityName;
    }


    public Money getPaymentGold() {
        return paymentGold;
    }

    public void setPaymentGold(Money paymentGold) {
        this.paymentGold = paymentGold;
    }

    public Money getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(Money depositAmount) {
        this.depositAmount = depositAmount;
    }

    public Money getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(Money paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public Money getSettledAmount() {
        return settledAmount;
    }

    public void setSettledAmount(Money settledAmount) {
        this.settledAmount = settledAmount;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
