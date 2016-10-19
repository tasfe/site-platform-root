package com.yongda.site.domain.domain.cashier;

import java.util.Date;

public class PayLimit {
	// 主键id
    private Integer            limitId;
    // 银行代码
    private String            bankCode;
    // 0-首次支付，1-二次支付
    private Integer            payTimes;
    // 支付渠道
    private String            payChannel;
    // 单笔限额
    private String            singleLimit;
    // 日限额
    private String            dailyLimit;
    // 月限额
    private String            monthLimit;
    // 备注
    private String            memo;
    // 新增时间
    private Date              gmtCreate;
    // 修改时间
    private Date              gmtModified;
    // 扩展字段
    private String            extension;
	public Integer getLimitId() {
		return limitId;
	}
	public void setLimitId(Integer limitId) {
		this.limitId = limitId;
	}
	public String getBankCode() {
		return bankCode;
	}
	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}
	public Integer getPayTimes() {
		return payTimes;
	}
	public void setPayTimes(Integer payTimes) {
		this.payTimes = payTimes;
	}
	public String getPayChannel() {
		return payChannel;
	}
	public void setPayChannel(String payChannel) {
		this.payChannel = payChannel;
	}
	public String getSingleLimit() {
		return singleLimit;
	}
	public void setSingleLimit(String singleLimit) {
		this.singleLimit = singleLimit;
	}
	public String getDailyLimit() {
		return dailyLimit;
	}
	public void setDailyLimit(String dailyLimit) {
		this.dailyLimit = dailyLimit;
	}
	public String getMonthLimit() {
		return monthLimit;
	}
	public void setMonthLimit(String monthLimit) {
		this.monthLimit = monthLimit;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	public Date getGmtCreate() {
		return gmtCreate;
	}
	public void setGmtCreate(Date gmtCreate) {
		this.gmtCreate = gmtCreate;
	}
	public Date getGmtModified() {
		return gmtModified;
	}
	public void setGmtModified(Date gmtModified) {
		this.gmtModified = gmtModified;
	}
	public String getExtension() {
		return extension;
	}
	public void setExtension(String extension) {
		this.extension = extension;
	}
}
