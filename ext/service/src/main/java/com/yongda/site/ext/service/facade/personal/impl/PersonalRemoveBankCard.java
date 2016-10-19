package com.yongda.site.ext.service.facade.personal.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netfinworks.ma.service.base.model.BankAccountInfo;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
import com.yongda.site.ext.service.facade.personal.common.CommonValidator;
import com.yongda.site.ext.service.facade.personal.common.PersonalFindBindThirdAccount;
import com.yongda.site.service.personal.facade.request.PersonalRemveBankCardRequest;
import com.yongda.site.service.personal.facade.request.PersonalShortcutSigRequest;
import com.yongda.site.service.personal.facade.response.BaseResponse;

/**
 * 删除绑定的银行卡
 * @author admin
 *
 */
public class PersonalRemoveBankCard extends AbstractRoutService<PersonalRemveBankCardRequest, BaseResponse>{
	private static Logger logger = LoggerFactory
			.getLogger(PersonalRemoveBankCard.class);
	
	@Resource(name = "defaultBankAccountService")
	private DefaultBankAccountService defaultBankAccountService;
	
	@Resource(name = "commonValidator")
	protected CommonValidator commonValidator;
	
	@Resource(name="personalFindBindThirdAccount")
	private PersonalFindBindThirdAccount personalFindBindThirdAccount;
	
	@Override
	public String getRoutName() {
		return "removebank_card";
	}
	@Override
	public PersonalRemveBankCardRequest buildRequest(Map<String, String> paramMap) {
		PersonalRemveBankCardRequest req = new PersonalRemveBankCardRequest();
		req.setBankCardId(paramMap.get("bankCardId"));
		req.setMemberId(paramMap.get("memberId"));
		return req;
	}
	@Override
	public BaseResponse doProcess(PersonalRemveBankCardRequest req) {
		BaseResponse restP = new BaseResponse();
		/*if (MemberId.startsWith("2")) {
			if (user.getNameVerifyStatus() != AuthResultStatus.PASS) {
				restP.setMessage("请先进行实名认证！");
				return restP;
			}
		}*/
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
			
			if(!CheckBankCard(req)){
				restP.setErrorMessage("未查询到绑定的银行卡信息！请检查参数");
				restP.setErrorCode(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode());
				restP.setSuccess(false);
				return restP;
			}
			BankAccRequest request = new BankAccRequest();
			request.setBankcardId(req.getBankCardId());
			request.setMemberId(req.getMemberId());
			defaultBankAccountService.removeBankAccount(request);
			restP.setSuccess(true);
			restP.setErrorMessage("解除会员银行卡绑定成功!");
		} catch (ServiceException e) {
			restP.setErrorCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
			restP.setErrorMessage("解除会员银行卡绑定失败,"+e.getMessage());
			restP.setSuccess(false);
			logger.error("解除会员银行卡绑定，调用接口异常！");
		}catch(Exception e){
			restP.setErrorCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
			restP.setErrorMessage("对不起，服务器繁忙，请稍后再试！");
			restP.setSuccess(false);
			logger.error("解除会员银行卡绑定失败：" + e.getMessage());
		}
		return restP;
	}
	
	public Boolean CheckBankCard(PersonalRemveBankCardRequest req){
		try {
			Boolean flag = false;
			BankAccRequest accReq = new BankAccRequest();
			accReq.setMemberId(req.getMemberId());
			List<BankAccountInfo> list = defaultBankAccountService.queryBankAccount(accReq);
			if ((list != null) && (list.size() > 0)) {
				for(BankAccountInfo info : list){
					if(info.getBankcardId().equals(req.getBankCardId())){
						flag = true;
						break;
					}
				}
			}
			return flag;
		}catch (Exception e) {
			logger.error("查询失败：" + e.getMessage());
		}
		return null;
	}
}
