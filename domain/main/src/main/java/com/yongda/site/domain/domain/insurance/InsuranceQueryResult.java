package com.yongda.site.domain.domain.insurance;

import java.io.Serializable;
import java.util.List;

import com.netfinworks.biz.common.util.QueryBase;

public class InsuranceQueryResult implements Serializable {

	/** 描述 */
	private static final long serialVersionUID = -3735289783651627767L;

	/**分页*/
    private QueryBase queryBase;
	
    List<InsuranceOrder> orders;

	public QueryBase getQueryBase() {
		return queryBase;
	}

	public void setQueryBase(QueryBase queryBase) {
		this.queryBase = queryBase;
	}

	public List<InsuranceOrder> getOrders() {
		return orders;
	}

	public void setOrders(List<InsuranceOrder> orders) {
		this.orders = orders;
	}
}
