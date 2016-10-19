package com.netfinworks.site.domainservice.spi;

import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.domain.comm.CommResponse;
import com.netfinworks.site.domain.domain.info.LoginPasswdCheck;
import com.netfinworks.site.domain.domain.request.LoginPasswdRequest;
import com.netfinworks.site.domain.domain.request.OperatorLoginPasswdRequest;

/**
 * <p>登录密码接口</p>
 * @author zhangyun.m
 * @version $Id: LoginPasswdService.java, v 0.1 2014年5月20日 下午3:52:12 zhangyun.m Exp $
 */
public interface DefaultLoginPasswdService {

    /**
     * 设置登陆密码
     * @param request
     * @return BankAccountResponse
     */
    public CommResponse setLoginPassword(LoginPasswdRequest req) throws ServiceException;

    /**设置登录密码（企业会员）
     * @param req
     * @return
     * @throws ServiceException
     */
    public CommResponse setLoginPasswordEnt(OperatorLoginPasswdRequest req) throws ServiceException;

    /**
     * 登录密码解锁
     * @param req
     * @return
     */
    public boolean resetLoginPasswordLock(LoginPasswdRequest req) throws ServiceException;

    /**验证登录密码是否锁定(个人会员)
     * @param req
     * @return
     * @throws ServiceException
     */
    public boolean isLoginPwdClocked(LoginPasswdRequest req) throws ServiceException;

    /**验证登录密码是否锁定（企业会员）
     * @param req
     * @return
     * @throws ServiceException
     */
    public boolean isLoginPwdClockedEnt(OperatorLoginPasswdRequest req) throws ServiceException;

    /**验证用户输入的登录密码是否和设定的登录密码相同(个人会员)
     * @param req
     * @return
     * @throws ServiceException
     */
    public LoginPasswdCheck checkPersonalLoginPasswd(LoginPasswdRequest req)
                                                                            throws ServiceException;

    /**验证用户输入的登录密码是否和设定的登录密码相同(企业人会员)
     * @param req
     * @return
     * @throws ServiceException
     */
    public LoginPasswdCheck checkEntLoginPasswd(OperatorLoginPasswdRequest req)
                                                                               throws ServiceException;

}
