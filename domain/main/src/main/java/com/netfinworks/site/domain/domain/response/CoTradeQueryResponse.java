package com.netfinworks.site.domain.domain.response;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.biz.common.util.BaseResult;
import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.site.domain.domain.info.BaseInfo;
import com.netfinworks.site.domain.domain.info.SummaryInfo;

/**
 * <p>结算流水明细</p>
 * @author zhangyun.m
 * @version $Id: SettlementInfo.java, v 0.1 2013-12-6 下午3:56:06 HP Exp $
 */
public class CoTradeQueryResponse extends BaseResult{

    private static final long serialVersionUID = 1L;
    
    /** 企业钱包结算汇总信息 */
    private SummaryInfo summaryInfo;
    
    /** 结算流水List */
    private List<BaseInfo> baseInfoList;
    
    /** 分页信息 */
    private QueryBase            queryBase;


    public QueryBase getQueryBase() {
        return queryBase;
    }

    public void setQueryBase(QueryBase queryBase) {
        this.queryBase = queryBase;
    }

    public SummaryInfo getSummaryInfo() {
        return summaryInfo;
    }

    public void setSummaryInfo(SummaryInfo summaryInfo) {
        this.summaryInfo = summaryInfo;
    }


    public List<BaseInfo> getBaseInfoList() {
        return baseInfoList;
    }

    public void setBaseInfoList(List<BaseInfo> baseInfoList) {
        this.baseInfoList = baseInfoList;
    }

    public CoTradeQueryResponse() {
        super();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
