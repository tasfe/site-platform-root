package com.yongda.site.service.personal.facade.request;

import com.yongda.site.core.common.annotation.HttpField;

public class QueryRechargeCashierRequest extends BaseRequest{
	
	/** 描述 */
	private static final long serialVersionUID = -3966218290675591027L;
	
	@HttpField(name="return_url")
	private String returnUrl;
	
	@HttpField(name="channal")
	private String channalType;

	public String getReturnUrl() {
		return returnUrl;
	}

	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}

	public String getChannalType() {
		return channalType;
	}

	public void setChannalType(String channalType) {
		this.channalType = channalType;
	}
}
