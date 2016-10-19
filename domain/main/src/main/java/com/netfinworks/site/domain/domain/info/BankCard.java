package com.netfinworks.site.domain.domain.info;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 *
 * <p>银行信息</p>
 * @author qinde
 * @version $Id: BankCard.java, v 0.1 2013-11-28 下午8:45:14 qinde Exp $
 */
public class BankCard implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 4732180536645733054L;
    private String            bankId;
    private String            bankName;
    private String            branchId;
    private String            branchName;
    private String            cardNo;

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
