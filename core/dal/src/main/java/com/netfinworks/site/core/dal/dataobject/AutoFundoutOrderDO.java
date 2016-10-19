package com.netfinworks.site.core.dal.dataobject;

import java.util.Date;

import com.netfinworks.common.util.money.Money;

public class AutoFundoutOrderDO {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_AUTO_FUNDOUT_ORDER.OUTER_TRADE_NO
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    private String outerTradeNo;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_AUTO_FUNDOUT_ORDER.FUNDOUT_ORDER_NO
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    private String fundoutOrderNo;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_AUTO_FUNDOUT_ORDER.AUTH_ORDER_NO
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    private String authOrderNo;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_AUTO_FUNDOUT_ORDER.AMOUNT
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
	private Money amount;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_AUTO_FUNDOUT_ORDER.MEMBER_ID
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    private String memberId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_AUTO_FUNDOUT_ORDER.MEMBER_IDENTITY
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    private String memberIdentity;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_AUTO_FUNDOUT_ORDER.BANK_CARD_INFO
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    private String bankCardInfo;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_AUTO_FUNDOUT_ORDER.WITHDRAWAL_STATUS
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    private String withdrawalStatus;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_AUTO_FUNDOUT_ORDER.FAIL_REASON
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    private String failReason;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_AUTO_FUNDOUT_ORDER.EXTENSION
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    private String extension;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_AUTO_FUNDOUT_ORDER.GMT_CREATED
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    private Date gmtCreated;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column T_AUTO_FUNDOUT_ORDER.GMT_MODIFIED
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    private Date gmtModified;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_AUTO_FUNDOUT_ORDER.OUTER_TRADE_NO
     *
     * @return the value of T_AUTO_FUNDOUT_ORDER.OUTER_TRADE_NO
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    public String getOuterTradeNo() {
        return outerTradeNo;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_AUTO_FUNDOUT_ORDER.OUTER_TRADE_NO
     *
     * @param outerTradeNo the value for T_AUTO_FUNDOUT_ORDER.OUTER_TRADE_NO
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    public void setOuterTradeNo(String outerTradeNo) {
        this.outerTradeNo = outerTradeNo == null ? null : outerTradeNo.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_AUTO_FUNDOUT_ORDER.FUNDOUT_ORDER_NO
     *
     * @return the value of T_AUTO_FUNDOUT_ORDER.FUNDOUT_ORDER_NO
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    public String getFundoutOrderNo() {
        return fundoutOrderNo;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_AUTO_FUNDOUT_ORDER.FUNDOUT_ORDER_NO
     *
     * @param fundoutOrderNo the value for T_AUTO_FUNDOUT_ORDER.FUNDOUT_ORDER_NO
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    public void setFundoutOrderNo(String fundoutOrderNo) {
        this.fundoutOrderNo = fundoutOrderNo == null ? null : fundoutOrderNo.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_AUTO_FUNDOUT_ORDER.AUTH_ORDER_NO
     *
     * @return the value of T_AUTO_FUNDOUT_ORDER.AUTH_ORDER_NO
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    public String getAuthOrderNo() {
        return authOrderNo;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_AUTO_FUNDOUT_ORDER.AUTH_ORDER_NO
     *
     * @param authOrderNo the value for T_AUTO_FUNDOUT_ORDER.AUTH_ORDER_NO
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    public void setAuthOrderNo(String authOrderNo) {
        this.authOrderNo = authOrderNo == null ? null : authOrderNo.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_AUTO_FUNDOUT_ORDER.AMOUNT
     *
     * @return the value of T_AUTO_FUNDOUT_ORDER.AMOUNT
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
	public Money getAmount() {
        return amount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_AUTO_FUNDOUT_ORDER.AMOUNT
     *
     * @param amount the value for T_AUTO_FUNDOUT_ORDER.AMOUNT
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
	public void setAmount(Money amount) {
        this.amount = amount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_AUTO_FUNDOUT_ORDER.MEMBER_ID
     *
     * @return the value of T_AUTO_FUNDOUT_ORDER.MEMBER_ID
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    public String getMemberId() {
        return memberId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_AUTO_FUNDOUT_ORDER.MEMBER_ID
     *
     * @param memberId the value for T_AUTO_FUNDOUT_ORDER.MEMBER_ID
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    public void setMemberId(String memberId) {
        this.memberId = memberId == null ? null : memberId.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_AUTO_FUNDOUT_ORDER.MEMBER_IDENTITY
     *
     * @return the value of T_AUTO_FUNDOUT_ORDER.MEMBER_IDENTITY
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    public String getMemberIdentity() {
        return memberIdentity;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_AUTO_FUNDOUT_ORDER.MEMBER_IDENTITY
     *
     * @param memberIdentity the value for T_AUTO_FUNDOUT_ORDER.MEMBER_IDENTITY
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    public void setMemberIdentity(String memberIdentity) {
        this.memberIdentity = memberIdentity == null ? null : memberIdentity.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_AUTO_FUNDOUT_ORDER.BANK_CARD_INFO
     *
     * @return the value of T_AUTO_FUNDOUT_ORDER.BANK_CARD_INFO
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    public String getBankCardInfo() {
        return bankCardInfo;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_AUTO_FUNDOUT_ORDER.BANK_CARD_INFO
     *
     * @param bankCardInfo the value for T_AUTO_FUNDOUT_ORDER.BANK_CARD_INFO
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    public void setBankCardInfo(String bankCardInfo) {
        this.bankCardInfo = bankCardInfo == null ? null : bankCardInfo.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_AUTO_FUNDOUT_ORDER.WITHDRAWAL_STATUS
     *
     * @return the value of T_AUTO_FUNDOUT_ORDER.WITHDRAWAL_STATUS
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    public String getWithdrawalStatus() {
        return withdrawalStatus;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_AUTO_FUNDOUT_ORDER.WITHDRAWAL_STATUS
     *
     * @param withdrawalStatus the value for T_AUTO_FUNDOUT_ORDER.WITHDRAWAL_STATUS
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    public void setWithdrawalStatus(String withdrawalStatus) {
        this.withdrawalStatus = withdrawalStatus == null ? null : withdrawalStatus.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_AUTO_FUNDOUT_ORDER.FAIL_REASON
     *
     * @return the value of T_AUTO_FUNDOUT_ORDER.FAIL_REASON
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    public String getFailReason() {
        return failReason;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_AUTO_FUNDOUT_ORDER.FAIL_REASON
     *
     * @param failReason the value for T_AUTO_FUNDOUT_ORDER.FAIL_REASON
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    public void setFailReason(String failReason) {
        this.failReason = failReason == null ? null : failReason.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_AUTO_FUNDOUT_ORDER.EXTENSION
     *
     * @return the value of T_AUTO_FUNDOUT_ORDER.EXTENSION
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    public String getExtension() {
        return extension;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_AUTO_FUNDOUT_ORDER.EXTENSION
     *
     * @param extension the value for T_AUTO_FUNDOUT_ORDER.EXTENSION
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    public void setExtension(String extension) {
        this.extension = extension == null ? null : extension.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_AUTO_FUNDOUT_ORDER.GMT_CREATED
     *
     * @return the value of T_AUTO_FUNDOUT_ORDER.GMT_CREATED
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    public Date getGmtCreated() {
        return gmtCreated;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_AUTO_FUNDOUT_ORDER.GMT_CREATED
     *
     * @param gmtCreated the value for T_AUTO_FUNDOUT_ORDER.GMT_CREATED
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    public void setGmtCreated(Date gmtCreated) {
        this.gmtCreated = gmtCreated;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column T_AUTO_FUNDOUT_ORDER.GMT_MODIFIED
     *
     * @return the value of T_AUTO_FUNDOUT_ORDER.GMT_MODIFIED
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    public Date getGmtModified() {
        return gmtModified;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column T_AUTO_FUNDOUT_ORDER.GMT_MODIFIED
     *
     * @param gmtModified the value for T_AUTO_FUNDOUT_ORDER.GMT_MODIFIED
     *
     * @mbggenerated Tue Apr 14 11:54:31 CST 2015
     */
    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }
}