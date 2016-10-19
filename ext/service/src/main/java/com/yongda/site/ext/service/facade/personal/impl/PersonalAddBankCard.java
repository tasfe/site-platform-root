package com.yongda.site.ext.service.facade.personal.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netfinworks.cert.service.model.enums.ResultStatus;
import com.netfinworks.ma.service.base.model.BankAccountInfo;
import com.netfinworks.ma.service.base.model.BankAcctDetailInfo;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;
import com.netfinworks.site.domain.domain.bank.CardBin;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domain.enums.CardType;
import com.netfinworks.site.domain.enums.Dbcr;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domainservice.pfs.DefaultPfsBaseService;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.ext.service.facade.converter.HttpRequestConvert;
import com.yongda.site.ext.service.facade.personal.common.CommonValidator;
import com.yongda.site.ext.service.facade.personal.common.PersonalCommonQueryRealName;
import com.yongda.site.ext.service.facade.personal.common.PersonalFindBindThirdAccount;
import com.yongda.site.service.personal.facade.request.PersonalAddBankCardRequest;
import com.yongda.site.service.personal.facade.response.BaseResponse;

/**
 * 添加银行卡（绑定）
 * @author admin
 *
 */
public class PersonalAddBankCard extends AbstractRoutService<PersonalAddBankCardRequest, BaseResponse>{
	
	private static final String MEMBERID_HEADVALUE = "2";
	private static final String CARD_ATTRI_BUTE = "1";
	private static Logger logger = LoggerFactory.getLogger(PersonalAddBankCard.class);
	
	@Resource(name = "commonValidator")
	protected CommonValidator commonValidator;
	
	@Resource(name = "defaultBankAccountService")
	private DefaultBankAccountService defaultBankAccountService;
	
	@Resource(name = "defaultPfsBaseService")
	private DefaultPfsBaseService defaultPfsBaseService;
	
	@Resource(name = "defaultCertService")
	private DefaultCertService defaultCertService;
	
	@Resource(name="personalFindBindThirdAccount")
	private PersonalFindBindThirdAccount personalFindBindThirdAccount;
	
	@Override
	public String getRoutName() {
		return "add_bankcard";
	}

	@Override
	public PersonalAddBankCardRequest buildRequest(Map<String, String> paramMap) {
		logger.info("添加银行卡，请求参数：{}", paramMap);
		PersonalAddBankCardRequest req = HttpRequestConvert.convert2Request(paramMap, PersonalAddBankCardRequest.class);
		return req;
	}

	@Override
	public BaseResponse doProcess(PersonalAddBankCardRequest req) {
		logger.info("添加银行卡，请求参数：{}", req);
		BaseResponse restP = new BaseResponse();
		req.setCardAttribute(CARD_ATTRI_BUTE);
		try{
			// 校验提交参数
			String errorMsg = commonValidator.validate(req);
			
			if (StringUtils.isNotEmpty(errorMsg)) {
				restP.setErrorMessage(errorMsg);
				restP.setErrorCode(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode());
				logger.error("缺少必要的查询参数！"+errorMsg);
				return restP;
			}
			/**
			 * 验证实名认证
			 */
			/*if (req.getMemberId().startsWith(MEMBERID_HEADVALUE)) {
				if (req.getNameVerifyStatus() != AuthResultStatus.PASS) {
					restP.setErrorMessage("实名认证后才可以添加银行卡！");
					restP.setSuccess(false);
					return restP;
				}
			}*/
			
			//根据认证类型查询认证结果
			String identitySts = PersonalCommonQueryRealName.queryRealName(req.getMemberId(),
					StringUtils.EMPTY, AuthType.IDENTITY,defaultCertService);
			
			if (StringUtils.isBlank(identitySts) || (!ResultStatus.CHECK_PASS.getCode().equals(identitySts) && !ResultStatus.AUDIT_PASS.getCode().equals(identitySts))) {
				restP.setErrorMessage("实名认证后才可以添加银行卡！");
				restP.setErrorCode(CommonDefinedException.NO_VERIFY_ERROR.getErrorCode());
				restP.setSuccess(false);
				return restP;
			}
			
			/**
			 * 验证 绑卡上限
			 */
			BankAccRequest tieoncardCountRequest = new BankAccRequest();
			tieoncardCountRequest.setMemberId(req.getMemberId());
			tieoncardCountRequest.setClientIp(req.getRemoteAddr());
			List<BankAccountInfo> list = defaultBankAccountService.queryBankAccount(tieoncardCountRequest);
			if (!(list.size() < 20)) {
				logger.error("最多添加20张银行卡！");
				restP.setErrorMessage("最多添加20张银行卡！");
				restP.setErrorCode(CommonDefinedException.ACCOUNT_COUNT_ERROR.getErrorCode());
				restP.setSuccess(false);
				return restP;
			}
		}catch(Exception e){
			logger.warn("添加银行卡失败,卡号={}", req.getBankAccountNum());
		}
		
		return addBankCard(req,restP);
	}
	
