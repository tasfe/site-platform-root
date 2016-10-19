/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.domainservice.spi;

import java.util.Map;

import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.domain.comm.CommResponse;
import com.netfinworks.site.domain.domain.info.PayPasswdCheck;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;

/**
 * 通用说明： 支付密码service
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-20 上午9:17:08
 *
 */
public interface DefaultPayPasswdService {

    /**
     * 设置支付密码
     * @param request
     * @return BankAccountResponse
     */
    public CommResponse setPayPassword(PayPasswdRequest req) throws ServiceException;

    /**
     * 密码解锁
     * @param req
     * @return
     */
    public boolean resetPayPasswordLock(PayPasswdRequest req) throws ServiceException;

    /**
     * 验证支付密码锁定
     * @param env
     * @param request
     * @return boolean
     */
    public boolean isPayPwdClocked(PayPasswdRequest req) throws ServiceException;

    /**
     * 验证支付密码是否设置
     * @param request
     * @return BankAccountResponse
     */
    public boolean isSetPayPwd(PayPasswdRequest req) throws ServiceException;

    /**
     * 验证加盐支付密码
     * @param request
     * @return BankAccountResponse
     */
    public PayPasswdCheck checkPayPwdToSalt(PayPasswdRequest req) throws ServiceException;

    /**
     * 找回支付密码
     * @param front
     * @param back
     * @param authRequest
     * @return
     * @throws ServiceException
     */
    public String payBackPassWord(String front, String back, AuthInfoRequest authRequest)
                                                                                         throws ServiceException;

    /**找回支付密码：（邮件找回：发送邮件）
     * @param mailAddress
     * @return
     * @throws ServiceException
     */
    public boolean sendEmail(String mailAddress, String tplId, Map<String, Object> objParams)
                                                                                             throws ServiceException;
    
	/**
	 * 发送短信
	 * 
	 * @param phoneNum
	 * @param tplId
	 * @param objParams
	 * @return
	 * @throws ServiceException
	 */
	public boolean sendMobileMsg(String phoneNum, String tplId, Map<String, Object> objParams) throws ServiceException;

}
