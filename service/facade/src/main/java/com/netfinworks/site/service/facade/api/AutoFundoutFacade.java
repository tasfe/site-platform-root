package com.netfinworks.site.service.facade.api;

import javax.jws.WebService;

import com.netfinworks.site.service.facade.response.AutoFundoutResponse;

/**
 * <p>出款信息查询接口</p>
 * @author zhangjq
 * @version 
 */
@WebService(targetNamespace = "http://facade.site.netfinworks.com")
public interface AutoFundoutFacade {

	/** 
	 * 根据MemberId查询出款信息
     * @param memberId
     * @return
     */
	public AutoFundoutResponse queryByMemberId(String memberId);
	
}
