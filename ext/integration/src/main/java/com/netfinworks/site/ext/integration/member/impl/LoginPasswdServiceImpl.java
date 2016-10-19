/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.ext.integration.member.impl;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.cert.service.facade.ICertFacade;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.ma.service.base.response.Response;
import com.netfinworks.ma.service.facade.ILoginPwdFacade;
import com.netfinworks.ma.service.request.LoginPwdRequest;
import com.netfinworks.ma.service.request.OperatorLoginPwdByIdRequest;
import com.netfinworks.ma.service.request.OperatorLoginPwdRequest;
import com.netfinworks.ma.service.request.PersonalLoginPwdLockRequest;
import com.netfinworks.ma.service.request.PersonalLoginPwdRequest;
import com.netfinworks.ma.service.request.ValidateLoginPwdRequest;
import com.netfinworks.ma.service.response.LoginPwdResponse;
import com.netfinworks.ma.service.response.ValidateLoginPwdResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.comm.CommResponse;
import com.netfinworks.site.domain.domain.info.LoginPasswdCheck;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.LoginPasswdRequest;
import com.netfinworks.site.domain.domain.request.OperatorLoginPasswdRequest;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.enums.IdentityType;
import com.netfinworks.site.domain.enums.NewPlatformTypeEnum;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.ClientEnvironment;
import com.netfinworks.site.ext.integration.comm.convert.CommResponseConvert;
import com.netfinworks.site.ext.integration.member.LoginPasswdService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.member.convert.LoginPasswdConvert;
import com.netfinworks.ues.util.UesUtils;
import com.netfinworks.ufs.client.UFSClient;

@Service("loginPasswdService")
public class LoginPasswdServiceImpl extends ClientEnvironment implements LoginPasswdService {
    private Logger          logger = LoggerFactory.getLogger(LoginPasswdServiceImpl.class);

    @Resource(name = "loginPwdFacade")
    private ILoginPwdFacade loginPwdFacade;
    
    @Resource(name = "memberService")
	private MemberService memberService;

    @Resource(name = "ufsClient")
    private UFSClient       ufsClient;
    @Resource(name = "certFacade")
    private ICertFacade     certFacade;

