package com.yongda.site.ext.service.facade.personal.impl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kjt.unionma.core.common.enumes.SysResponseCode;
import com.netfinworks.ma.service.facade.IMemberFacade;
import com.netfinworks.ma.service.request.AccountQueryRequest;
import com.netfinworks.ma.service.request.MemberIntegratedIdRequest;
import com.netfinworks.ma.service.response.MemberIntegratedResponse;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.util.MD5Util;
import com.netfinworks.site.domain.domain.member.MemberInfo;
import com.netfinworks.site.domain.domain.request.PayPasswordCheckReq;
import com.netfinworks.site.domain.domain.response.UnionmaBaseResponse;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.ext.integration.member.impl.MaQueryService;
import com.netfinworks.site.ext.integration.unionma.PayPasswordService;
import com.netfinworks.site.ext.service.facade.converter.HttpRequestConvert;
import com.yongda.site.ext.service.facade.personal.common.CommonValidator;
import com.yongda.site.ext.service.facade.personal.common.PersonalFindBindThirdAccount;
import com.yongda.site.service.personal.facade.request.PersonalVerificationPayPWDRequest;
import com.yongda.site.service.personal.facade.response.BaseResponse;

public class PersonalVerificationPayPWDService extends AbstractRoutService<PersonalVerificationPayPWDRequest, BaseResponse> {
	private Logger logger=LoggerFactory.getLogger(this.getClass());
	@Resource(name = "commonValidator")
	protected CommonValidator commonValidator;
	@Resource(name = "maQueryService")
	private MaQueryService maQueryService;
	/*@Resource(name = "defaultPayPasswdService")
    private DefaultPayPasswdService defaultPayPasswdService;*/
	
	@Resource(name = "payPasswordService")
	private PayPasswordService payPasswordService;
	
	@Resource(name = "memberFacade")
	private IMemberFacade memberFacade;

	@Resource(name="personalFindBindThirdAccount")
	private PersonalFindBindThirdAccount personalFindBindThirdAccount;
	@Override
	public String getRoutName() {
		return "personal_verification_pay_pwd";
	}

	@Override
	public PersonalVerificationPayPWDRequest buildRequest(
			Map<String, String> paramMap) {
		return HttpRequestConvert.convert2Request(paramMap, PersonalVerificationPayPWDRequest.class);
		
	}

	@Override
	public BaseResponse doProcess(
			PersonalVerificationPayPWDRequest req) {
		BaseResponse resp=new BaseResponse();
		
		/**校验参数**/
		String errorMsg = commonValidator.validate(req);			
		if (StringUtils.isNotEmpty(errorMsg)) {
			resp.setErrorMessage(errorMsg);
			resp.setErrorCode(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode());
			resp.setSuccess(false);
			logger.info("缺少必要的参数！"+errorMsg);
			return resp;
		}
		
		/********************************************/
		Map<String,String> map=null;
		try {
			map=getaccountId(req.getMemberId());
		} catch (Exception e) {
			resp.setErrorMessage(e.getMessage());
			resp.setErrorCode(CommonDefinedException.SYSTEM_ERROR.getErrorCode());
			resp.setSuccess(false);
			logger.error("",e);
			return resp;
		}
		/**修改支付密码--验证加盐支付密码**/
		PayPasswordCheckReq pwdreq = new PayPasswordCheckReq();
		pwdreq.setOperatorId(map.get("operatorId"));
		pwdreq.setAccountId(map.get("accountId"));
		pwdreq.setPayPassword(req.getPasswd());
		try {
			
			UnionmaBaseResponse checkResult = payPasswordService.payPasswordCheck(pwdreq);
			if(!checkResult.getIsSuccess()){
				if(StringUtils.equalsIgnoreCase(checkResult.getResponseCode(),SysResponseCode.PAY_PASSWORD_LOCKED.getCode())){
					throw new ServiceException("支付密码被锁定",null);
				}else if(StringUtils.equalsIgnoreCase(checkResult.getResponseCode(),SysResponseCode.PAY_PASSWORD_WRONG.getCode())){
					throw new ServiceException("支付密码错误",null);
				}else if(StringUtils.equalsIgnoreCase(checkResult.getResponseCode(),SysResponseCode.PAY_PWD_IS_NULL.getCode())){
					throw new ServiceException("支付密码未设置",null);
				}else
					throw new ServiceException(StringUtils.isNotBlank(checkResult.getResponseMessage())?checkResult.getResponseMessage():"支付密码验证未通过",null);
			}else{
				resp.setErrorMessage("密码验证通过");
				resp.setSuccess(true);
				logger.info("memberId:"+req.getMemberId()+"--校验密码通过");
				return resp;
			}
		} catch (ServiceException e) {
			resp.setErrorMessage(e.getMessage());
			resp.setSuccess(false);
			resp.setErrorCode(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode());
			logger.error("加盐支付密码验证过程失败："+e.getMessage(),e);
			return resp;
		} catch (BizException e) {
			resp.setErrorMessage(e.getMessage());
			resp.setSuccess(false);
			resp.setErrorCode(e.getCode().getCode());
			logger.error("加盐支付密码验证过程异常："+e.getMessage(),e);
			return resp;
		}
	}
	
	/**获取操作员id、账户id、手机号码**/
	private Map<String,String> getaccountId(String memberId) throws Exception{
		Map<String,String> map=new HashMap<String,String>();
		MemberInfo memberInfo = maQueryService.queryMemberByMemberId(memberId);//根据id查出所有帐号信息
		
		/**获取操作员id**/
		MemberIntegratedIdRequest paramMemberIntegratedIdRequest = new MemberIntegratedIdRequest();
		paramMemberIntegratedIdRequest.setMemberId(memberId);
		paramMemberIntegratedIdRequest.setRequireDefaultOperator(true);
		paramMemberIntegratedIdRequest.setRequireVerifyInfos(true);
		//查询账号
        AccountQueryRequest accountRequest = new AccountQueryRequest();
        accountRequest.setRequireAccountInfos(true);
		MemberIntegratedResponse queryResult = memberFacade.queryMemberIntegratedInfoById(
			           null, paramMemberIntegratedIdRequest);
		map.put("operatorId",queryResult.getDefaultOperator().getOperatorId());
		map.put("accountId",memberInfo.getBaseAccountId());
		return map;
	}


	
}
