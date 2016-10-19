/**
 * 
 */
package com.netfinworks.site.domain.domain.trade;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.common.util.money.Money;
import com.netfinworks.site.domain.domain.info.PartyInfo;
import com.netfinworks.site.domain.enums.TradeType;

/**
 * <p>批量交易请求信息</p>
 * @author fjl
 * @version $Id: BatchTradeRequestInfo.java, v 0.1 2013-12-4 下午3:16:21 fjl Exp $
 */
public class BatchTradeRequestInfo extends TradeEnvironment {
    private static final long serialVersionUID = 1L;

    //付款方
    private PartyInfo         payer;
    //收款方
    private List<PartyInfo>   payee;

    private Money             amount;

    private Money             free             = new Money();
    //交易类型
    private TradeType         tradeType;
    //交易原始凭证号
    private List<String>      tradeSourceVoucherNo;
    //交易凭证号
    private List<String>      tradeVoucherNo;
    //备注
    private String            memo;
    //交易扩展信息
    private String            tradeExtension;

    public PartyInfo getPayer() {
        return payer;
    }

    public void setPayer(PartyInfo payer) {
        this.payer = payer;
    }

    public List<PartyInfo> getPayee() {
        return payee;
    }

    public void setPayee(List<PartyInfo> payee) {
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

    public List<String> getTradeSourceVoucherNo() {
        return tradeSourceVoucherNo;
    }

    public void setTradeSourceVoucherNo(List<String> tradeSourceVoucherNo) {
        this.tradeSourceVoucherNo = tradeSourceVoucherNo;
    }

    public List<String> getTradeVoucherNo() {
        return tradeVoucherNo;
    }

    public void setTradeVoucherNo(List<String> tradeVoucherNo) {
        this.tradeVoucherNo = tradeVoucherNo;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getTradeExtension() {
        return tradeExtension;
    }

    public void setTradeExtension(String tradeExtension) {
        this.tradeExtension = tradeExtension;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