    /* (non-Javadoc)修改登录密码
     * @see com.netfinworks.site.ext.integration.member.LoginPasswdService#setLoginPassword(com.netfinworks.site.domain.domain.request.LoginPasswdRequest)
     */
    @Override
    public CommResponse setLoginPassword(LoginPasswdRequest req) throws BizException {
        if (logger.isInfoEnabled()) {
            logger.info("修改登录密码requst参数:" + req.toString());
        }
        LoginPwdRequest request = LoginPasswdConvert.createLoginPasswordRequest(req);
        Response response = loginPwdFacade.setLoginPwd(getEnv(req.getClientIp()), request);
        CommResponse commRep = CommResponseConvert.commResponseConvert(response);
        if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
            if (logger.isInfoEnabled()) {
				logger.info("修改登录密码成功：信息：" + response.getResponseMessage());
			}
            commRep.setSuccess(true);
            return commRep;
        } else {
            String error = "修改登录密码失败,返回信息：" + response.getResponseMessage();
            logger.error(error);
            commRep.setSuccess(false);
            if(ResponseCode.LOGIN_PASSWORD_EQUAL_PAY.getCode().equals(response.getResponseCode())){
                return commRep;
            }else{
                throw new BizException(ErrorCode.SYSTEM_ERROR, error);
            }
        }
    }

    /* (non-Javadoc) 重置登录密码锁
     * @see com.netfinworks.site.ext.integration.member.LoginPasswdService#resetLoginPasswordLock(com.netfinworks.site.domain.domain.request.LoginPasswdRequest)
     */
    @Override
    public boolean resetLoginPasswordLock(LoginPasswdRequest req) throws BizException {
        PersonalLoginPwdLockRequest request = LoginPasswdConvert.createLoginPwdLockRequest(req);
        Response response = loginPwdFacade.resetPersonalLoginPwdLock(getEnv(req.getClientIp()),
            request);
        if (ResponseCode.SUCCESS.equals(response.getResponseCode())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public LoginPasswdCheck checkLoginPwdToSalt(LoginPasswdRequest req) throws BizException {
        try {
            long beginTime = 0L;
            if (logger.isInfoEnabled()) {
                logger.info("远程验证加盐登录密码，请求参数：{}", req);
                beginTime = System.currentTimeMillis();
            }
            PersonalLoginPwdRequest request = LoginPasswdConvert.createLoginPwdCheckRequest(req);
            LoginPwdResponse response = loginPwdFacade.checkPersonalLoginPwd(
                getEnv(req.getClientIp()), request);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("远程验证加盐登录密码， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime,
                        response });
            }
            LoginPasswdCheck result = new LoginPasswdCheck();
            if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
                result.setSuccess(true);
                if (logger.isInfoEnabled()) {
					logger.info("远程验证加盐登录密码: 成功");
				}
                return result;
            } else {
                result.setSuccess(false);
                result.setRemainNum(response.getRemainNum() != null ? response.getRemainNum()
                    .intValue() : -1);
                if (CommonConstant.LOGINPASSWD_LOCKED.equals(response.getResponseCode())) {
                    result.setLocked(true);
                }
                if (logger.isInfoEnabled()) {
					logger.info("远程验证加盐登录密码: 登录密码被锁定了");
				}
                return result;
            }
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error("远程验证加盐支付密码: {},信息异常{}", req, e);
                throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage());
            }
        }
    }

    /* (non-Javadoc) 验证登录密码是否被锁定
     * @see com.netfinworks.site.ext.integration.member.LoginPasswdService#validatePayPwd(com.netfinworks.site.domain.domain.request.LoginPasswdRequest)
     */
    @Override
    public boolean validateLoginPwd(LoginPasswdRequest req) throws BizException {
        ValidateLoginPwdRequest request = LoginPasswdConvert.createValidateLoginPwdRequest(req);
        ValidateLoginPwdResponse response = loginPwdFacade.validateLoginPwd(
            getEnv(req.getClientIp()), request);
        if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
            return response.getIsLock() == 1;
        } else {
            String error = "验证登录密码是否锁定失败：信息：" + response.getResponseMessage();
            logger.error(error);
            throw new BizException(ErrorCode.SYSTEM_ERROR, error);
        }
    }

	@Override
	public PersonMember checkMemberLoginPwd(String loginName, String password,String memberIdentity,
			String salt,String clientIp) throws BizException {
		PersonalLoginPwdRequest request = new PersonalLoginPwdRequest();
		request.setMemberIdentity(loginName);
		request.setPlatformType("1");
		String saltPasswd = UesUtils.hashSignContent(UesUtils.hashSignContent(password)
                + salt);
		request.setPassword(saltPasswd);
		request.setSalt(salt);
		try {
			OperationEnvironment env = new OperationEnvironment();
			LoginPwdResponse response = loginPwdFacade.checkPersonalLoginPwd(getEnv(clientIp), request);
 			if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
				return this.queryMember(response.getMemberId(), memberIdentity, loginName, env);
			} 
//			ResponseCode=274 表示解锁剩余时间（单位为分钟），ResponseCode=266 表示还可以重试次数。
			else if("274".equals(response.getResponseCode())){
				logger.error("验证密码异常:返回信息:{}", "登录密码被锁定,请"+response.getRemainNum()+"分钟后尝试");
				throw new BizException(ErrorCode.SYSTEM_ERROR, "登录密码被锁定,请"+response.getRemainNum()+"分钟后尝试");
			}else if("266".equals(response.getResponseCode())){
				logger.error("验证密码异常:返回信息:{}", "登录密码错误,还可以重试"+response.getRemainNum()+"次");
				throw new BizException(ErrorCode.SYSTEM_ERROR, "登录密码错误,还可以重试"+response.getRemainNum()+"次");
			}else if("267".equals(response.getResponseCode())){
				logger.error("验证密码异常:返回信息:{}", "尚未设置登录密码[尚未设置登录密码]");
				throw new BizException(ErrorCode.SYSTEM_ERROR, "该账户未设置登录密码，请进行账户激活");
			}else if("130".equals(response.getResponseCode())){
				logger.error("验证密码异常:返回信息:{}", "尚未激活账户[尚未激活账户]");
				throw new BizException(ErrorCode.SYSTEM_ERROR, "该账户未设置登录密码，请进行账户激活");
			}else if (CommonConstant.USER_NOT_EXIST.equals(response.getResponseCode())) {// 用户不存在
				throw new BizException(ErrorCode.ACCOUNT_NOT_EXIST);
			} else{
				logger.error("验证密码异常:返回信息:{}", response.getResponseMessage());
				throw new BizException(ErrorCode.SYSTEM_ERROR, response.getResponseMessage());
			}

		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error("验证会员异常:错误信息:{}", e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
			}
		}
	}
	
	@Override
	public EnterpriseMember checkEnterpriseLoginPwd(String loginName, String password,String memberIdentity,
			String salt,String clientIp) throws BizException {
		OperatorLoginPwdRequest request = new OperatorLoginPwdRequest();
		request.setLoginName(loginName);
		request.setMemberIdentity(loginName);
		request.setPlatformType("1");
		request.setLoginNamePlatformType("1");
		String saltPasswd = UesUtils.hashSignContent(UesUtils.hashSignContent(password)
				+ salt);
		request.setPassword(saltPasswd);
		request.setSalt(salt);
		try {
			OperationEnvironment env = new OperationEnvironment();
//			LoginPwdResponse response = loginPwdFacade.checkPersonalLoginPwd(getEnv(clientIp), request);
			LoginPwdResponse response = loginPwdFacade.checkOperatorLoginPwd(getEnv(clientIp), request);
			if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
				return this.queryMemberEnterprise(response.getMemberId(), null, loginName, loginName, env);
			} 
//			ResponseCode=274 表示解锁剩余时间（单位为分钟），ResponseCode=266 表示还可以重试次数。
			else if("274".equals(response.getResponseCode())){
				logger.error("验证密码异常:返回信息:{}", "登录密码被锁定,请"+response.getRemainNum()+"分钟后尝试");
				throw new BizException(ErrorCode.SYSTEM_ERROR, "登录密码被锁定,请"+response.getRemainNum()+"分钟后尝试");
			}else if("266".equals(response.getResponseCode())){
				logger.error("验证密码异常:返回信息:{}", "登录密码错误,还可以重试"+response.getRemainNum()+"次");
				throw new BizException(ErrorCode.SYSTEM_ERROR, "登录密码错误,还可以重试"+response.getRemainNum()+"次");
			}else if("267".equals(response.getResponseCode())){
				logger.error("验证密码异常:返回信息:{}", "尚未设置登录密码[尚未设置登录密码]");
				throw new BizException(ErrorCode.SYSTEM_ERROR, "该账户未设置登录密码，请进行账户激活");
			}else if("130".equals(response.getResponseCode())){
				return null;
			}else if (CommonConstant.USER_NOT_EXIST.equals(response.getResponseCode())) {// 用户不存在
				throw new BizException(ErrorCode.ACCOUNT_NOT_EXIST);
			} else{
				logger.error("验证密码异常:返回信息:{}", response.getResponseMessage());
				throw new BizException(ErrorCode.SYSTEM_ERROR, response.getResponseMessage());
			}
			
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error("验证会员异常:错误信息:{}", e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
			}
		}
	}
	
	
	@Override
	public EnterpriseMember checkOperatorLoginPwd(String operator_loginName,String operator_id, String password,String memberIdentity,
			String salt,String clientIp) throws BizException {
		OperatorLoginPwdRequest request = new OperatorLoginPwdRequest();
		request.setLoginName(operator_loginName);
		request.setMemberIdentity(memberIdentity);
		request.setPlatformType(IdentityType.EMAIL.getPlatformType());
		request.setLoginNamePlatformType("1");
		
		String saltPasswd = UesUtils.hashSignContent(UesUtils.hashSignContent(password)
				+ salt);
		request.setPassword(saltPasswd);
		request.setSalt(salt);
		try {
			OperationEnvironment env = new OperationEnvironment();
			LoginPwdResponse response = loginPwdFacade.checkOperatorLoginPwd(getEnv(clientIp), request);
			if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
				return this.queryMemberEnterprise(response.getMemberId(), operator_id, memberIdentity,
						operator_loginName, env);
			} 
//			ResponseCode=274 表示解锁剩余时间（单位为分钟），ResponseCode=266 表示还可以重试次数。
			else if("274".equals(response.getResponseCode())){
				logger.error("验证密码异常:返回信息:{}", "登录密码被锁定,请"+response.getRemainNum()+"分钟后尝试");
				throw new BizException(ErrorCode.SYSTEM_ERROR, "登录密码被锁定,请"+response.getRemainNum()+"分钟后尝试");
			}else if("266".equals(response.getResponseCode())){
				logger.error("验证密码异常:返回信息:{}", "登录密码错误,还可以重试"+response.getRemainNum()+"次");
				throw new BizException(ErrorCode.SYSTEM_ERROR, "登录密码不正确,还有" + response.getRemainNum() + "次机会！");
			}else if("130".equals(response.getResponseCode())){
				return null;
			}else if (CommonConstant.USER_NOT_EXIST.equals(response.getResponseCode())) {// 用户不存在
				throw new BizException(ErrorCode.ACCOUNT_NOT_EXIST);
			} else{
				logger.error("验证密码异常:返回信息:{}", response.getResponseMessage());
				throw new BizException(ErrorCode.SYSTEM_ERROR, response.getResponseMessage());
			}
			
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error("验证会员异常:错误信息:{}", e);
				throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
			}
		}
	}
	
	public PersonMember queryMember(String memberId, String memberIdentity,String loginName, OperationEnvironment env) throws BizException {
		PersonMember personMember = new PersonMember();
		try {
			PersonMember user = new PersonMember();
			user.setMemberId(memberId);
			user.setPlateFormId(NewPlatformTypeEnum.KJT.getCode().toString());
			user.setMemberIdentity(memberIdentity);
			user.setLoginName(loginName);
			personMember = memberService.queryMemberIntegratedInfo(user, env);
		} catch (Exception e) {
		}
		return personMember;
	}
	public EnterpriseMember queryMemberEnterprise(String memberId,String operator_id, String memberIdentity,String loginName, OperationEnvironment env) throws BizException {
		EnterpriseMember personMember = new EnterpriseMember();
		try {
			EnterpriseMember member = new EnterpriseMember();
			member.setMemberId(memberId);
			member.setLoginName(memberIdentity);
			member.setOperator_login_name(loginName);
			member.setCurrentOperatorId(operator_id);
			personMember = memberService.queryCompanyMember(member, env);
		} catch (Exception e) {
		}
		return personMember;
	}

    /* (non-Javadoc)
     * @see com.netfinworks.site.ext.integration.member.LoginPasswdService#setLoginPasswordEnt(com.netfinworks.site.domain.domain.request.OperatorLoginPasswdRequest)
     */
    @Override
    public CommResponse setLoginPasswordEnt(OperatorLoginPasswdRequest req) throws BizException {
        if (logger.isInfoEnabled()) {
            logger.info("修改企业会员登录密码requst参数:" + req.toString());
        }
        LoginPwdRequest request = LoginPasswdConvert.createEntLoginPasswordRequest(req);
        Response response = loginPwdFacade.setLoginPwd(getEnv(req.getClientIp()), request);
        CommResponse commRep = CommResponseConvert.commResponseConvert(response);
        if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
            if (logger.isInfoEnabled()) {
				logger.info("修改企业会员登录密码成功：信息：" + response.getResponseMessage());
			}
            commRep.setSuccess(true);
            return commRep;
        } else {
            String error = "修改企业会员登录密码失败,返回信息：" + response.getResponseMessage();
            logger.error(error);
            commRep.setSuccess(false);
            if(ResponseCode.LOGIN_PASSWORD_EQUAL_PAY.getCode().equals(response.getResponseCode())){
                return commRep;
            }else{
                throw new BizException(ErrorCode.SYSTEM_ERROR, error);
            }
        }
    }

    /* (non-Javadoc)企业会员验证原登录密码
     * @see com.netfinworks.site.ext.integration.member.LoginPasswdService#checkEntLoginPwdToSalt(com.netfinworks.site.domain.domain.request.OperatorLoginPasswdRequest)
     */
    @Override
    public LoginPasswdCheck checkEntLoginPwdToSalt(OperatorLoginPasswdRequest req)
                                                                                  throws BizException {
        try {
            long beginTime = 0L;
            if (logger.isInfoEnabled()) {
                logger.info("企业会员远程验证加盐登录密码，请求参数：{}", req);
                beginTime = System.currentTimeMillis();
            }
            OperatorLoginPwdByIdRequest request = LoginPasswdConvert.createEntLoginPwdCheckRequest(req);
            LoginPwdResponse response = loginPwdFacade.checkOperatorLoginPwdById(getEnv(req.getClientIp()), request);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("企业会员远程验证加盐登录密码， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime,
                        response });
            }
            LoginPasswdCheck result = new LoginPasswdCheck();
            if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
                result.setSuccess(true);
                if (logger.isInfoEnabled()) {
					logger.info("企业会员远程验证加盐登录密码: 成功");
				}
                return result;
            } else {
                result.setSuccess(false);
                result.setRemainNum(response.getRemainNum() != null ? response.getRemainNum()
                    .intValue() : -1);
                if (CommonConstant.LOGINPASSWD_LOCKED.equals(response.getResponseCode())) {
                    result.setLocked(true);
                }
                if (logger.isInfoEnabled()) {
					logger.info("企业会员远程验证加盐登录密码: 登录密码被锁定了");
				}
                return result;
            }
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error("企业会员远程验证加盐支付密码: {},信息异常{}", req, e);
                throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage());
            }
        }
    }

    /* (non-Javadoc)验证登录密码是否设置，是否锁定（企业会员）
     * @see com.netfinworks.site.ext.integration.member.LoginPasswdService#validateLoginPwdEnt(com.netfinworks.site.domain.domain.request.OperatorLoginPasswdRequest)
     */
    @Override
    public boolean validateLoginPwdEnt(OperatorLoginPasswdRequest req) throws BizException {
        ValidateLoginPwdRequest request = LoginPasswdConvert.createValidateLoginPwdRequest(req);
        ValidateLoginPwdResponse response = loginPwdFacade.validateLoginPwd(
            getEnv(req.getClientIp()), request);
        if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
            return response.getIsLock() == 1;
        } else {
            String error = "验证企业会员登录密码是否锁定失败：信息：" + response.getResponseMessage();
            logger.error(error);
            throw new BizException(ErrorCode.SYSTEM_ERROR, error);
        }
    }
    
    /**
     * 验证操作员是否设置登录密码（个人会员）
     */
	@Override
	public boolean validateLoginPwdIsNull(LoginPasswdRequest req)
			throws BizException {
		   ValidateLoginPwdRequest request = LoginPasswdConvert.createValidateLoginPwdRequest(req);
	        ValidateLoginPwdResponse response = loginPwdFacade.validateLoginPwd(
	            getEnv(req.getClientIp()), request);
	        if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
	            return response.getIsSet() == 0;
	        } else {
	            String error = "验证登录密码是否为空失败：信息：" + response.getResponseMessage();
	            logger.error(error);
	            throw new BizException(ErrorCode.SYSTEM_ERROR, error);
	        }
	}


}
