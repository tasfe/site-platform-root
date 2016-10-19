/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-domainservice-main
 * @date 2014年8月4日
 */
package com.netfinworks.site.domainservice.lflt.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.lflt.api.request.check.GetQuatoTimesRequest;
import com.netfinworks.lflt.api.request.check.VerifyRequest;
import com.netfinworks.lflt.api.response.AvailableResponse;
import com.netfinworks.lflt.api.response.CheckLfltResponse;
import com.netfinworks.lflt.api.service.ILfltFacadeWS;
import com.netfinworks.site.domainservice.lflt.LfltService;

/**
 * 限额限次服务的实现
 * @author xuwei
 * @date 2014年8月4日
 */
@Service("lfltService")
public class LfltServiceImpl implements LfltService {
	@Resource(name = "iLfltFacadeWS")
	private ILfltFacadeWS lfltFacade;

	@Override
	public CheckLfltResponse verifyLimit(VerifyRequest request,
			OperationEnvironment env) {
		return lfltFacade.verifyLimitForLimitedTimes(request, env);
	}

	@Override
	public AvailableResponse getAvailableLimit(GetQuatoTimesRequest request,
			OperationEnvironment env) {
		return lfltFacade.getLeftQuatoTimes(request, env);
	}
	
}
