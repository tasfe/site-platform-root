package com.netfinworks.site.domainservice.dccj.impl;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.MemberLockStatus;
import com.netfinworks.site.domain.enums.MemberStatus;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.netfinworks.site.domain.exception.ErrorCodeException;
import com.netfinworks.site.domainservice.cache.PayPassWordCacheService;
import com.netfinworks.site.domainservice.dccj.TransferService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.ClientEnvironment;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.member.impl.AccountServiceImpl;
import com.netfinworks.site.ext.integration.ues.UesServiceClient;


@Service("transferService")
public class TransferServiceImpl extends ClientEnvironment implements TransferService{
	
	private Logger         logger = LoggerFactory.getLogger(AccountServiceImpl.class);
	
	@Resource(name = "memberService")
	private MemberService memberService;
	
	@Resource(name = "uesService")
	private UesServiceClient uesServiceClient;
	
	@Resource(name = "defaultSmsService")
	private DefaultSmsService defaultSmsService;
	
	@Resource(name = "payPassWordCacheService")
	private PayPassWordCacheService payPassWordCacheService;
	
	@Resource(name = "defaultUesService")
	private DefaultUesService defaultUesService;
	
	
	private static final String REGISTER_TYPE = "1";//注册标志
	
	private static final String LOGIN_TYPE = "0";//登录标志
	
	/**
	 * 发送短信验证码
	 * @param bizId 
	 * @param mobile 绑定的手机号
	 * @param env
	 * @param bizType 约为类型
	 * @return
	 * @throws ServiceException   发送短信失败
	 */
	@Override
	public boolean sendMobileMessage(String bizId,String mobile, OperationEnvironment env,
		BizType bizType) throws ServiceException {
		// 封装发送短信请求
		AuthCodeRequest req = new AuthCodeRequest();
		req.setMobile(mobile);
		req.setBizId(bizId);
		req.setBizType(bizType.getCode());
		String ticket = defaultUesService.encryptData(mobile);
		req.setMobileTicket(ticket);
		req.setValidity(CommonConstant.VALIDITY);
		// 发送短信
		if (defaultSmsService.sendMessage(req, env)) {
			return true;
		} else {
			return false;
		}
	}
	/**
	 * 校验短信验证码是否正确
	 * @param memberIdentity  会员身份
	 * @param mobile 绑定手机号
	 * @param bizId 
	 * @param loginName 登录名
	 * @param bizType   业务类型
	 * @param otpValue  验证码
	 * @param env
	 * @return
	 * @throws ServiceException  验证处理失败
	 */
	@Override
	public boolean validateOtpValue(String memberIdentity, String loginName,String mobile,String bizId,
			BizType bizType,String otpValue, OperationEnvironment env){
			try {
				if ("email".equals(memberIdentity)
						|| "enterprise".equals(memberIdentity)) {
					String keyStr = mobile + "_" + otpValue.toLowerCase() + "_"
							+ env.getClientIp();
					String info = payPassWordCacheService.load(keyStr);
					if ((null == info) || "".equals(info)) {
						return false;
					}
				} else {
					AuthCodeRequest req = new AuthCodeRequest();
					req.setAuthCode(otpValue);
					req.setMobile(mobile);
					req.setMobileTicket(uesServiceClient.encryptData(mobile));
					req.setBizId(bizId);
					req.setBizType(bizType.getCode());
					if(bizType.equals(BizType.REGISTER_MOBILE)){
						if (isNeedActiveMember(loginName, env)) {
							req.setBizType(BizType.ACTIVE_MOBILE.getCode());
						} else {
							req.setBizType(BizType.REGISTER_MOBILE.getCode());
						}						
					}
					logger.info("校验手机短信请求参数：{}",req.toString());
					boolean result = defaultSmsService.verifyMobileAuthCode(req,
							env);
					return result;
				}
			} catch (Exception e) {
				return false;
			}
			return true;
	}
	
	/**
	 * 是否需要激活
	 * 
	 * @param loginName
	 * @param env
	 * @return
	 */
	@Override
	public boolean isNeedActiveMember(
			String loginName, OperationEnvironment env) throws ServiceException {
		// 账户是否激活
		boolean isNeedActiveMember = false;
		try {
			PersonMember person = new PersonMember();
			person.setLoginName(loginName);
			person = memberService.queryMemberIntegratedInfo(person, env);
			if (null != person && null != person.getStatus()) {
				if (person.getMemberType() == MemberType.PERSONAL) {
					isNeedActiveMember = person.getStatus() == MemberStatus.NOT_ACTIVE ? true
							: false;
				}
			}
		} catch (Exception e) {
			logger.info("{}未注册，无需激活", loginName);
		}
		return isNeedActiveMember;
	}


	/**
	 * 检查用户是否存在
	 */
	@Override
	public RestResponse checkLoginUserName(String username, RestResponse restP,String type){
		OperationEnvironment env = new OperationEnvironment();
		PersonMember person = new PersonMember();
		person.setLoginName(username);
		try {
			person = memberService.queryMemberIntegratedInfo(person, env);
			if (REGISTER_TYPE.equals(type) && !StringUtils.isBlank(person.getMemberId())) {
				return buildExpResponse(CommonDefinedException.ACCOUNT_EXIST_ERROR,CommonDefinedException.ACCOUNT_EXIST_ERROR.getErrorMsg());
			}
			if (person.getLockStatus() == MemberLockStatus.LOCKED) {
				return buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT,"用户名被锁定");
			}
		} catch (Exception e) {
			if (LOGIN_TYPE.equals(type)) {
				return buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT,"用户不存在，请先注册");
			} 
				restP.setSuccess(true);
		}
		return restP;
	}
	
	public static RestResponse buildExpResponse(ErrorCodeException.CommonException exp,String errorMsg) {
		return buildExpResponse(exp.getErrorCode(),errorMsg);
    }
	
	public static RestResponse buildExpResponse(String errorCode,String errorMsg) {
        RestResponse jsonResp = new RestResponse();
        jsonResp.setSuccess(false);
        jsonResp.setCode(errorCode);
        jsonResp.setMessage(errorMsg);
		return jsonResp;
    }

}
