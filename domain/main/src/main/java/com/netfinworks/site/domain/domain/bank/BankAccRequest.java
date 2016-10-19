/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.domain.domain.bank;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.common.domain.OperationEnvironment;

/**
 * 通用说明： 银行卡信息
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-26 下午3:30:55
 *
 */
public class BankAccRequest extends OperationEnvironment {
    /**
     *
     */
    private static final long serialVersionUID = -1895631700707341379L;
    /**会员Id*/
    private String            memberId;
    /**会员实名*/
    private String            realName;
    /**会员银行卡id*/
    private String            bankcardId;
    /**银行代码*/
    private String            bankCode;
    /**银行名称*/
    private String            bankName;
    /**分行ID    */
    private String            branchId;
    /**联行号    */
    private String            branchNo;
    /**分行名称    */
    private String            branchName;
    /**分行简称    */
    private String            branchShortName;
    /**省名称    */
    private String            provName;
    /**城市名称    */
    private String            cityName;
    /**银行卡号码*/
    private String            bankAccountNum;
    /**卡属性(0对公 1对私)*/
    private int               cardAttribute;
    /**枚举
     * （qpay：大快捷，bsqpay：小快捷，umppay：联动优势，normal：普通卡，trust_collect：代扣）
     */
    private String            payAttribute;
    /**卡类型**/
    private int               cardType;
	/** 认证状态(0未认证 1已认证 2认证中 3认证失败) **/
	private Integer isVerified;

	/** 扩展信息 */
	private String extInfo;

	/** 操作员Id */
	private String operatorId;
	
	/**
	 * 银行卡状态(0失效  1正常 2锁定)
	 */
	private Integer status;

	/** 会员标识 */
	private String memberIdentity;

	 /** 预留手机号*/
    private String mobile;
    
    public int getCardType() {
        return cardType;
    }

    public void setCardType(int cardType) {
        this.cardType = cardType;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getBankcardId() {
		return bankcardId;
	}

	public void setBankcardId(String bankcardId) {
		this.bankcardId = bankcardId;
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

    public int getCardAttribute() {
        return cardAttribute;
    }

    public void setCardAttribute(int cardAttribute) {
        this.cardAttribute = cardAttribute;
    }

    public String getPayAttribute() {
        return payAttribute;
    }

    public void setPayAttribute(String payAttribute) {
        this.payAttribute = payAttribute;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
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

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

	public String getExtInfo() {
		return extInfo;
	}

	public void setExtInfo(String extInfo) {
		this.extInfo = extInfo;
	}

	public Integer getIsVerified() {
		return isVerified;
	}

	public void setIsVerified(Integer isVerified) {
		this.isVerified = isVerified;
	}

	public String getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}
	

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getMemberIdentity() {
		return memberIdentity;
	}

	public void setMemberIdentity(String memberIdentity) {
		this.memberIdentity = memberIdentity;
	}
	
	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	@Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
