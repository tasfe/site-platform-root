package com.yongda.site.ext.service.facade.personal.impl;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netfinworks.cert.service.model.enums.ResultStatus;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.domain.response.VerifyAmountResponse;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.yongda.site.ext.service.facade.personal.common.CommonValidator;
import com.yongda.site.ext.service.facade.personal.common.PersonalCommonQueryRealName;
import com.yongda.site.ext.service.facade.personal.common.PersonalFindBindThirdAccount;
import com.yongda.site.service.personal.facade.request.PersonalVerifyAmountRequest;
import com.yongda.site.service.personal.facade.response.PersonalVerifyAmountFacadeResponse;
import com.yongda.site.service.personal.facade.response.BaseResponse;
/**
 * 金额认证 接口
 * @author admin
 *
 */
public class PersonalVerifyAmount  extends AbstractRoutService<PersonalVerifyAmountRequest, BaseResponse>{
	
	@Resource(name = "defaultCertService")
	private DefaultCertService defaultCertService;
	
	@Resource(name = "commonValidator")
	protected CommonValidator commonValidator;
	
	@Resource(name="personalFindBindThirdAccount")
	private PersonalFindBindThirdAccount personalFindBindThirdAccount;
	
	private static Logger logger = LoggerFactory
			.getLogger(PersonalVerifyAmount.class);
	
	@Override
	public String getRoutName() {
		return "verify_amount";
	}

	@Override
	public PersonalVerifyAmountRequest buildRequest(Map<String, String> paramMap) {
		PersonalVerifyAmountRequest req = new PersonalVerifyAmountRequest();
		req.setAmount(paramMap.get("amount"));
		req.setMemberId(paramMap.get("memberId"));
		req.setOperatorId(StringUtils.EMPTY);
		return req;
	}

	@Override
	public BaseResponse doProcess(PersonalVerifyAmountRequest req) {
		BaseResponse response = new BaseResponse();// 响应请求
		
		try {
			// 校验提交参数
			String errorMsg = commonValidator.validate(req);
			if (StringUtils.isNotEmpty(errorMsg)) {
				response.setErrorCode(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode());
				response.setErrorMessage(errorMsg);
				response.setSuccess(false);
				logger.error("缺少必要的查询参数！"+errorMsg);
				return response;
			}
			
			String bankCardSts = PersonalCommonQueryRealName.queryRealName(req.getMemberId(), null, AuthType.BANK_CARD,defaultCertService);
	
			if (ResultStatus.CHECK_PASS.getCode().equals(bankCardSts)) {
				response.setErrorMessage("您的银行卡认证已经通过！");
				response.setSuccess(false);
				response.setErrorCode(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode());
				return response;
			}
			
			VerifyAmountResponse rsp = defaultCertService.verifyAmount(req.getMemberId(), req.getOperatorId(),
					req.getAmount());
	
			if (!rsp.isSuccess()) {
				response.setErrorCode(CommonDefinedException.TRADE_AMOUNT_MATCH_ERROR.getErrorCode());
				response.setErrorMessage("金额认证失败");
				response.setSuccess(false);
				response.setData(rsp.getRemainCount());
			} else {
				response.setErrorMessage("金额认证成功");
				response.setSuccess(true);
			}
		} catch (ServiceException e) {
			logger.error("个人打款金额验证：" + e.getMessage());
			response.setErrorCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
			response.setSuccess(false);
			response.setErrorMessage(e.getMessage());
		} catch (Exception e) {
			logger.error("个人打款金额验证：" + e.getMessage());
			response.setErrorCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
			response.setSuccess(false);
			response.setErrorMessage("对不起，服务器繁忙，请稍后再试！");
		}
		return response;
	}
}
