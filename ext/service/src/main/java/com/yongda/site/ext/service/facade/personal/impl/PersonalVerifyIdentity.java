package com.yongda.site.ext.service.facade.personal.impl;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netfinworks.cert.service.model.enums.ResultStatus;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domainservice.pfs.DefaultPfsBaseService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.ext.service.facade.converter.HttpRequestConvert;
import com.yongda.site.ext.service.facade.personal.common.CommonValidator;
import com.yongda.site.ext.service.facade.personal.common.PersonalCommonQueryRealName;
import com.yongda.site.ext.service.facade.personal.common.PersonalFindBindThirdAccount;
import com.yongda.site.service.personal.facade.request.PersonalIdentityRequest;
import com.yongda.site.service.personal.facade.response.BaseResponse;
/**
 * 身份认证
 */
public class PersonalVerifyIdentity extends AbstractRoutService<PersonalIdentityRequest, BaseResponse>{
	private static Logger logger = LoggerFactory
			.getLogger(PersonalVerifyIdentity.class);
	
	@Resource(name = "defaultCertService")
	private DefaultCertService defaultCertService;
	
	@Resource(name = "defaultPfsBaseService")
	private DefaultPfsBaseService defaultPfsBaseService;
	
	@Resource(name = "commonValidator")
	protected CommonValidator commonValidator;
	
	@Resource(name="personalFindBindThirdAccount")
	private PersonalFindBindThirdAccount personalFindBindThirdAccount;
	
	@Override
	public String getRoutName() {
		return "verify_identity";
	}
	
	@Override
	public PersonalIdentityRequest buildRequest(Map<String, String> paramMap) {
		PersonalIdentityRequest req = HttpRequestConvert.convert2Request(paramMap, PersonalIdentityRequest.class);
		return req;
	}
	
	@Override
	public BaseResponse doProcess(PersonalIdentityRequest req) {
		logger.info("身份认证，请求参数：{}", req);
		BaseResponse response = new BaseResponse();// 响应请求
		OperationEnvironment env = new OperationEnvironment();
		if (StringUtils.isBlank(req.getIdcard())) {
			response.setErrorCode(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode());
			response.setErrorMessage("证件号码不能为空");
			response.setSuccess(false);
			logger.info("缺少必要的查询参数！"+"证件号码不能为空");
			return response;
		}
		req.setIdcard(req.getIdcard().toUpperCase());//对18位的身份证号码进行处理最后一位大写进行匹配
		try {
			// 校验提交参数
			String errorMsg = commonValidator.validate(req);
			
			if (StringUtils.isNotEmpty(errorMsg)) {
				response.setErrorCode(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode());
				response.setErrorMessage(errorMsg);
				response.setSuccess(false);
				logger.info("缺少必要的查询参数！"+errorMsg);
				return response;
			}
			
			logger.info("根据认证类型查询认证结果");
			//根据认证类型查询认证结果
			String identitySts = PersonalCommonQueryRealName.queryRealName(req.getMemberId(),
					req.getOperatorId(), AuthType.IDENTITY,defaultCertService);
			
			if (ResultStatus.CHECK_PASS.getCode().equals(identitySts)) {
				response.setErrorMessage("您的身份认证已经审核通过！");
				response.setSuccess(true);
				return response;
			}
			logger.info("未审核通过  提交身份认证申请：");
			//未审核通过  提交身份认证申请  
			Boolean status = defaultCertService.verifyRealName(
					req.getMemberId(), req.getOperatorId(),
					StringUtils.trim(req.getRealname()),
					StringUtils.trim(req.getIdcard()), req.getCardtype(), env);
			response.setSuccess(status);
			response.setErrorMessage("身份认证成功！");
		} catch (ServiceException e) {
			logger.error("个人身份认证：" + e.getMessage());
			response.setErrorCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
			response.setErrorMessage(e.getMessage());
			response.setSuccess(false);
		} catch (Exception e) {
			logger.error("个人身份认证：" + e.getMessage());
			response.setErrorCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
			response.setErrorMessage("对不起，服务器繁忙，请稍后再试！");
			response.setSuccess(false);
		}
		return response;
	}

}
