/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.ext.integration.member;


import com.netfinworks.site.domain.domain.comm.CommResponse;
import com.netfinworks.site.domain.domain.info.LoginPasswdCheck;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.request.LoginPasswdRequest;
import com.netfinworks.site.domain.domain.request.OperatorLoginPasswdRequest;
import com.netfinworks.site.domain.exception.BizException;

/**
 * <p>会员远程调用接口：登录密码</p>
 * @author zhangyun.m
 * @version $Id: LoginPasswdService.java, v 0.1 2014年5月20日 下午5:16:30 zhangyun.m Exp $
 */
public interface LoginPasswdService {

    /**
     * 设置登录密码（个人会员）
     * @param env
     * @param request
     * @return BankAccountResponse
     */
    public CommResponse setLoginPassword(LoginPasswdRequest req) throws BizException;
    
    /**设置登录密码（企业会员）
     * @param req
     * @return
     * @throws BizException
     */
    public CommResponse setLoginPasswordEnt(OperatorLoginPasswdRequest req) throws BizException;

    /**
     * 密码解锁
     * @param req
     * @return
     */
    public boolean resetLoginPasswordLock(LoginPasswdRequest req) throws BizException;

    /**
     * 验证加盐登录密码(个人会员)
     * @param env
     * @param request
     * @return boolean
     */
    public LoginPasswdCheck checkLoginPwdToSalt(LoginPasswdRequest req) throws BizException;
    
    /**
     * 验证加盐登录密码(企业会员)
     * @param env
     * @param request
     * @return boolean
     */
    public LoginPasswdCheck checkEntLoginPwdToSalt(OperatorLoginPasswdRequest req) throws BizException;

    /**
     * 验证操作员是否设置登录密码，登录密码是否锁定（个人会员）
     * @param env
     * @param request
     * @return boolean
     */
    public boolean validateLoginPwd(LoginPasswdRequest req) throws BizException;
    
    
    /**
     * 验证操作员是否设置登录密码，登录密码是否锁定（企业会员）
     * @param env
     * @param request
     * @return boolean
     */
    public boolean validateLoginPwdEnt(OperatorLoginPasswdRequest req) throws BizException;
    
    /**
     * 验证登录
    * @Title: checkMemberLoginPwd
    *  @param loginName
    *  @param password
    *  @param salt
    *  @throws BizException
    * @return PersonMember
    * @throws
     */
    public PersonMember checkMemberLoginPwd(String loginName, String password, String memberIdentity, String salt,String clientIp) throws BizException;
    
    /**
     * 验证企业会员登录
     * @Title: checkMemberLoginPwd
     *  @param loginName
     *  @param password
     *  @param salt
     *  @throws BizException
     * @return PersonMember
     * @throws
     */
    public EnterpriseMember checkEnterpriseLoginPwd(String loginName, String password, String memberIdentity, String salt,String clientIp) throws BizException;
    
    /**
     * 验证企业会员登录
     * @Title: checkMemberLoginPwd
     *  @param loginName
     *  @param password
     *  @param salt
     *  @throws BizException
     * @return PersonMember
     * @throws
     */
    public EnterpriseMember checkOperatorLoginPwd(String operator_loginName,String operator_id, String password, String memberIdentity, String salt,String clientIp) throws BizException;
    
    /**
     * 验证操作员是否设置登录密码（个人会员）
     * @param env
     * @param request
     * @return boolean
     */
    public boolean validateLoginPwdIsNull(LoginPasswdRequest req) throws BizException;

}
