package com.netfinworks.site.ext.integration.util;

import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.tradeservice.facade.model.query.PageInfo;

public class QueryBaseForPage {
    
    /**
     * 转页面信息
     *
     * @param page
     * @return
     */
    public static QueryBase convertPage(PageInfo page) {
        QueryBase queryBase = new QueryBase();
        queryBase.setCurrentPage(page.getCurrentPage());
        queryBase.setPageSize(page.getPageSize());
        queryBase.setTotalItem(page.getTotalCount());
        return queryBase;
    }

}
