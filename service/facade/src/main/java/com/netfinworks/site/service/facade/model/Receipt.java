package com.netfinworks.site.service.facade.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.common.util.money.Money;

/**
 * <p>电子对账单/p>
 * @author zhangyun.m
 * @version $Id: ElectronicDocument.java, v 0.1 2014年7月1日 下午5:22:01 zhangyun.m Exp $
 */
public class Receipt {

    /** 交易凭证号 */
    private String tradeVoucherNo;

    /** 交易金额 */
    private Money  tradeAmount;

    /** 费用 */
    private Money  feeAmount = new Money();

    /** 买家客户ID */
    private String buyerId;

    /** 买家名称 */
    private String buyerName;

    /** 买家账户 */
    private String buyerAccountNo;

    /** 买家开户行名称 */
    private String buyerBranchName;

    /** 卖家客户ID */
    private String sellerId;

    /** 卖家名称*/
    private String sellerName;

    /** 卖家账户 */
    private String sellerAccountNo;

    /** 卖家开户行名称 */
    private String sellerBranchName;

    /** 账户类型 */
    private String accountType;

    /** 交易类型 */
    private String tradeType;

    /** 入账账户  */
    private String enterAccount;

    /** 入账账户名称  */
    private String enterAccountName;

    /** 入账金额  */
    private String enterAmount;

    /** 订单时间  */
    private String gmtPaid;
    
    /** 交易备注 */
    private String tradeMemo;

    public String getTradeVoucherNo() {
        return tradeVoucherNo;
    }

    public void setTradeVoucherNo(String tradeVoucherNo) {
        this.tradeVoucherNo = tradeVoucherNo;
    }

    public Money getTradeAmount() {
        return tradeAmount;
    }

    public void setTradeAmount(Money tradeAmount) {
        this.tradeAmount = tradeAmount;
    }

    public Money getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(Money feeAmount) {
        this.feeAmount = feeAmount;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getBuyerAccountNo() {
        return buyerAccountNo;
    }

    public void setBuyerAccountNo(String buyerAccountNo) {
        this.buyerAccountNo = buyerAccountNo;
    }

    public String getBuyerBranchName() {
        return buyerBranchName;
    }

    public void setBuyerBranchName(String buyerBranchName) {
        this.buyerBranchName = buyerBranchName;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getSellerAccountNo() {
        return sellerAccountNo;
    }

    public void setSellerAccountNo(String sellerAccountNo) {
        this.sellerAccountNo = sellerAccountNo;
    }

    public String getSellerBranchName() {
        return sellerBranchName;
    }

    public void setSellerBranchName(String sellerBranchName) {
        this.sellerBranchName = sellerBranchName;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    public String getEnterAccount() {
        return enterAccount;
    }

    public void setEnterAccount(String enterAccount) {
        this.enterAccount = enterAccount;
    }

    public String getEnterAccountName() {
        return enterAccountName;
    }

    public void setEnterAccountName(String enterAccountName) {
        this.enterAccountName = enterAccountName;
    }

    public String getEnterAmount() {
        return enterAmount;
    }

    public void setEnterAmount(String enterAmount) {
        this.enterAmount = enterAmount;
    }

    public String getGmtPaid() {
        return gmtPaid;
    }

    public void setGmtPaid(String gmtPaid) {
        this.gmtPaid = gmtPaid;
    }

    public String getTradeMemo() {
        return tradeMemo;
    }

    public void setTradeMemo(String tradeMemo) {
        this.tradeMemo = tradeMemo;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
