package com.netfinworks.site.ext.integration.member.impl;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.cert.service.facade.ICertValidateFacade;
import com.netfinworks.cert.service.model.CertValidateInfo;
import com.netfinworks.cert.service.model.enums.ValidateResult;
import com.netfinworks.cert.service.request.CertValidateRequest;
import com.netfinworks.cert.service.response.CertValidateResponse;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.common.util.SampleExceptionUtils;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.member.ICertValidateService;
/** 
 * <p>国政通实名认证</p>
 * 
 * @author tangL
 * @date 2014年12月26日
 * @since 1.6
 */ 
@Service("iCertValidateService")
public class ICertValidateServiceImpl implements ICertValidateService {

	@SuppressWarnings("unused")
	private Logger logger = LoggerFactory.getLogger(ICertValidateServiceImpl.class);
	
	@Resource(name = "icertvalidateFacade")
	private ICertValidateFacade icertvalidateFacade;

	@Override
	public CertValidateResponse validate(OperationEnvironment environment, CertValidateRequest request) throws BizException{		
		CertValidateResponse response = icertvalidateFacade.validate(environment, request);
		CertValidateInfo validateInfo = response.getCertValidateInfo();
		
		// 当返回信息为空时
		if (validateInfo == null)
			throw new BizException(ErrorCode.SYSTEM_ERROR, SampleExceptionUtils.addTip("无法获取用户信息"));
		
		ValidateResult validateResult = validateInfo.getValidateResult();		
		boolean isSuccess = validateResult.SUCCESS.equals(validateResult);
		if (!isSuccess) {
			// 只需验证数据不一致情况 
			if(ValidateResult.FAIL.equals(validateResult)) {
				throw new BizException(ErrorCode.SYSTEM_ERROR, SampleExceptionUtils.addTip("用户名与身份证不一致"));
			}
			// 获取错误信息
			ValidateResult r = ValidateResult.getByCode(validateResult.getCode());
			String errorMsg = (r == null ? "" : r.getMessage());
			
			throw new BizException(ErrorCode.SYSTEM_ERROR, "实名认证失败, 错误信息: " + errorMsg);
		}
		return response;
	}

}
