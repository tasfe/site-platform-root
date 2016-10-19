package com.netfinworks.site.domain.domain.trade;

import java.io.Serializable;
import java.util.Date;

import com.netfinworks.biz.common.util.QueryBase;

/**
 * <p>审核列表查询domain</p>
 * @author yangshihong
 * @version $Id: AuditListQuery.java, v 0.1 2014年5月22日 上午11:23:02 hdfs Exp $
 */
public class AuditListQuery implements Serializable {

    /**会员ID */
    private String memberId;
    
    /**类型 */
    private String auditType;
    
    /**状态 */
    private String status;
    
    /**开始时间 */
    private Date startDate;
    
    /**结束时间 */
    private Date endDate;

    /**分页*/
    private QueryBase queryBase;
    
    /**关键字*/
    private String selectType;
    
    /**关键值*/
    private String value;
    
	/**
     * 提现类型
     */
    private String txType;
    
    /**
     * 应用类型
     */
    private String appType;
    
    /**交易订单号*/
    private String transId;
    
    /**申请时间、审核时间*/
    private String queryByTime;
    
    private String auditSubType;
    
	public String getQueryByTime() {
		return queryByTime;
	}

	public void setQueryByTime(String queryByTime) {
		this.queryByTime = queryByTime;
	}

	public String getTransId() {
		return transId;
	}

	public void setTransId(String transId) {
		this.transId = transId;
	}

	public String getAppType() {
		return appType;
	}

	public void setAppType(String appType) {
		this.appType = appType;
	}

	public String getTxType() {
		return txType;
	}

	public void setTxType(String txType) {
		this.txType = txType;
	}

	public String getSelectType() {
		return selectType;
	}

	public void setSelectType(String selectType) {
		this.selectType = selectType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

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
     * @return the auditType
     */
    public String getAuditType() {
        return auditType;
    }

    /**
     * @param auditType the auditType to set
     */
    public void setAuditType(String auditType) {
        this.auditType = auditType;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the startDate
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * @return the endDate
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * @param endDate the endDate to set
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * @return the queryBase
     */
    public QueryBase getQueryBase() {
        return queryBase;
    }

    /**
     * @param queryBase the queryBase to set
     */
    public void setQueryBase(QueryBase queryBase) {
        this.queryBase = queryBase;
    }

    public String getAuditSubType() {
        return auditSubType;
    }

    public void setAuditSubType(String auditSubType) {
        this.auditSubType = auditSubType;
    }
    
}
