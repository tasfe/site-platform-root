package com.netfinworks.site.core.dal.dataobject;

import java.util.Date;

import com.netfinworks.common.util.money.Money;

public class PosTradeDO {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_POS_TRADE.TRADE_NO
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    private String tradeNo;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_POS_TRADE.MERCHANT_NO
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    private String merchantNo;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_POS_TRADE.MERCHANT_NAME
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    private String merchantName;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_POS_TRADE.CLIENT_NO
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    private String clientNo;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_POS_TRADE.TRADE_S_TIME
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    private String tradeSTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_POS_TRADE.TRADE_TIME
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    private Date tradeTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_POS_TRADE.TRADE_TYPE
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    private String tradeType;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_POS_TRADE.BANK_NO
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    private String bankNo;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_POS_TRADE.CARD_TYPE
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    private String cardType;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_POS_TRADE.BANK
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    private String bank;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_POS_TRADE.TRADE_AMOUNT
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    private Money tradeAmount;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_POS_TRADE.TRADE_FEE
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    private Money tradeFee;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_POS_TRADE.TRADE_SUM
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    private Money tradeSum;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_POS_TRADE.STATUS
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    private String status;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_POS_TRADE.TRADE_SRC_NO
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    private String tradeSrcNo;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_POS_TRADE.REASON
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    private String reason;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_POS_TRADE.REMARK
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    private String remark;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_POS_TRADE.GMT_CREATE
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    private Date gmtCreate;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_POS_TRADE.GMT_UPDATE
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    private Date gmtUpdate;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_POS_TRADE.OPERATOR
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    private String operator;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_POS_TRADE.TRADE_NO
     *
     * @return the value of T_POS_TRADE.TRADE_NO
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public String getTradeNo() {
        return tradeNo;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_POS_TRADE.TRADE_NO
     *
     * @param tradeNo the value for T_POS_TRADE.TRADE_NO
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo == null ? null : tradeNo.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_POS_TRADE.MERCHANT_NO
     *
     * @return the value of T_POS_TRADE.MERCHANT_NO
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public String getMerchantNo() {
        return merchantNo;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_POS_TRADE.MERCHANT_NO
     *
     * @param merchantNo the value for T_POS_TRADE.MERCHANT_NO
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public void setMerchantNo(String merchantNo) {
        this.merchantNo = merchantNo == null ? null : merchantNo.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_POS_TRADE.MERCHANT_NAME
     *
     * @return the value of T_POS_TRADE.MERCHANT_NAME
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public String getMerchantName() {
        return merchantName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_POS_TRADE.MERCHANT_NAME
     *
     * @param merchantName the value for T_POS_TRADE.MERCHANT_NAME
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName == null ? null : merchantName.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_POS_TRADE.CLIENT_NO
     *
     * @return the value of T_POS_TRADE.CLIENT_NO
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public String getClientNo() {
        return clientNo;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_POS_TRADE.CLIENT_NO
     *
     * @param clientNo the value for T_POS_TRADE.CLIENT_NO
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public void setClientNo(String clientNo) {
        this.clientNo = clientNo == null ? null : clientNo.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_POS_TRADE.TRADE_S_TIME
     *
     * @return the value of T_POS_TRADE.TRADE_S_TIME
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public String getTradeSTime() {
        return tradeSTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_POS_TRADE.TRADE_S_TIME
     *
     * @param tradeSTime the value for T_POS_TRADE.TRADE_S_TIME
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public void setTradeSTime(String tradeSTime) {
        this.tradeSTime = tradeSTime == null ? null : tradeSTime.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_POS_TRADE.TRADE_TIME
     *
     * @return the value of T_POS_TRADE.TRADE_TIME
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public Date getTradeTime() {
        return tradeTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_POS_TRADE.TRADE_TIME
     *
     * @param tradeTime the value for T_POS_TRADE.TRADE_TIME
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public void setTradeTime(Date tradeTime) {
        this.tradeTime = tradeTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_POS_TRADE.TRADE_TYPE
     *
     * @return the value of T_POS_TRADE.TRADE_TYPE
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public String getTradeType() {
        return tradeType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_POS_TRADE.TRADE_TYPE
     *
     * @param tradeType the value for T_POS_TRADE.TRADE_TYPE
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public void setTradeType(String tradeType) {
        this.tradeType = tradeType == null ? null : tradeType.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_POS_TRADE.BANK_NO
     *
     * @return the value of T_POS_TRADE.BANK_NO
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public String getBankNo() {
        return bankNo;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_POS_TRADE.BANK_NO
     *
     * @param bankNo the value for T_POS_TRADE.BANK_NO
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public void setBankNo(String bankNo) {
        this.bankNo = bankNo == null ? null : bankNo.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_POS_TRADE.CARD_TYPE
     *
     * @return the value of T_POS_TRADE.CARD_TYPE
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public String getCardType() {
        return cardType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_POS_TRADE.CARD_TYPE
     *
     * @param cardType the value for T_POS_TRADE.CARD_TYPE
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public void setCardType(String cardType) {
        this.cardType = cardType == null ? null : cardType.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_POS_TRADE.BANK
     *
     * @return the value of T_POS_TRADE.BANK
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public String getBank() {
        return bank;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_POS_TRADE.BANK
     *
     * @param bank the value for T_POS_TRADE.BANK
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public void setBank(String bank) {
        this.bank = bank == null ? null : bank.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_POS_TRADE.TRADE_AMOUNT
     *
     * @return the value of T_POS_TRADE.TRADE_AMOUNT
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public Money getTradeAmount() {
        return tradeAmount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_POS_TRADE.TRADE_AMOUNT
     *
     * @param tradeAmount the value for T_POS_TRADE.TRADE_AMOUNT
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public void setTradeAmount(Money tradeAmount) {
        this.tradeAmount = tradeAmount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_POS_TRADE.TRADE_FEE
     *
     * @return the value of T_POS_TRADE.TRADE_FEE
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public Money getTradeFee() {
        return tradeFee;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_POS_TRADE.TRADE_FEE
     *
     * @param tradeFee the value for T_POS_TRADE.TRADE_FEE
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public void setTradeFee(Money tradeFee) {
        this.tradeFee = tradeFee;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_POS_TRADE.TRADE_SUM
     *
     * @return the value of T_POS_TRADE.TRADE_SUM
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public Money getTradeSum() {
        return tradeSum;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_POS_TRADE.TRADE_SUM
     *
     * @param tradeSum the value for T_POS_TRADE.TRADE_SUM
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public void setTradeSum(Money tradeSum) {
        this.tradeSum = tradeSum;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_POS_TRADE.STATUS
     *
     * @return the value of T_POS_TRADE.STATUS
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public String getStatus() {
        return status;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_POS_TRADE.STATUS
     *
     * @param status the value for T_POS_TRADE.STATUS
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_POS_TRADE.TRADE_SRC_NO
     *
     * @return the value of T_POS_TRADE.TRADE_SRC_NO
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public String getTradeSrcNo() {
        return tradeSrcNo;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_POS_TRADE.TRADE_SRC_NO
     *
     * @param tradeSrcNo the value for T_POS_TRADE.TRADE_SRC_NO
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public void setTradeSrcNo(String tradeSrcNo) {
        this.tradeSrcNo = tradeSrcNo == null ? null : tradeSrcNo.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_POS_TRADE.REASON
     *
     * @return the value of T_POS_TRADE.REASON
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public String getReason() {
        return reason;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_POS_TRADE.REASON
     *
     * @param reason the value for T_POS_TRADE.REASON
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public void setReason(String reason) {
        this.reason = reason == null ? null : reason.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_POS_TRADE.REMARK
     *
     * @return the value of T_POS_TRADE.REMARK
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public String getRemark() {
        return remark;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_POS_TRADE.REMARK
     *
     * @param remark the value for T_POS_TRADE.REMARK
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_POS_TRADE.GMT_CREATE
     *
     * @return the value of T_POS_TRADE.GMT_CREATE
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public Date getGmtCreate() {
        return gmtCreate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_POS_TRADE.GMT_CREATE
     *
     * @param gmtCreate the value for T_POS_TRADE.GMT_CREATE
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_POS_TRADE.GMT_UPDATE
     *
     * @return the value of T_POS_TRADE.GMT_UPDATE
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public Date getGmtUpdate() {
        return gmtUpdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_POS_TRADE.GMT_UPDATE
     *
     * @param gmtUpdate the value for T_POS_TRADE.GMT_UPDATE
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public void setGmtUpdate(Date gmtUpdate) {
        this.gmtUpdate = gmtUpdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_POS_TRADE.OPERATOR
     *
     * @return the value of T_POS_TRADE.OPERATOR
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public String getOperator() {
        return operator;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_POS_TRADE.OPERATOR
     *
     * @param operator the value for T_POS_TRADE.OPERATOR
     *
     * @mbggenerated Mon May 09 14:14:01 CST 2016
     */
    public void setOperator(String operator) {
        this.operator = operator == null ? null : operator.trim();
    }
}