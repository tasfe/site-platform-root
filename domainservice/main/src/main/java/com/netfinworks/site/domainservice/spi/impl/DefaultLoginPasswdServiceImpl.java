package com.netfinworks.site.domainservice.spi.impl;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.domain.comm.CommResponse;
import com.netfinworks.site.domain.domain.info.LoginPasswdCheck;
import com.netfinworks.site.domain.domain.request.LoginPasswdRequest;
import com.netfinworks.site.domain.domain.request.OperatorLoginPasswdRequest;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.spi.DefaultLoginPasswdService;
import com.netfinworks.site.ext.integration.member.LoginPasswdService;

/**
 * <p>登录密码接口实现</p>
 * @author zhangyun.m
 * @version $Id: LoginPasswdServiceImpl.java, v 0.1 2014年5月20日 下午5:01:57 zhangyun.m Exp $
 */
@Service("defaultLoginPasswdService")
public class DefaultLoginPasswdServiceImpl implements DefaultLoginPasswdService {

    protected Logger           log = LoggerFactory.getLogger(getClass());

    @Resource(name = "loginPasswdService")
    private LoginPasswdService loginPasswdService;

    /* (non-Javadoc)设置登录密码
     * @see com.netfinworks.site.domainservice.spi.DefaultLoginPasswdService#setLoginPassword(com.netfinworks.site.domain.domain.request.LoginPasswdRequest)
     */
    @Override
    public CommResponse setLoginPassword(LoginPasswdRequest req) throws ServiceException {
        try {
            return loginPasswdService.setLoginPassword(req);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public boolean resetLoginPasswordLock(LoginPasswdRequest req) throws ServiceException {
        try {
            return loginPasswdService.resetLoginPasswordLock(req);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

    /* (non-Javadoc)验证登录密码是否被锁定(个人会员)
     * @see com.netfinworks.site.domainservice.spi.DefaultLoginPasswdService#isLoginPwdClocked(com.netfinworks.site.domain.domain.request.LoginPasswdRequest)
     */
    @Override
    public boolean isLoginPwdClocked(LoginPasswdRequest req) throws ServiceException {
        try {
            req.setValidateMode(2);
            return loginPasswdService.validateLoginPwd(req);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

    /* (non-Javadoc)
     * @see com.netfinworks.site.domainservice.spi.DefaultLoginPasswdService#checkPersonalLoginPasswd(com.netfinworks.site.domain.domain.request.LoginPasswdRequest)
     */
    @Override
    public LoginPasswdCheck checkPersonalLoginPasswd(LoginPasswdRequest req)
                                                                            throws ServiceException {
        try {
            return loginPasswdService.checkLoginPwdToSalt(req);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

    /* (non-Javadoc)企业会员
     * @see com.netfinworks.site.domainservice.spi.DefaultLoginPasswdService#checkEntLoginPasswd(com.netfinworks.site.domain.domain.request.OperatorLoginPasswdRequest)
     */
    @Override
    public LoginPasswdCheck checkEntLoginPasswd(OperatorLoginPasswdRequest req)
                                                                               throws ServiceException {
        try {
            return loginPasswdService.checkEntLoginPwdToSalt(req);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

    /* (non-Javadoc)验证登录密码是否被锁定(企业会员)
     * @see com.netfinworks.site.domainservice.spi.DefaultLoginPasswdService#isLoginPwdClockedEnt(com.netfinworks.site.domain.domain.request.OperatorLoginPasswdRequest)
     */
    @Override
    public boolean isLoginPwdClockedEnt(OperatorLoginPasswdRequest req) throws ServiceException {
        try {
            req.setValidateMode(2);
            return loginPasswdService.validateLoginPwdEnt(req);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

    /* (non-Javadoc)重置企业会员登录密码
     * @see com.netfinworks.site.domainservice.spi.DefaultLoginPasswdService#setLoginPasswordEnt(com.netfinworks.site.domain.domain.request.OperatorLoginPasswdRequest)
     */
    @Override
    public CommResponse setLoginPasswordEnt(OperatorLoginPasswdRequest req) throws ServiceException {
        try {
            return loginPasswdService.setLoginPasswordEnt(req);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

}
