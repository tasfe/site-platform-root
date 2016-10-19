/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.ext.integration.member;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.netfinworks.site.domain.domain.comm.CommResponse;
import com.netfinworks.site.domain.domain.info.PayPasswdCheck;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.domain.response.UsfUploadFileResponse;
import com.netfinworks.site.domain.exception.BizException;

/**
 * 通用说明：会员远程调用接口
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-12 下午3:53:51
 *
 */
public interface PayPasswdService {

    /**
     * 设置支付密码
     * @param env
     * @param request
     * @return BankAccountResponse
     */
    public CommResponse setPayPassword(PayPasswdRequest req) throws BizException;

    /**
     * 密码解锁
     * @param req
     * @return
     */
    public boolean resetPayPasswordLock(PayPasswdRequest req) throws BizException;

    /**
     * 验证加盐支付密码
     * @param env
     * @param request
     * @return boolean
     */
    public PayPasswdCheck checkPayPwdToSalt(PayPasswdRequest req) throws BizException;

    /**
     * 验证支付密码是否设置与锁定
     * @param env
     * @param request
     * @return boolean
     */
    public boolean validatePayPwd(PayPasswdRequest req) throws BizException;

    /**
     * 发送找回支付密码的信息
     * @param request
     * @param env
     * @return
     * @throws BizException
     */
    public String payBackPassWord(AuthInfoRequest request) throws BizException;

    /**
     * 上传文件至统一文件系统
     * @param ctx
     * @return
     * @throws BizException
     */
    public UsfUploadFileResponse saveFile(MultipartFile front, String frontFileName)
                                                                                    throws BizException;

    /**找回支付密码（邮件找回：发送邮件）
     * @param mailAddress
     * @return
     * @throws BizException
     */
    public boolean sendEmail(String mailAddress, String tplId, Map<String, Object> objParams)
                                                                                             throws BizException;
    
	/**
	 * 发送短信
	 * 
	 * @param phoneNum
	 * @param tplId
	 * @param objParams
	 * @return
	 * @throws BizException
	 */
	public boolean sendMobileMsg(String phoneNum, String tplId, Map<String, Object> objParams) throws BizException;
}
