package com.yongda.site.ext.service.facade.personal.impl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netfinworks.ma.service.facade.IMemberFacade;
import com.netfinworks.ma.service.request.AccountQueryRequest;
import com.netfinworks.ma.service.request.MemberIntegratedIdRequest;
import com.netfinworks.ma.service.response.MemberIntegratedResponse;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.util.MD5Util;
import com.netfinworks.site.domain.domain.comm.CommResponse;
import com.netfinworks.site.domain.domain.info.PayPasswdCheck;
import com.netfinworks.site.domain.domain.member.MemberInfo;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.ext.integration.member.impl.MaQueryService;
import com.netfinworks.site.ext.service.facade.converter.HttpRequestConvert;
import com.yongda.site.ext.service.facade.personal.common.CommonValidator;
import com.yongda.site.ext.service.facade.personal.common.PersonalFindBindThirdAccount;
import com.yongda.site.service.personal.facade.request.PersonalModifyPayPWDRequest;
import com.yongda.site.service.personal.facade.response.BaseResponse;

public class PersonalModifyPayPWDService extends AbstractRoutService<PersonalModifyPayPWDRequest, BaseResponse> {
	@Resource(name = "commonValidator")
	protected CommonValidator commonValidator;
	@Resource(name = "maQueryService")
	private MaQueryService maQueryService;
	@Resource(name = "memberFacade")
	private IMemberFacade memberFacade;
	@Resource(name = "defaultPayPasswdService")
    private DefaultPayPasswdService defaultPayPasswdService;
	@Resource(name="personalFindBindThirdAccount")
	private PersonalFindBindThirdAccount personalFindBindThirdAccount;
	private Logger logger=LoggerFactory.getLogger(this.getClass());
	@Override
	public String getRoutName() {
		// TODO Auto-generated method stub
		return "personal_modify_pay_pwd";
	}

	@Override
	public PersonalModifyPayPWDRequest buildRequest(Map<String, String> paramMap) {
		return HttpRequestConvert.convert2Request(paramMap, PersonalModifyPayPWDRequest.class);
	}

	@SuppressWarnings("finally")
	@Override
	public BaseResponse doProcess(PersonalModifyPayPWDRequest req) {
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
		if(!(req.getNewPasswd().equals(req.getRenewPasswd())&&!req.getNewPasswd().equals(req.getOldPasswd()))){
			resp.setErrorMessage("新密码与确认密码不符||新密码与旧密码不能相同！");
			resp.setErrorCode(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode());
			resp.setSuccess(false);
			logger.info("新密码与确认密码不符||新密码与旧密码不能相同！");
			return resp;
		}
		if(!judgmentPWD(req.getNewPasswd())){
			resp.setErrorMessage("新密码强度不够，应该为包含字母、数字、特殊符号中的两种或以上组合，并且长度在7-23位！");
			resp.setErrorCode(CommonDefinedException.ILLEGAL_ARGUMENT.getErrorCode());
			resp.setSuccess(false);
			logger.info("新密码强度不够，应该为包含字母、数字、特殊符号中的两种或以上组合，并且长度在7-23位！");
			return resp;
		}
		Map<String,String> map=null;
		try {
			map=getaccountId(req.getMemberId());
		} catch (Exception e) {
			resp.setErrorMessage(CommonDefinedException.MEMBER_ID_NOT_EXIST.getErrorMsg());
			resp.setErrorCode(CommonDefinedException.MEMBER_ID_NOT_EXIST.getErrorCode());
			resp.setSuccess(false);
			logger.error("",e);
			return resp;
		}
		/**修改支付密码--验证加盐支付密码**/
		PayPasswdRequest pwdreq = new PayPasswdRequest();
		pwdreq.setOperator(map.get("operatorId"));
		pwdreq.setAccountId(map.get("accountId"));
		pwdreq.setOldPassword( MD5Util.MD5(req.getOldPasswd()));
		pwdreq.setValidateType(1);
		pwdreq.setPassword(MD5Util.MD5(req.getNewPasswd()));
		try {
			PayPasswdCheck checkResult = defaultPayPasswdService.checkPayPwdToSalt(pwdreq);
			if(!checkResult.isSuccess()){
				if(checkResult.isLocked()){
					if (logger.isInfoEnabled()) {
		                logger.info("支付密码被锁定");
		                throw new ServiceException("支付密码被锁定",null);
					}
				}
				throw new ServiceException("支付密码错误||未设置支付密码",null);
			}
		} catch (ServiceException e) {
			resp.setErrorMessage(e.getMessage());
			resp.setErrorCode(CommonDefinedException.PASSWORD_ERROR.getErrorCode());
			resp.setSuccess(false);
			logger.error("加盐支付密码验证过程失败："+e.getMessage(),e);
			return resp;
		}
		/**设置支付密码**/
		try {
			CommResponse commRep = defaultPayPasswdService.setPayPassword(pwdreq);
			if (commRep.isSuccess()) {
				resp.setErrorMessage("修改支付密码成功！");
				resp.setSuccess(true);
			}else{
				resp.setSuccess(false);
				if(ResponseCode.PASSWORD_EQUAL_LOGIN_PASSWORD.getCode().equals(commRep.getResponseCode())) {
					resp.setErrorMessage("支付密码不能和登录密码相同！");
					resp.setErrorCode(CommonDefinedException.PASSWORD_ERROR.getErrorCode());
				}else{
					resp.setErrorMessage("修改支付密码失败");
					resp.setErrorCode(CommonDefinedException.PASSWORD_ERROR.getErrorCode());
				}
			}
		} catch (ServiceException e) {
			resp.setErrorMessage("修改支付密码失败:"+e.getMessage());
			resp.setErrorCode(CommonDefinedException.PASSWORD_ERROR.getErrorCode());
			resp.setSuccess(false);
			logger.error("设置支付密码错误："+e.getMessage(),e);
			
		}finally{
			return resp;
		}
	}
	
	
	/**判断密码格式是否正确   正确=true**/
	private boolean judgmentPWD(String str){
		String zz1="^([a-zA-z0-9~!@#$%^&*\\;',./_+|{}\\[\\]:\"<>?]{7,23})?$";
		String zz2="^(((?=[\\x20-\\x7e]*?[A-Za-z])(?=[\\x20-\\x7e]*?[0-9])[\\x20-\\x7E]{7,23})|((?=[\\x20-\\x7e]*?[\\x20-\\x2f\\x3a-\\x40\\x5b-\\x60\\x7b-\\x7e])(?=[\\x20-\\x7e]*?[0-9])[\\x20-\\x7E]{7,23})|((?=[\\x20-\\x7e]*?[A-Za-z])(?=[\\x20-\\x7e]*?[\\x20-\\x2f\\x3a-\\x40\\x5b-\\x60\\x7b-\\x7e])[\\x20-\\x7E]{7,23}))$";
		System.out.println("TF:"+(str.matches(zz1)&&str.matches(zz2)));
		return str.matches(zz1)&&str.matches(zz2);
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
		map.put("phone",memberInfo.getMemberIdentity());
		return map;
	}

}
