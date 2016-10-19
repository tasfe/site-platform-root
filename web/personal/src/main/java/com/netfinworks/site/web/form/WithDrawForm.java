/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.web.form;

import java.io.Serializable;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotEmpty;

import com.netfinworks.site.core.common.constants.ValidationConstants;

/**
 * 通用说明：提现输入form
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-14 下午3:00:04
 *
 */
public class WithDrawForm implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 2837757856564332144L;

    /**账户*/
    private String            memberId;

    /**手机号*/
    private String            mobile;
    /**账户余额*/
    private String            fee;

    /**提现金额*/
    @Pattern(regexp = ValidationConstants.MONEY_PATTERN, message = "moneynum_error")
    private String            moneyNum;
    /**转账银行卡Id*/
    private String            bankcardId;
    /**支付密码*/
    @NotEmpty(message = "paypasswd_not_exist")
    private String            passwd;

    private String            passwdKey;

    private String passwdMode;

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getMoneyNum() {
        return moneyNum;
    }

    public void setMoneyNum(String moneyNum) {
        this.moneyNum = moneyNum;
    }

    public String getBankcardId() {
        return bankcardId;
    }

    public void setBankcardId(String bankcardId) {
        this.bankcardId = bankcardId;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public String getPasswdKey() {
        return passwdKey;
    }

    public void setPasswdKey(String passwdKey) {
        this.passwdKey = passwdKey;
    }

    public String getPasswdMode() {
        return passwdMode;
    }

    public void setPasswdMode(String passwdMode) {
        this.passwdMode = passwdMode;
    }

}
