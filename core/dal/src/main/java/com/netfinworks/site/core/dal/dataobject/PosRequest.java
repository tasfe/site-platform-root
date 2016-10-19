package com.netfinworks.site.core.dal.dataobject;

import java.util.Date;


public class PosRequest {
	
	/** 交易单号 */
    private String          tradeNo;
    
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
    
    /** 会员ID */
    private String          memberNo;
    
    /**分页 */
    private int          page1;
    
    /**分页*/
    private int          page2;
    
    
	public int getPage1() {
		return page1;
	}

	public void setPage1(int page1) {
		this.page1 = page1;
	}

	public int getPage2() {
		return page2;
	}

	public void setPage2(int page2) {
		this.page2 = page2;
	}

	public String getTradeNo() {
		return tradeNo;
	}

	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}

	public String getTradeSrcNo() {
		return tradeSrcNo;
	}

	public void setTradeSrcNo(String tradeSrcNo) {
		this.tradeSrcNo = tradeSrcNo;
	}

	public String getMemberNo() {
		return memberNo;
	}

	public void setMemberNo(String memberNo) {
		this.memberNo = memberNo;
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
    
    
}
