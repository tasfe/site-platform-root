/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.ext.integration.member.convert;

import com.netfinworks.ma.service.request.LoginPwdRequest;
import com.netfinworks.ma.service.request.OperatorLoginPwdByIdRequest;
import com.netfinworks.ma.service.request.PersonalLoginPwdLockRequest;
import com.netfinworks.ma.service.request.PersonalLoginPwdRequest;
import com.netfinworks.ma.service.request.ValidateLoginPwdRequest;
import com.netfinworks.site.core.common.util.RadomUtil;
import com.netfinworks.site.domain.domain.request.LoginPasswdRequest;
import com.netfinworks.site.domain.domain.request.OperatorLoginPasswdRequest;
import com.netfinworks.ues.util.UesUtils;


/**
 * <p>登录密码convert</p>
 * @author zhangyun.m
 * @version $Id: LoginPasswdConvert.java, v 0.1 2014年5月20日 下午5:26:46 zhangyun.m Exp $
 */
public class LoginPasswdConvert {

    /**
     * 创建设置登录密码的request（个人会员）
     * @return
     */
    public static LoginPwdRequest createLoginPasswordRequest(LoginPasswdRequest req) {
        String newPasswd = UesUtils.hashSignContent(req.getPassword());
        LoginPwdRequest request = new LoginPwdRequest();
        request.setOperatorId(req.getOperatorId());// 操作员编号
        request.setPassword(newPasswd);// 支付密码: SHA-256(密码明文）
        return request;
    }
    /**
     * 创建设置登录密码的request(企业会员)
     * @return
     */
    public static LoginPwdRequest createEntLoginPasswordRequest(OperatorLoginPasswdRequest req) {
        String newPasswd = UesUtils.hashSignContent(req.getPassword());
        LoginPwdRequest request = new LoginPwdRequest();
        request.setOperatorId(req.getOperatorId());// 操作员编号
        request.setPassword(newPasswd);// 支付密码: SHA-256(密码明文）
        return request;
    }

    /**创建查询交易密码状态的request（个人会员）
     * @param req
     * @return
     */
    public static ValidateLoginPwdRequest createValidateLoginPwdRequest(LoginPasswdRequest req) {
        ValidateLoginPwdRequest request = new ValidateLoginPwdRequest();
        request.setOperatorId(req.getOperatorId());
        request.setValidateMode(req.getValidateMode());
        return request;
    }
    
    /**创建查询交易密码状态的request （企业会员）
     * @param req
     * @return
     */
    public static ValidateLoginPwdRequest createValidateLoginPwdRequest(OperatorLoginPasswdRequest req) {
        ValidateLoginPwdRequest request = new ValidateLoginPwdRequest();
        request.setOperatorId(req.getOperatorId());
        request.setValidateMode(req.getValidateMode());
        return request;
    }

    /**
     * 加盐原登录密码request（个人会员）
     *
     * @return
     */
    public static PersonalLoginPwdRequest createLoginPwdCheckRequest(LoginPasswdRequest req) {
        String passwd = null;
        if(req.getValidateType() == 1) {
            passwd = req.getOldPassword();
        } else {
            passwd = req.getPassword();
        }
        long salt = RadomUtil.createRadom();
        String saltStr = String.valueOf(salt);
        //加密登录密码
        String saltPasswd = UesUtils.hashSignContent(UesUtils.hashSignContent(passwd)
                                                     + saltStr);
        PersonalLoginPwdRequest request = new PersonalLoginPwdRequest();
        request.setPlatformType(req.getPlatformType());
        request.setMemberIdentity(req.getMemberIdentity());
        request.setPassword(saltPasswd);
        request.setSalt(saltStr);      
        
        return request;
    }
    
    /**企业会员（操作员）请求转换（加盐企业会员原登录密码）
     * @param req
     * @return
     */
    public static OperatorLoginPwdByIdRequest createEntLoginPwdCheckRequest(OperatorLoginPasswdRequest req) {
        String passwd = null;
        if(req.getValidateType() == 1) {
            passwd = req.getOldPassword();
        } else {
            passwd = req.getPassword();
        }
        long salt = RadomUtil.createRadom();
        String saltStr = String.valueOf(salt);
        //加密登录密码
        String saltPasswd = UesUtils.hashSignContent(UesUtils.hashSignContent(passwd)
                                                     + saltStr);
        OperatorLoginPwdByIdRequest request = new OperatorLoginPwdByIdRequest();
        request.setOperatorId(req.getOperatorId());
        request.setPassword(saltPasswd);
        request.setSalt(saltStr);
        
        return request;
    }
    
    /**
     * 密码解锁request
     *
     * @return
     */
    public static PersonalLoginPwdLockRequest createLoginPwdLockRequest(LoginPasswdRequest req) {
        PersonalLoginPwdLockRequest request = new PersonalLoginPwdLockRequest();
        request.setMemberIdentity(req.getMemberIdentity());
        request.setPlatformType(req.getPlatformType());
        return request;
    }

    

}
