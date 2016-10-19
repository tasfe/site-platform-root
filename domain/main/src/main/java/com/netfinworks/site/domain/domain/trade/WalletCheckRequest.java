package com.netfinworks.site.domain.domain.trade;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.domain.enums.DealType;

/**
 * <p>企业钱包对账请求</p>
 * @author zhangyun.m
 * @version $Id: WalletCheckRequestInfo.java, v 0.1 2013-12-4 上午9:58:22 HP Exp $
 */
public class WalletCheckRequest extends OperationEnvironment implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会员ID(卖家Id) */
    private String            memberId;
    /** 查询开始时间 */
    private Date              beginTime;
    /** 查询结束时间 */
    private Date              endTime;
    /**账户余额： 账户类型 */
    private Integer           accountType;
    /**账户余额：交易类型 */
    private DealType          txnType;
    /** 查询上月余额时间 last month last day*/
    private Date              lmlday;
    /** 账户号 */
    private String            accountNo;
    /** 是否需要汇总 */
    private boolean           needSummary= false;
    /** 分页 */
    private QueryBase         queryBase;
    /** 订单号*/
    private String            sysTraceNo;
    
    public DealType getTxnType() {
        return txnType;
    }

    public void setTxnType(DealType txnType) {
        this.txnType = txnType;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getAccountType() {
        return accountType;
    }

    public void setAccountType(Integer accountType) {
        this.accountType = accountType;
    }

    public Date getLmlday() {
        return lmlday;
    }

    public void setLmlday(Date lmlday) {
        this.lmlday = lmlday;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public boolean isNeedSummary() {
        return needSummary;
    }

    public void setNeedSummary(boolean needSummary) {
        this.needSummary = needSummary;
    }

    public QueryBase getQueryBase() {
        return queryBase;
    }

    public void setQueryBase(QueryBase queryBase) {
        this.queryBase = queryBase;
    }
    
    public String getSysTraceNo() {
		return sysTraceNo;
	}

	public void setSysTraceNo(String sysTraceNo) {
		this.sysTraceNo = sysTraceNo;
	}

	@Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
