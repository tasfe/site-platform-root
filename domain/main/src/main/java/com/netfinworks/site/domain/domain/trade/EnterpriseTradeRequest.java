package com.netfinworks.site.domain.domain.trade;

import java.io.Serializable;
import java.util.Date;

import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.common.domain.OperationEnvironment;

public class EnterpriseTradeRequest extends OperationEnvironment  implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 卖家Id */
    private String            memberId;
    /** 查询开始时间 */
    private Date              beginTime;
    /** 查询结束时间 */
    private Date              endTime;
    /** 交易状态 */
    private String            status;
    /** 分页 */
    private QueryBase         queryBase;
    /** 是否汇总 */
    private boolean   needSummary = false;

    public String getMemberId() {
        return memberId;
    }

    public QueryBase getQueryBase() {
        return queryBase;
    }

    public void setQueryBase(QueryBase queryBase) {
        this.queryBase = queryBase;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isNeedSummary() {
        return needSummary;
    }

    public void setNeedSummary(boolean needSummary) {
        this.needSummary = needSummary;
    }
    


}
