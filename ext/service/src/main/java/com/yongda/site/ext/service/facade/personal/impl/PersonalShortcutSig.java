package com.yongda.site.ext.service.facade.personal.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kjt.unionma.core.common.utils.RadomUtil;
import com.netfinworks.ma.service.base.model.BankAccountInfo;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;
import com.netfinworks.site.domain.domain.bank.CardBin;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domainservice.pfs.DefaultPfsBaseService;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
import com.netfinworks.site.domainservice.spi.DefaultShortcutSigService;
import com.netfinworks.site.ext.service.facade.converter.HttpRequestConvert;
import com.yongda.site.ext.service.facade.personal.common.CommonValidator;
import com.yongda.site.ext.service.facade.personal.common.PersonalFindBindThirdAccount;
import com.yongda.site.service.personal.facade.request.PersonalBankCardRequest;
import com.yongda.site.service.personal.facade.request.PersonalShortcutSigRequest;
import com.yongda.site.service.personal.facade.response.BaseResponse;
import com.yongda.supermag.core.common.utils.OperatEnvironment;
import com.yongda.supermag.facade.response.AgreementResponse;

/*
 *签约银行卡接口 
 */
public class PersonalShortcutSig extends AbstractRoutService<PersonalShortcutSigRequest, BaseResponse>{
	
	private static Logger logger = LoggerFactory.getLogger(PersonalAddBankCard.class);
	private static final String MY_PARTNERID = "ydPartnerId";
	@Resource(name = "defaultShortcutSigService")
	private DefaultShortcutSigService defaultShortcutSigService;
	
	@Resource(name = "defaultPfsBaseService")
	private DefaultPfsBaseService defaultPfsBaseService;
	
	@Resource(name = "defaultBankAccountService")
	private DefaultBankAccountService defaultBankAccountService;
	
	@Resource(name = "commonValidator")
	protected CommonValidator commonValidator;
	
	@Resource(name="personalFindBindThirdAccount")
	private PersonalFindBindThirdAccount personalFindBindThirdAccount;
	
	@Override
	public String getRoutName() {
		return "shortcut_sig";
	}

	@Override
	public PersonalShortcutSigRequest buildRequest(
			Map<String, String> paramMap) {
		PersonalShortcutSigRequest req = HttpRequestConvert.convert2Request(paramMap, PersonalShortcutSigRequest.class);
		return req;
	}

	@Override
	public BaseResponse doProcess(PersonalShortcutSigRequest req) {
		req.setRequestId(RadomUtil.getDatetimeId());
		BaseResponse restP = new BaseResponse();
		AgreementResponse response = null;
		try{
			// 校验提交参数
			String errorMsg = commonValidator.validate(req);
			if (StringUtils.isNotEmpty(errorMsg)) {
				restP.setErrorCode(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode());
				restP.setErrorMessage(errorMsg);
				restP.setSuccess(false);
				logger.error("缺少必要的查询参数！"+errorMsg);
				return restP;
			}
			
			try {
				req.setPartnerId(MY_PARTNERID);
				CardBin cardBin = defaultPfsBaseService.queryByCardNo(req.getCardNo(),
						CommonConstant.PERSONAL_APP_ID);
				if (cardBin == null) {
					restP.setErrorMessage("没有查询到该银行卡信息!");
					return restP;
				}
			} catch (Exception e) {
				logger.warn("未查询到卡bin信息，卡号={}", req.getCardNo());
				if (!defaultPfsBaseService.cardValidate(CommonConstant.PERSONAL_APP_ID, req.getTargetInst(),
						req.getCardNo())) {
					restP.setErrorCode(CommonDefinedException.CARD_BIN_ERROR.getErrorCode());
					restP.setErrorMessage("签约银行卡卡号不正确!");
					restP.setSuccess(false);
					return restP;
				}
				restP.setErrorCode(CommonDefinedException.CARD_BIN_ERROR.getErrorCode());
				restP.setErrorMessage("卡bin校验失败!");
				restP.setSuccess(false);
				return restP;
			}
			//检查该银行卡是否已绑定
			if(!CheckBankCard(req)){
				restP.setErrorCode(CommonDefinedException.ACCOUNT_BINDING_REPEAT_ERROR.getErrorCode());
				restP.setErrorMessage("对不起，此银行卡已绑定，请重新选择另一张银行卡进行签约!");
				restP.setSuccess(false);
				return restP;
			}
			OperatEnvironment env = new OperatEnvironment();
			response = defaultShortcutSigService.ShortcutSig(req, env);
			if(response.isSuccess()){
				restP.setData(response.getToken());
				restP.setSuccess(true);
				restP.setErrorMessage("签约成功！");
			}
		} catch (ServiceException e) {
			restP.setErrorCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
			restP.setErrorMessage("签约失败！"+e.getMessage());
			restP.setSuccess(false);
			logger.warn("快捷签约失败,卡号={}", req.getCardNo());
		}catch(Exception e){
			restP.setErrorCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
			restP.setErrorMessage("对不起，服务器繁忙，请稍后再试！");
			restP.setSuccess(false);
			logger.error("快捷签约失败：" + e.getMessage());
		}
		return restP;
	}

	public Boolean CheckBankCard(PersonalShortcutSigRequest req){
		try {
			BankAccRequest accReq = new BankAccRequest();
			accReq.setMemberId(req.getMemberId());
			accReq.setClientIp(StringUtils.EMPTY);
			accReq.setBankAccountNum(req.getCardNo());
			List<BankAccountInfo> list = defaultBankAccountService.queryBankAccount(accReq);
			if ((list != null) && (list.size() > 0)) {
				return false;
			}
		}catch (Exception e) {
			logger.error("查询失败：" + e.getMessage());
		}
		return true;
	}
}
