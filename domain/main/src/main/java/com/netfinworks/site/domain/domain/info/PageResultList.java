package com.netfinworks.site.domain.domain.info;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.biz.common.util.QueryBase;

/**
 *
 * <p>分页结果</p>
 * @author qinde
 * @version $Id: PageList.java, v 0.1 2013-12-5 上午11:08:13 qinde Exp $
 */
public class PageResultList<T> implements Serializable {
    /**
     *统计 总信息
     */
	private SummaryInfo summaryInfo;
    public SummaryInfo getSummaryInfo() {
		return summaryInfo;
	}

	public void setSummaryInfo(SummaryInfo summaryInfo) {
		this.summaryInfo = summaryInfo;
	}

	private static final long serialVersionUID = -7696614370950028797L;
    /** 页面信息*/
    private QueryBase         pageInfo;
    /**结果记录    */
    private List<T>           infos;

    public QueryBase getPageInfo() {
        return pageInfo;
    }

    public void setPageInfo(QueryBase pageInfo) {
        this.pageInfo = pageInfo;
    }

    public List<T> getInfos() {
        return infos;
    }

    public void setInfos(List<T> infos) {
        this.infos = infos;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
