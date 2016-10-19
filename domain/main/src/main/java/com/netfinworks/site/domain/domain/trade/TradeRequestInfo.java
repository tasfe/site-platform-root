/**
 * 
 */
package com.netfinworks.site.domain.domain.trade;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.common.util.money.Money;
import com.netfinworks.site.domain.domain.info.PartyInfo;
import com.netfinworks.site.domain.enums.TradeType;

/**
 * <p>交易请求</p>
 * @author fjl
 * @version $Id: TradeRequestInfo.java, v 0.1 2013-11-28 下午1:21:50 fjl Exp $
 */
public class TradeRequestInfo extends TradeEnvironment {
    private static final long serialVersionUID = 1L;

    //付款方
    private PartyInfo         payer;

    //收款方
    private PartyInfo         payee;

    private Money             amount;

    private Money             free             = new Money();

    private TradeType         tradeType;
    //交易原始凭证号
    private String            tradeSourceVoucherNo;
    //交易凭证号
    private String            tradeVoucherNo;                //充值，提现调用统一凭证生成
    //支付凭证号
    private String            paymentVoucherNo;              //转账调用统一凭证生成
    //转账备注
    private String            memo;
    
    private boolean           sendMessage;                   //是否发送转账留言
    
    /**
     * 交易扩展信息
     */
    private String            tradeExtension;

    public PartyInfo getPayer() {
        return payer;
    }

    public void setPayer(PartyInfo payer) {
        this.payer = payer;
    }

    public PartyInfo getPayee() {
        return payee;
    }

    public void setPayee(PartyInfo payee) {
        this.payee = payee;
    }

    public Money getAmount() {
        return amount;
    }

    public void setAmount(Money amount) {
        this.amount = amount;
    }

    public Money getFree() {
        return free;
    }

    public void setFree(Money free) {
        this.free = free;
    }

    public TradeType getTradeType() {
        return tradeType;
    }

    public void setTradeType(TradeType tradeType) {
        this.tradeType = tradeType;
    }

    public String getTradeVoucherNo() {
        return tradeVoucherNo;
    }

    public void setTradeVoucherNo(String tradeVoucherNo) {
        this.tradeVoucherNo = tradeVoucherNo;
    }

    public String getPaymentVoucherNo() {
        return paymentVoucherNo;
    }

    public void setPaymentVoucherNo(String paymentVoucherNo) {
        this.paymentVoucherNo = paymentVoucherNo;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getTradeSourceVoucherNo() {
        return tradeSourceVoucherNo;
    }

    public void setTradeSourceVoucherNo(String tradeSourceVoucherNo) {
        this.tradeSourceVoucherNo = tradeSourceVoucherNo;
    }

    public String getTradeExtension() {
        return tradeExtension;
    }

    public void setTradeExtension(String tradeExtension) {
        this.tradeExtension = tradeExtension;
    }
    
    public boolean isSendMessage() {
        return sendMessage;
    }

    public void setSendMessage(boolean sendMessage) {
        this.sendMessage = sendMessage;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
