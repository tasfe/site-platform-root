package com.yongda.site.ext.service.facade.personal.impl;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netfinworks.cert.service.model.enums.ResultStatus;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;
import com.netfinworks.site.domain.domain.bank.CardBin;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domain.enums.CertifyLevel;
import com.netfinworks.site.domain.enums.Dbcr;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domainservice.pfs.DefaultPfsBaseService;
import com.netfinworks.site.domainservice.spi.DefaultCertService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.ext.service.facade.converter.HttpRequestConvert;
import com.yongda.site.ext.service.facade.personal.common.CommonValidator;
import com.yongda.site.ext.service.facade.personal.common.PersonalCommonQueryRealName;
import com.yongda.site.ext.service.facade.personal.common.PersonalFindBindThirdAccount;
import com.yongda.site.service.personal.facade.request.PersonalBankCardRequest;
import com.yongda.site.service.personal.facade.response.BaseResponse;

/**
 * 银行卡认证
 * @author admin
 *
 */
public class PersonalIdentificationBankCard extends AbstractRoutService<PersonalBankCardRequest, BaseResponse>{
	private static Logger logger = LoggerFactory
			.getLogger(PersonalIdentificationBankCard.class);

	@Resource(name = "defaultCertService")
	private DefaultCertService defaultCertService;
	
	@Resource(name = "defaultPfsBaseService")
	private DefaultPfsBaseService defaultPfsBaseService;
	
	@Resource(name="personalFindBindThirdAccount")
	private PersonalFindBindThirdAccount personalFindBindThirdAccount;
	
	//@Autowired
	@Resource(name = "commonValidator")
	protected CommonValidator commonValidator;
	
	@Resource(name = "defaultMemberService")
	private DefaultMemberService defaultMemberService;
	
	@Override
	public String getRoutName() {
		return "verify_bankcard";
	}
	
	@Override
	public PersonalBankCardRequest buildRequest(Map<String, String> paramMap) {
		PersonalBankCardRequest req = HttpRequestConvert.convert2Request(paramMap, PersonalBankCardRequest.class);
		return req;
	}
	
	@Override
	public BaseResponse doProcess(PersonalBankCardRequest req) {
		OperationEnvironment env = new OperationEnvironment();
		logger.info("银行卡验证，请求参数：{}", req);
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
			
			String level = defaultMemberService.getMemberVerifyLevel(req.getMemberId(), env);
			//银行卡认证接口要判断是否是V0用户，不是V0用户要先身份证认证，已经是V1用户则不能使用
			if(CertifyLevel.NOT_CERTIFY.getCode().equals(level)){
				response.setErrorMessage("请先进行身份证认证！");
				response.setSuccess(false);
				response.setErrorCode(CommonDefinedException.NO_VERIFY_ERROR.getErrorCode());
				return response;
			}else if(CertifyLevel.CERTIFY_V1.getCode().equals(level)){
				response.setErrorMessage("您的实名认证等级已经是实名认证V1,不能使用此功能！");
				response.setSuccess(false);
				response.setErrorCode(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode());
				return response;
			}
			
			String bankCardSts = PersonalCommonQueryRealName.queryRealName(req.getMemberId(), req.getOperatorId(), AuthType.BANK_CARD,defaultCertService);
	
			if (!(StringUtils.EMPTY.equals(bankCardSts) || ResultStatus.AUDIT_REJECT.getCode().equals(bankCardSts) || ResultStatus.CHECK_REJECT
					.getCode().equals(bankCardSts))) {
				response.setErrorMessage("您的银行卡认证已经提交！");
				response.setSuccess(true);
				return response;
			}
	
			// 卡bin校验
			CardBin cardBin = null;
			try {
				 cardBin = defaultPfsBaseService.queryByCardNo(req.getBankAccountNum(),
						CommonConstant.PERSONAL_APP_ID);
				if ((cardBin != null) && !Dbcr.DC.getCode().equals(cardBin.getCardType())) {
					response.setErrorMessage("绑定银行卡不能是信用卡!");
					response.setErrorCode(CommonDefinedException.CARD_TYPE_NOT_SUPPORTED.getErrorCode());
					response.setSuccess(false);
					return response;
				}
			} catch (Exception e) {
				logger.warn("未查询到卡bin信息，卡号={}", req.getBankAccountNum());
				response.setErrorCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
				response.setErrorMessage("BIN校验不通过");
				response.setSuccess(false);
				return response;
			}
	
			BankAccRequest bankReq = new BankAccRequest();
			bankReq.setMemberId(req.getMemberId());
			bankReq.setOperatorId(req.getOperatorId());
			bankReq.setRealName(req.getRealName());
			bankReq.setBankName(cardBin.getBankName());
			bankReq.setBankCode(req.getBankCode());
			bankReq.setBranchName(req.getBranchName());
			bankReq.setBranchNo(req.getBranchNo());
			bankReq.setCardType(Integer.valueOf(req.getCardType()));
			bankReq.setCardAttribute(Integer.valueOf(req.getCardAttribute()));
			bankReq.setPayAttribute("normal");// 支付属性：普通
			bankReq.setProvName(req.getProvName());
			bankReq.setCityName(req.getCityName());
			bankReq.setBankAccountNum(req.getBankAccountNum());
			bankReq.setIsVerified(0);// 0-未认证
			bankReq.setMemberIdentity(req.getMemberIdentity());// 会员标识
			defaultCertService.verifyBankCard(bankReq, env);
			response.setErrorMessage("银行卡认证成功.");
			response.setSuccess(true);
		} catch (ServiceException e) {
			logger.error("个人银行卡认证：" + e.getMessage());
			response.setErrorCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
			response.setErrorMessage(e.getMessage());
			response.setSuccess(false);
		} catch (Exception e) {
			logger.error("个人银行卡认证：" + e.getMessage());
			response.setErrorCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
			response.setErrorMessage("对不起，服务器繁忙，请稍后再试！");
			response.setSuccess(false);
		}
		return response;
	}

}
