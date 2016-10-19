package com.netfinworks.site.ext.integration.member;

import com.netfinworks.cert.service.request.CertValidateRequest;
import com.netfinworks.cert.service.response.CertValidateResponse;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.domain.exception.BizException;
/** 
 * <p>国政通实名认证</p>
 * 
 * @author tangL
 * @date 2014年12月26日
 * @since 1.6
 */ 

public interface ICertValidateService {
	/**
	 * 调用远程验证公共方法
	 * @param environment
	 * @param certValidateRequest
	 * @return CertValidateResponse
	 * @throws BizException
	 */
	public CertValidateResponse validate(OperationEnvironment environment, CertValidateRequest certValidateRequest) throws BizException;
	
}
