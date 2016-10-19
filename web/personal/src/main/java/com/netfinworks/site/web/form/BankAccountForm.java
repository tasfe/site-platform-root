package com.netfinworks.site.web.form;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.netfinworks.site.core.common.constants.ValidationConstants;

/**
 * 通用说明：添加银行卡
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-26 下午6:26:08
 *
 */
public class BankAccountForm implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 8639640820604078798L;

    /**会员Id*/
    private String            memberId;
    /**省份*/
    @NotNull(message = "province_not_exist")
    @Size(min = 1,  message = "province_not_exist")
    private String            province;
    /**城市*/
    private String            city;
    /**银行卡号*/
    private String            BankcardId;
    /**银行代码*/
    @NotNull(message = "bankcode_not_exist")
    @Size(min = 1,  message = "bankcode_not_exist")
    private String            bankCode;
    /**银行名称*/
    private String            bankName;
    /**分行代码*/
    @NotNull(message = "branchcode_not_exist")
    @Size(min = 1,  message = "branchcode_not_exist")
    private String            branchCode;
    /**分行名称*/
    private String            branchName;
    /**联行号*/
    private String            branchNo;
    /**银行卡号码*/
    @NotNull(message = "bankcardno_not_exist")
    @Size(min = 1,  message = "bankcardno_not_exist")
    @Pattern(regexp = ValidationConstants.BANK_CARD_NO_PATTERN, message = "bankcardno_pattern_not_right")
    private String            bankAccountNum;
    /**卡属性(0对公 1对私)*/
    private String            cardAttribute;
    /**枚举
     * （qpay：大快捷，bsqpay：小快捷，umppay：联动优势，normal：普通卡，trust_collect：代扣）
     */
    private String            payAttribute;
    
    /** 户名 */
    @NotNull(message = "realname_not_exist")
    private String            realName;

	/** 分行简称 */
	private String branchShortName;

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getBankcardId() {
        return BankcardId;
    }

    public void setBankcardId(String bankcardId) {
        BankcardId = bankcardId;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankAccountNum() {
        return bankAccountNum;
    }

    public void setBankAccountNum(String bankAccountNum) {
        this.bankAccountNum = bankAccountNum;
    }

    public String getCardAttribute() {
        return cardAttribute;
    }

    public void setCardAttribute(String cardAttribute) {
        this.cardAttribute = cardAttribute;
    }

    public String getPayAttribute() {
        return payAttribute;
    }

    public void setPayAttribute(String payAttribute) {
        this.payAttribute = payAttribute;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
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

	public String getBranchNo() {
        return branchNo;
    }

    public void setBranchNo(String branchNo) {
        this.branchNo = branchNo;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }
    

}
