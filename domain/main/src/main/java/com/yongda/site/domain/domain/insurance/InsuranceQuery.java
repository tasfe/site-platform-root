package com.yongda.site.domain.domain.insurance;

import java.io.Serializable;
import java.util.Date;

import com.netfinworks.biz.common.util.QueryBase;

public class InsuranceQuery implements Serializable {

	private static final long serialVersionUID = 7327847813746547830L;

	/**分页*/
    private QueryBase queryBase;
	
	/**会员ID */
    private String memberId;
    
    /**状态 */
    private String status;
    
    /**开始时间 */
    private Date startDate;
    
    /**结束时间 */
    private Date endDate;

    /** 保单号 */
    private String bxgsId;
    
    /** 保险公司 */
    private String company;

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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getBxgsId() {
		return bxgsId;
	}

	public void setBxgsId(String bxgsId) {
		this.bxgsId = bxgsId;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}
}