	public BaseResponse addBankCard(PersonalAddBankCardRequest req,BaseResponse restP){
		try {
					// 查询绑定银行卡
					BankAccRequest accReq = new BankAccRequest();
					accReq.setMemberId(req.getMemberId());
					accReq.setClientIp(req.getRemoteAddr());
					accReq.setBankAccountNum(req.getBankAccountNum());
					List<BankAccountInfo> list = defaultBankAccountService.queryBankAccount(accReq);
					if ((list != null) && (list.size() > 0)) {
						restP.setErrorMessage("对不起，此银行卡已绑定，请重新选择另一张!");
						restP.setSuccess(false);
						restP.setErrorCode(CommonDefinedException.ACCOUNT_BINDING_REPEAT_ERROR.getErrorCode());
						return restP;
					}
					CardBin cardBin = null;
					try {
						cardBin = defaultPfsBaseService.queryByCardNo(req.getBankAccountNum(),
								CommonConstant.PERSONAL_APP_ID);
						if ((cardBin != null) && !Dbcr.DC.getCode().equals(cardBin.getCardType())) {
							restP.setErrorMessage("添加银行卡不能是信用卡!");
							restP.setErrorCode(CommonDefinedException.CARD_TYPE_NOT_SUPPORTED.getErrorCode());
							return restP;
						}
						if(!cardBin.getBankCode().equalsIgnoreCase(req.getBankCode())){
							restP.setSuccess(false);
							restP.setErrorMessage("卡bin校验失败!请检测输入参数");
							restP.setErrorCode(CommonDefinedException.CARD_BIN_ERROR.getErrorCode());
							return restP;
						}
					} catch (Exception e) {
						logger.warn("未查询到卡bin信息，卡号={}", req.getBankAccountNum());
						if (!defaultPfsBaseService.cardValidate(CommonConstant.PERSONAL_APP_ID, req.getBankCode(),
								req.getBankAccountNum())) {
							restP.setErrorCode(CommonDefinedException.CARD_BIN_ERROR.getErrorCode());
							restP.setErrorMessage("绑定银行卡卡号不正确!");
							return restP;
						}
						restP.setErrorMessage("无匹配卡bin信息!");
						restP.setErrorCode(CommonDefinedException.CARD_BIN_ERROR.getErrorCode());
						restP.setSuccess(false);
						return restP;
					}

					BankAccRequest backaccreq = new BankAccRequest();
					backaccreq.setMemberId(req.getMemberId());
					backaccreq.setBankName(cardBin.getBankName());
					backaccreq.setBankCode(req.getBankCode());
					backaccreq.setBranchName(req.getBranchName());
					backaccreq.setBranchShortName(req.getBranchShortName());
					backaccreq.setBranchNo(req.getBranchNo());
					backaccreq.setProvName(req.getProvince());
					backaccreq.setCityName(req.getCity());
					backaccreq.setCardType(Integer.valueOf(CardType.JJK.getCode()));
					backaccreq.setCardAttribute(Integer.valueOf(req.getCardAttribute()));
					backaccreq.setPayAttribute("qpay");
					backaccreq.setRealName(req.getRealName());
					backaccreq.setBankAccountNum(req.getBankAccountNum());
					backaccreq.setMobile(req.getMobile());
					String bankcardId = defaultBankAccountService.addBankAccount(backaccreq);//返回銀行卡主鍵
					BankAcctDetailInfo detailinfo = defaultBankAccountService.queryBankAccountDetail(bankcardId);
					//user.setBankCardCount(user.getBankCardCount() + 1);
					restP.setData(detailinfo);
					//restP.setData(detailinfo);
					restP.setSuccess(true);
					restP.setErrorMessage("绑定银行卡成功!");
				} catch (Exception e) {
					restP.setErrorMessage(e.getMessage());
					restP.setSuccess(false);
					restP.setErrorCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
					return restP;
				}
		return restP;
	}
}
