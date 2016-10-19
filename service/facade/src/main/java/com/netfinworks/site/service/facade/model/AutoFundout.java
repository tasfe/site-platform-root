package com.netfinworks.site.service.facade.model;

import java.io.Serializable;
import java.util.Date;

import com.netfinworks.common.util.money.Money;

/**
 * <p>出款信息</p>
 * @author zhangjq
 * @version 
 */
public class AutoFundout implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4195759502236128389L;
	/** 商户订单号 */
	private String outerTradeNo;
	/** 出款订单号 */
	private String fundoutOrderNo;
	/** 银行卡认证订单号 */
	private String authOrderNo;
	/** 打款金额 */
	private Money amount;
	/** 会员编号 */
	private String memberId;
	/** 会员标识 */
	private String memberIdentity;
	/** 银行卡相关信息 JSON格式 */
	private String bankCardInfo;
	/** 出款状态 */
	private String withdrawalStatus;
	/** 失败原因 */
	private String failReason;
	/** 扩展信息 */
	private String extension;
	/** 创建时间 */
	private Date gmtCreated;
	/** 修改时间 */
	private Date gmtModified;

	public String getOuterTradeNo() {
		return outerTradeNo;
	}

	public void setOuterTradeNo(String outerTradeNo) {
		this.outerTradeNo = outerTradeNo;
	}

	public String getFundoutOrderNo() {
		return fundoutOrderNo;
	}

	public void setFundoutOrderNo(String fundoutOrderNo) {
		this.fundoutOrderNo = fundoutOrderNo;
	}

	public String getAuthOrderNo() {
		return authOrderNo;
	}

	public void setAuthOrderNo(String authOrderNo) {
		this.authOrderNo = authOrderNo;
	}

	public Money getAmount() {
		return amount;
	}

	public void setAmount(Money amount) {
		this.amount = amount;
	}

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public String getMemberIdentity() {
		return memberIdentity;
	}

	public void setMemberIdentity(String memberIdentity) {
		this.memberIdentity = memberIdentity;
	}

	public String getBankCardInfo() {
		return bankCardInfo;
	}

	public void setBankCardInfo(String bankCardInfo) {
		this.bankCardInfo = bankCardInfo;
	}

	public String getWithdrawalStatus() {
		return withdrawalStatus;
	}

	public void setWithdrawalStatus(String withdrawalStatus) {
		this.withdrawalStatus = withdrawalStatus;
	}

	public String getFailReason() {
		return failReason;
	}

	public void setFailReason(String failReason) {
		this.failReason = failReason;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public Date getGmtCreated() {
		return gmtCreated;
	}

	public void setGmtCreated(Date gmtCreated) {
		this.gmtCreated = gmtCreated;
	}

	public Date getGmtModified() {
		return gmtModified;
	}

	public void setGmtModified(Date gmtModified) {
		this.gmtModified = gmtModified;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
