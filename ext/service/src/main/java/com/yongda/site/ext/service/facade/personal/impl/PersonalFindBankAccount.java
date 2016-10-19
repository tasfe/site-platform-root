package com.yongda.site.ext.service.facade.personal.impl;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
import com.yongda.site.ext.service.facade.personal.common.PersonalFindBindThirdAccount;
import com.yongda.site.service.personal.facade.request.BaseRequest;
import com.yongda.site.service.personal.facade.response.PersonalBankAccountResponse;
import com.yongda.site.service.personal.facade.response.BaseResponse;

/**
 * 查询绑定银行卡信息
 * @author admin
 *
 */
public class PersonalFindBankAccount extends AbstractRoutService{
	 
	private static Logger logger = LoggerFactory.getLogger(PersonalFindBankAccount.class);
	
	@Resource(name = "defaultBankAccountService")
	private DefaultBankAccountService defaultBankAccountService;

	@Resource(name="personalFindBindThirdAccount")
	private PersonalFindBindThirdAccount personalFindBindThirdAccount;
	
	@Override
	public String getRoutName() {
		return "bankcard_rout";
	}

	@Override
	public BaseRequest buildRequest(Map paramMap) {
		BaseRequest req = new BaseRequest();
		req.setMemberId((String)paramMap.get("memberId"));
		req.setVersion((String)paramMap.get("version"));
		return req;
	}

	@Override
	public BaseResponse doProcess(BaseRequest reqParam) {
		BaseResponse response = new BaseResponse();
		if(!cheackRequestParams(reqParam.getMemberId(),reqParam.getVersion(),response)){
			logger.error(response.getErrorMessage());
			return response;
		}
		try {
			BankAccRequest req = new BankAccRequest();
			req.setMemberId(reqParam.getMemberId());
			response.setData(defaultBankAccountService.queryBankAccount(req));
			response.setSuccess(true);
		} catch (ServiceException e) {
			response.setSuccess(false);
			response.setErrorMessage("查询绑定银行卡失败！"+e.getMessage());
			response.setErrorCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
			logger.warn("查询绑定银行卡失败,会员号={}", reqParam.getMemberId());
		}catch(Exception e){
			logger.error("查询绑定银行卡失败：" + e.getMessage());
			response.setErrorCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
			response.setErrorMessage("对不起，服务器繁忙，请稍后再试！");
			response.setSuccess(false);
		}
		return response;
	}
	
	public Boolean cheackRequestParams(String memberId,String version,
			BaseResponse response){
		if (StringUtils.isEmpty(memberId)) {
			response.setErrorCode(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode());
			response.setErrorMessage("会员id不能为空!");
			response.setSuccess(false);
			return false;
		}
		if (StringUtils.isEmpty(version)) {
			response.setErrorCode(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode());
			response.setErrorMessage("version不能为空!");
			response.setSuccess(false);
			return false;
		}
		return true;
	}
}
