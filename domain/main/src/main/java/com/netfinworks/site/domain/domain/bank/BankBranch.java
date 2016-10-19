package com.netfinworks.site.domain.domain.bank;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 *
 * <p>分行信息</p>
 * @author qinde
 * @version $Id: BankBranch.java, v 0.1 2013-11-29 下午4:25:43 qinde Exp $
 */
public class BankBranch implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -7056422307315254159L;
    /**分行ID    */
    long                      branchId;
    /**联行号    */
    private String            branchNo;
    /**分行名称    */
    private String            branchName;
    /**分行简称    */
    private String            branchShortName;
    /**银行代码    */
    private String            bankCode;
    /**省名称    */
    private String            provName;
    /**城市名称    */
    private String            cityName;

    public long getBranchId() {
        return branchId;
    }

    public void setBranchId(long branchId) {
        this.branchId = branchId;
    }

    public String getBranchNo() {
        return branchNo;
    }

    public void setBranchNo(String branchNo) {
        this.branchNo = branchNo;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBranchShortName() {
        return branchShortName;
    }

    public void setBranchShortName(String branchShortName) {
        this.branchShortName = branchShortName;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getProvName() {
        return provName;
    }

    public void setProvName(String provName) {
        this.provName = provName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
