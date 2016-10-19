package com.netfinworks.site.domain.domain.trade;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.common.domain.OperationEnvironment;


/**
 * <p>企业查询所有交易记录请求</p>
 * @author zhangyun.m
 * @version $Id: BusinessRequest.java, v 0.1 2013-12-4 上午9:51:38 HP Exp $
 */
public class BusinessRequest extends OperationEnvironment implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 企业Id */
    private String            memberId;
    
    /** 查询开始时间 */
    private Date              beginTime;

    /** 查询结束时间 */
    private Date              endTime;
    /** 分页 */
    private QueryBase         queryBase;

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

    public QueryBase getQueryBase() {
        return queryBase;
    }

    public void setQueryBase(QueryBase queryBase) {
        this.queryBase = queryBase;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
