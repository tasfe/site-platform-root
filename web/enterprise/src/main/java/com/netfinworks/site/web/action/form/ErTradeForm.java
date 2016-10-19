package com.netfinworks.site.web.action.form;

public class ErTradeForm {	
	/** 时间段查询：开始时间 */
	private String queryStartTime;
	
	/** 时间段查询：结束时间 */
	private String queryEndTime;
	/**
	 * 查询类型
	 * */
	private String txnType;
	/**
	 * 交易类型 
	 */
	private String tradeType;
	/**
	 * 下载状态
	 * */
	private String downStatus;
	
	/** 当前页 */
	private String currentPage;
	/**关键字类型*/
	private String keyword;
	/**关键字内容*/
	private String keyvalue;
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

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public String getTradeType() {
		return tradeType;
	}

	public void setTradeType(String tradeType) {
		this.tradeType = tradeType;
	}

	public String getDownStatus() {
		return downStatus;
	}

	public void setDownStatus(String downStatus) {
		this.downStatus = downStatus;
	}

	public String getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(String currentPage) {
		this.currentPage = currentPage;
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

}
