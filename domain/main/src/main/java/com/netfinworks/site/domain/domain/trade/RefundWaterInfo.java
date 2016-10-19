package com.netfinworks.site.domain.domain.trade;

import java.util.Date;
import com.netfinworks.common.util.money.Money;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 
 * <p>退款流水</p>
 * @author Guan Xiaoxu
 * @version $Id: RefundWaterInfo.java, v 0.1 2013-12-10 下午6:18:07 Guanxiaoxu Exp $
 */
public class RefundWaterInfo extends BillBaseInfo {

    private static final long serialVersionUID = 2627561157605546477L;
    /**
     * 商户退单ID
     */
    private String            merchantRefundOrderId;
    /**
     * 退款金额
     */
    private Money             refundAmount;
    /**
     * 退款金额
     */
    private String            refundAmountNum;
    /**
     * 退担保金额
     */
    private Money             refundGuarantAmount;
    /**
     * 退金币金额
     */
    private Money             refundGoldAmount;
    /**
     * 退订金金额
     */
    private Money             refundDepositAmount;
    /**
     * 退款提交时间
     */
    private Date              refundSubmitTime;
    /**
     * 退款时间
     */
    private Date              refundTime;
    /**
     * 退款状态
     */
    private String            refundState;

    public String getMerchantRefundOrderId() {
        return merchantRefundOrderId;
    }

    public void setMerchantRefundOrderId(String merchantRefundOrderId) {
        this.merchantRefundOrderId = merchantRefundOrderId;
    }

    public Money getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(Money refundAmount) {
        this.refundAmount = refundAmount;
    }

    public Money getRefundGuarantAmount() {
        return refundGuarantAmount;
    }

    public void setRefundGuarantAmount(Money refundGuarantAmount) {
        this.refundGuarantAmount = refundGuarantAmount;
    }

    public Money getRefundGoldAmount() {
        return refundGoldAmount;
    }

    public void setRefundGoldAmount(Money refundGoldAmount) {
        this.refundGoldAmount = refundGoldAmount;
    }

    public Money getRefundDepositAmount() {
        return refundDepositAmount;
    }

    public void setRefundDepositAmount(Money refundDepositAmount) {
        this.refundDepositAmount = refundDepositAmount;
    }

    public Date getRefundSubmitTime() {
        return refundSubmitTime;
    }

    public void setRefundSubmitTime(Date refundSubmitTime) {
        this.refundSubmitTime = refundSubmitTime;
    }

    public Date getRefundTime() {
        return refundTime;
    }

    public void setRefundTime(Date refundTime) {
        this.refundTime = refundTime;
    }

    public String getRefundState() {
        return refundState;
    }

    public void setRefundState(String refundState) {
        this.refundState = refundState;
    }

    public String getRefundAmountNum() {
        return refundAmountNum;
    }

    public void setRefundAmountNum(String refundAmountNum) {
        this.refundAmountNum = refundAmountNum;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
