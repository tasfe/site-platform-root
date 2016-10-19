package com.netfinworks.site.domain.domain.trade;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.common.util.money.Money;

/**
 * 
 * <p>订单结算</p>
 * @author Guan Xiaoxu
 * @version $Id: OrderSettleInfo.java, v 0.1 2013-12-10 下午6:15:52 Guanxiaoxu Exp $
 */
public class OrderSettleInfo extends BillBaseInfo {

    /**
     * 
     */
    private static final long serialVersionUID = -2629569173530105894L;
    /**
     * 佣金款
     */
    private Money            commission;
    /**
     * 金币款
     */
    private Money            goldAmount;
    /**
     * 交易结束时间
     */
    private Date            tradeOverTime;



    public Money getCommission() {
        return commission;
    }

    public void setCommission(Money commission) {
        this.commission = commission;
    }

    public Money getGoldAmount() {
        return goldAmount;
    }

    public void setGoldAmount(Money goldAmount) {
        this.goldAmount = goldAmount;
    }

    public Date getTradeOverTime() {
        return tradeOverTime;
    }

    public void setTradeOverTime(Date tradeOverTime) {
        this.tradeOverTime = tradeOverTime;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
