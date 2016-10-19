package com.netfinworks.site.web.action.form;

import java.io.Serializable;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotEmpty;

import com.netfinworks.site.core.common.constants.ValidationConstants;

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

    /**账户*/
    private String            memberId;

    @Pattern(regexp = ValidationConstants.EMAIL_MOBILE_APTTERN, message = "email_mobile_error")
    private String            identityNo;
    
    /**平台类型*/
    private String            platformType;

    /**转账金额*/
    @Pattern(regexp = ValidationConstants.MONEY_PATTERN, message = "moneynum_error")
    private String            transferNum;
    
    /**转账说明*/
    private String            transferInfo;
    
    /**发短信*/
    private String            msn;
    
    /**企业邮箱*/
    private String            compEmail;
    
    /**支付密码*/
    @NotEmpty(message = "paypasswd_not_exist")
    private String            password;

    /**
     * @return the memberId
     */
    public String getMemberId() {
        return memberId;
    }

    /**
     * @param memberId the memberId to set
     */
    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    /**
     * @return the identityNo
     */
    public String getIdentityNo() {
        return identityNo;
    }

    /**
     * @param identityNo the identityNo to set
     */
    public void setIdentityNo(String identityNo) {
        this.identityNo = identityNo;
    }

    /**
     * @return the platformType
     */
    public String getPlatformType() {
        return platformType;
    }

    /**
     * @param platformType the platformType to set
     */
    public void setPlatformType(String platformType) {
        this.platformType = platformType;
    }

    /**
     * @return the transferNum
     */
    public String getTransferNum() {
        return transferNum;
    }

    /**
     * @param transferNum the transferNum to set
     */
    public void setTransferNum(String transferNum) {
        this.transferNum = transferNum;
    }

    /**
     * @return the transferInfo
     */
    public String getTransferInfo() {
        return transferInfo;
    }

    /**
     * @param transferInfo the transferInfo to set
     */
    public void setTransferInfo(String transferInfo) {
        this.transferInfo = transferInfo;
    }

    /**
     * @return the msn
     */
    public String getMsn() {
        return msn;
    }

    /**
     * @param msn the msn to set
     */
    public void setMsn(String msn) {
        this.msn = msn;
    }

    /**
     * @return the compEmail
     */
    public String getCompEmail() {
        return compEmail;
    }

    /**
     * @param compEmail the compEmail to set
     */
    public void setCompEmail(String compEmail) {
        this.compEmail = compEmail;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

}
