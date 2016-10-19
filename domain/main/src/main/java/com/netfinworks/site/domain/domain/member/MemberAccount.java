package com.netfinworks.site.domain.domain.member;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.common.util.money.Money;

/**
 *
 * <p>账户信息</p>
 * @author qinde
 * @version $Id: Account.java, v 0.1 2013-12-7 下午2:24:15 qinde Exp $
 */
public class MemberAccount implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 2336589112115066757L;
    /**会员ID*/
    private String            memberId;
    /**账户ID     */
    private String            accountId;
    /**账户名称     */
    private String            accountName;
    /**账户类型     */
    private Long              accountType;
    /**账户总余额     */
    private Money             balance;
    /**可以余额     */
    private Money             availableBalance;
    /**冻结余额     */
    private Money             frozenBalance;
    /**可提现余额     */
    private Money             withdrawBalance;
    /**账户状态     */
    private Long              activateStatus;
    /**冻结状态     */
    private Long              freezeStatus;
    /**     */
    private Long              lifeCycleStatus;
    /**     */
    private String            currencyCode;
    /**账户属性     */
    private Long              accountAttribute;

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public Long getAccountType() {
        return accountType;
    }

    public void setAccountType(Long accountType) {
        this.accountType = accountType;
    }

    public Money getBalance() {
        return balance;
    }

    public void setBalance(Money balance) {
        this.balance = balance;
    }

    public Money getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(Money availableBalance) {
        this.availableBalance = availableBalance;
    }

    public Money getFrozenBalance() {
        return frozenBalance;
    }

    public void setFrozenBalance(Money frozenBalance) {
        this.frozenBalance = frozenBalance;
    }

    public Money getWithdrawBalance() {
        return withdrawBalance;
    }

    public void setWithdrawBalance(Money withdrawBalance) {
        this.withdrawBalance = withdrawBalance;
    }

    public Long getActivateStatus() {
        return activateStatus;
    }

    public void setActivateStatus(Long activateStatus) {
        this.activateStatus = activateStatus;
    }

    public Long getFreezeStatus() {
        return freezeStatus;
    }

    public void setFreezeStatus(Long freezeStatus) {
        this.freezeStatus = freezeStatus;
    }

    public Long getLifeCycleStatus() {
        return lifeCycleStatus;
    }

    public void setLifeCycleStatus(Long lifeCycleStatus) {
        this.lifeCycleStatus = lifeCycleStatus;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Long getAccountAttribute() {
        return accountAttribute;
    }

    public void setAccountAttribute(Long accountAttribute) {
        this.accountAttribute = accountAttribute;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
