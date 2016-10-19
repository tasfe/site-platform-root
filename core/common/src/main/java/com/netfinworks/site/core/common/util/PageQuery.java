package com.netfinworks.site.core.common.util;

import com.netfinworks.biz.common.util.QueryBase;

public class PageQuery extends QueryBase{

	private static final long serialVersionUID = -934013157586417275L;

	@Override
	public boolean isAllowedQuery() {
		return true;
	}

	@Override
	public <K extends QueryBase> void copyProperties(K k) {
		super.copyProperties(k);
		k.setPageSize(this.getPageSize());
	}
	
	public <K extends QueryBase> void copyPropertiesFrom(K k) {
		if (k == null){
            return;
        }else {
        	this.setPageSize(k.getPageSize());
            this.setCurrentPage(k.getCurrentPage());
            this.setEndRow(k.getEndRow());
            this.setNeedDelete(k.isNeedDelete());
            this.setNeedQeryTotal(k.isNeedQeryTotal());
            this.setNeedQueryAll(k.isNeedQueryAll());
            this.setStartRow(k.getStartRow());
            this.setTotalItem(k.getTotalItem());
            if (k.getTotalSum() != null){
            	this.setTotalSum(k.getTotalSum().getCent());
            }
        }
	}
}
