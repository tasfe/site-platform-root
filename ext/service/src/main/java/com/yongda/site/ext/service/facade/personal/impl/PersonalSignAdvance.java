package com.yongda.site.ext.service.facade.personal.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domainservice.spi.DefaultShortcutSigService;
import com.yongda.site.ext.service.facade.personal.common.CommonValidator;
import com.yongda.site.ext.service.facade.personal.common.PersonalFindBindThirdAccount;
import com.yongda.site.service.personal.facade.request.PersonalSignAdvanceRequest;
import com.yongda.site.service.personal.facade.response.BaseResponse;
import com.yongda.supermag.core.common.utils.OperatEnvironment;
import com.yongda.supermag.facade.response.AgreementAdvanceResponse;


/**
 * 签约推进
 * @author admin
 *
 */
public class PersonalSignAdvance extends AbstractRoutService<PersonalSignAdvanceRequest, BaseResponse>{
	private static Logger logger = LoggerFactory.getLogger(PersonalAddBankCard.class);
	
	@Resource(name = "defaultShortcutSigService")
	private DefaultShortcutSigService defaultShortcutSigService;
	
	@Resource(name="personalFindBindThirdAccount")
	private PersonalFindBindThirdAccount personalFindBindThirdAccount;
	
	@Resource(name = "commonValidator")
	protected CommonValidator commonValidator;
	@Override
	public String getRoutName() {
		return "sign_advance";
	}

	@Override
	public PersonalSignAdvanceRequest buildRequest(Map<String, String> paramMap) {
		PersonalSignAdvanceRequest req = new PersonalSignAdvanceRequest();
		try {
			BeanUtils.populate(req, paramMap);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return req;
	}

	@Override
	public BaseResponse doProcess(PersonalSignAdvanceRequest req) {
		BaseResponse restP = new BaseResponse();
		AgreementAdvanceResponse response = null;
		/**验证memberId与plat_usr_id是否匹配**/
		try {
			// 校验提交参数
			String errorMsg = commonValidator.validate(req);
			if (StringUtils.isNotEmpty(errorMsg)) {
				restP.setErrorCode(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode());
				restP.setErrorMessage(errorMsg);
				restP.setSuccess(false);
				logger.error("缺少必要的查询参数！"+errorMsg);
				return restP;
			}
			/*if(!personalFindBindThirdAccount.match(req.getMemberId(),req.getPlatUserId(), req.getPartnerId())){
				throw new Exception("memberId与plat_usr_id不匹配");
			}*/
			
		} catch (Exception e1) {
			restP.setErrorMessage(e1.getMessage());
			restP.setSuccess(false);
			return restP;
		}
		
		try{
			OperatEnvironment env = new OperatEnvironment();
			response = defaultShortcutSigService.signAdvance(req, env);
			if(response.isSuccess()){
				restP.setData(response.getRequestId());
				restP.setSuccess(true);
				restP.setErrorMessage("签约推进成功");
			}
		} catch (ServiceException e) {
			restP.setErrorCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
			restP.setErrorMessage("签约推进失败！"+e.getMessage());
			restP.setSuccess(false);
			logger.warn("签约推进失败,token={}", req.getToken());
		}catch(Exception e){
			restP.setErrorCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
			restP.setErrorMessage("对不起，服务器繁忙，请稍后再试！");
			restP.setSuccess(false);
			logger.error("签约推进失败：" + e.getMessage());
		}
		return restP;
	}
}
