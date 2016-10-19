package com.netfinworks.site.service.facade.response;

import java.util.List;

import javax.xml.bind.annotation.XmlType;

import com.netfinworks.biz.common.util.BaseResult;
import com.netfinworks.site.service.facade.model.AutoFundout;

/**
 * <p>出款信息查询响应</p>
 * @author zhangjq
 * @version 
 */
@XmlType(namespace="http://response.site.netfinworks.com")
public class AutoFundoutResponse extends BaseResult{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8921728281543013666L;
	
	

	private List<AutoFundout> autoFundOutlist;

	public List<AutoFundout> getAutoFundOutlist() {
		return autoFundOutlist;
	}

	public void setAutoFundOutlist(List<AutoFundout> autoFundOutlist) {
		this.autoFundOutlist = autoFundOutlist;
	} 
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
