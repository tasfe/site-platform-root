package com.netfinworks.site.domainservice.pos;

import java.util.Date;

import com.netfinworks.biz.common.util.QueryBase;



public class PosTradeRequest {

	/** 交易单号 */
    private String          tradeNo;

	/** 会员ID */
    private String          memberId;
    
    /** 交易单号 */
    private String          tradeSrcNo;
    
    /** 查询起始时间 */
    private Date            startTime;
    
    /** 查询结束时间 */
    private Date            endTime;
    
    /** 查询交易类型 */
    private String          tradeType;
    
    /** 查询交易状态 */
    private String          status;
    
    /** 分页信息 */
    private QueryBase           queryBase;
    

	public QueryBase getQueryBase() {
		return queryBase;
	}

	public void setQueryBase(QueryBase queryBase) {
		this.queryBase = queryBase;
	}

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public String getTradeType() {
		return tradeType;
	}

	public void setTradeType(String tradeType) {
		this.tradeType = tradeType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getTradeSrcNo() {
		return tradeSrcNo;
	}

	public void setTradeSrcNo(String tradeSrcNo) {
		this.tradeSrcNo = tradeSrcNo;
	}

	public String getTradeNo() {
		return tradeNo;
	}

	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}
    
}
