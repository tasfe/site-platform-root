/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.web.form;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

/**
 * 通用说明：充值form
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-14 下午3:00:04
 *
 */
public class TransferForm implements Serializable {
    private static final long serialVersionUID = 5325161679649149687L;

    private String            memberId;                               //账户
    @NotNull(message = "moneynum_not_exist")
    private String            moneyNum;                               //充值金额
    private String            bank_ico;                               //登录名称

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getMoneyNum() {
        return moneyNum;
    }

    public void setMoneyNum(String moneyNum) {
        this.moneyNum = moneyNum;
    }

    public String getBank_ico() {
        return bank_ico;
    }

    public void setBank_ico(String bank_ico) {
        this.bank_ico = bank_ico;
    }

}
