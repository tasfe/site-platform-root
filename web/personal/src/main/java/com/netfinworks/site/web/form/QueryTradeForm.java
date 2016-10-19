package com.netfinworks.site.web.form;

import java.io.Serializable;

/**
 * <p>
 * 企业钱包查询页面
 * </p>
 * 
 * @author zhangyun.m
 * @version $Id: QueryTradeForm.java, v 0.1 2013-12-10 下午5:04:41 HP Exp $
 */
public class QueryTradeForm implements Serializable {
	private static final long serialVersionUID = 1L;

	/** 卖家Id */
	private String memberId;
	/** 查询开始时间 */
	private String queryTime;
	/** 账户余额： 账户类型 */
	private String accountType;
	/**
	 * 账户余额：交易类型 枚举 1.所有，2.支出，3.收入
	 * */
	private String txnType;
	/** 设置每页大小 */
	// private String pageSize;
	/** 当前页 */
	private String currentPage;
	/** 时间段查询：开始时间 */
	private String queryStartTime;
	/** 时间段查询：结束时间 */
	private String queryEndTime;
	/**关键字对应项*/
	private String keyword;
	/** 关键字对应值*/
	private String keyvalue;
	/**交易状态*/
	private String tradeStatus;
	/**交易类型*/
	private String tradeType;
	/**退款金额*/
	private String refundAmount;
	/**备注*/
	private String remarks;
	/***/
	private String success;
	public String getSuccess() {
		return success;
	}

	public void setSuccess(String success) {
		this.success = success;
	}

	public String getRefundAmount() {
		return refundAmount;
	}

	public void setRefundAmount(String refundAmount) {
		this.refundAmount = refundAmount;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getTradeType() {
		return tradeType;
	}

	public void setTradeType(String tradeType) {
		this.tradeType = tradeType;
	}

	public String getTradeStatus() {
		return tradeStatus;
	}

	public void setTradeStatus(String tradeStatus) {
		this.tradeStatus = tradeStatus;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getKeyvalue() {
		return keyvalue;
	}

	public void setKeyvalue(String keyvalue) {
		this.keyvalue = keyvalue;
	}

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public String getQueryTime() {
		return queryTime;
	}

	public void setQueryTime(String queryTime) {
		this.queryTime = queryTime;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public String getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(String currentPage) {
		this.currentPage = currentPage;
	}

	public String getQueryStartTime() {
		return queryStartTime;
	}

	public void setQueryStartTime(String queryStartTime) {
		this.queryStartTime = queryStartTime;
	}

	public String getQueryEndTime() {
		return queryEndTime;
	}

	public void setQueryEndTime(String queryEndTime) {
		this.queryEndTime = queryEndTime;
	}

}
