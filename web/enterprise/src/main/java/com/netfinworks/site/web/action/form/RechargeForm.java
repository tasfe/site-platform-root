package com.netfinworks.site.web.action.form;

import java.io.Serializable;

import javax.validation.constraints.Pattern;

import com.netfinworks.site.core.common.constants.ValidationConstants;

/**
 * 通用说明：转账输入form
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-14 下午3:00:04
 *
 */
public class RechargeForm implements Serializable {

    private static final long serialVersionUID = -1949768627245411279L;
    /**账户*/
    private String            memberId;

    /**收款人手机号*/
    @Pattern(regexp = ValidationConstants.MOBILE_PATTERN, message = "mobile_error")
    private String            mobile;

    /**转账金额*/
    @Pattern(regexp = ValidationConstants.MONEY_PATTERN, message = "moneynum_error")
    private String            transferNum;
    /**转账说明*/
    private String            transferInfo;
    /**发短信*/
    private String            msn;
    /**企业邮箱*/
    private String            compEmail;

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

    public String getTransferNum() {
        return transferNum;
    }

    public void setTransferNum(String transferNum) {
        this.transferNum = transferNum;
    }

    public String getTransferInfo() {
        return transferInfo;
    }

    public void setTransferInfo(String transferInfo) {
        this.transferInfo = transferInfo;
    }

    public String getMsn() {
        return msn;
    }

    public void setMsn(String msn) {
        this.msn = msn;
    }

    public String getCompEmail() {
        return compEmail;
    }

    public void setCompEmail(String compEmail) {
        this.compEmail = compEmail;
    }

}
