/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.ext.integration.member.impl;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.netfinworks.cert.service.facade.ICertFacade;
import com.netfinworks.cert.service.request.AuthRequest;
import com.netfinworks.cert.service.response.AuthResponse;
import com.netfinworks.ma.service.base.response.Response;
import com.netfinworks.ma.service.facade.IPayPwdFacade;
import com.netfinworks.ma.service.request.PayPasswordRequest;
import com.netfinworks.ma.service.request.PayPwdCheckRequest;
import com.netfinworks.ma.service.request.PayPwdLockRequest;
import com.netfinworks.ma.service.request.ValidatePayPwdRequest;
import com.netfinworks.ma.service.response.PayPwdCheckResponse;
import com.netfinworks.ma.service.response.ValidatePayPwdResponse;
import com.netfinworks.mns.client.INotifyClient;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.comm.CommResponse;
import com.netfinworks.site.domain.domain.info.PayPasswdCheck;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.site.domain.domain.request.PayPasswdRequest;
import com.netfinworks.site.domain.domain.response.UsfUploadFileResponse;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.ClientEnvironment;
import com.netfinworks.site.ext.integration.comm.convert.CommResponseConvert;
import com.netfinworks.site.ext.integration.member.PayPasswdService;
import com.netfinworks.site.ext.integration.member.convert.PaypasswdConvert;
import com.netfinworks.ufs.client.UFSClient;
import com.netfinworks.ufs.client.ctx.InputStreamFileContext;
import com.netfinworks.ufs.client.exception.CallFailException;

/**
 * 通用说明：支付密码接口
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-12 下午3:54:27
 *
 */
@Service("payPasswdService")
public class PayPasswdServiceImpl extends ClientEnvironment implements PayPasswdService {
    private Logger        logger = LoggerFactory.getLogger(PayPasswdServiceImpl.class);

    @Resource(name = "payPwdFacade")
    private IPayPwdFacade payPwdFacade;
    @Resource(name = "ufsClient")
    private UFSClient     ufsClient;
    @Resource(name = "certFacade")
    private ICertFacade   certFacade;

    @Resource(name = "mnsClient")
    private INotifyClient mnsClient;

    /**
     * 设置支付密码
     * @param env
     * @param request
     * @return BankAccountResponse
     */
    @Override
    public CommResponse setPayPassword(PayPasswdRequest req) throws BizException {
        if (logger.isInfoEnabled()) {
            logger.info("设置支付密码requst参数:" + req.toString());
        }
        PayPasswordRequest request = PaypasswdConvert.createPayPasswordRequest(req);
        Response response = payPwdFacade.setPayPassword(getEnv(req.getClientIp()), request);
        CommResponse commRep = CommResponseConvert.commResponseConvert(response);
        if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
            if (logger.isInfoEnabled()) {
				logger.info("设置支付密码成功：信息：" + response.getResponseMessage());
			}
            commRep.setSuccess(true);
            return commRep;
        }else {
            String error = "设置支付密码失败,返回信息：" + response.getResponseMessage();
            logger.error(error);
            commRep.setSuccess(false);
            if(ResponseCode.PASSWORD_EQUAL_LOGIN_PASSWORD.getCode().equals(response.getResponseCode())){
				commRep.setResponseMessage(ResponseCode.PASSWORD_EQUAL_LOGIN_PASSWORD.getMessage());
                return commRep;
            }else{
                throw new BizException(ErrorCode.SYSTEM_ERROR, error);
            }
        }
    }

    /**
     * 验证支付密码是否设置与锁定
     * @param env
     * @param request
     * @return boolean
     */
    @Override
    public boolean validatePayPwd(PayPasswdRequest req) throws BizException {
        ValidatePayPwdRequest request = PaypasswdConvert.createValidatePayPwdRequest(req);
        ValidatePayPwdResponse response = payPwdFacade.validatePayPwd(getEnv(req.getClientIp()),
            request);
        if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
            return response.getIsLock() == 1;
        } else {
            String error = "验证支付密码失败：信息：" + response.getResponseMessage();
            logger.error(error);
            throw new BizException(ErrorCode.SYSTEM_ERROR, error);
        }
    }

    /**
     * 验证加盐支付密码
     * @param env
     * @param request
     * @return BankAccountResponse
     */
    @Override
    public PayPasswdCheck checkPayPwdToSalt(PayPasswdRequest req) throws BizException {
        try {
            long beginTime = 0L;
            if (logger.isInfoEnabled()) {
                logger.info("远程验证加盐支付密码，请求参数：{}", req);
                beginTime = System.currentTimeMillis();
            }
            PayPwdCheckRequest request = PaypasswdConvert.createPayPwdCheckRequest(req);
            PayPwdCheckResponse response = payPwdFacade.checkPayPwdToSalt(
                getEnv(req.getClientIp()), request);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("远程验证加盐支付密码， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime,
                        response });
            }
            PayPasswdCheck result = new PayPasswdCheck();
            if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
                result.setSuccess(true);
                if (logger.isInfoEnabled()) {
					logger.info("远程验证加盐支付密码: 成功");
				}
                return result;
            } else {
                result.setSuccess(false);
                if (CommonConstant.PAYPASSWD_LOCKED.equals(response.getResponseCode())) {
                    result.setLocked(true);
                    result.setRemainNum(response.getRemainNum().intValue());
                } else if (CommonConstant.REMAINNUM.equals(response.getResponseCode())) {
                    result.setRemainNum(response.getRemainNum().intValue());
                }
                if (logger.isInfoEnabled()) {
					logger.info("远程验证加盐支付密码: 支付密码被锁定了");
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

    @Override
    public UsfUploadFileResponse saveFile(MultipartFile front, String frontFileName)
                                                                                    throws BizException {
        try {
            InputStreamFileContext ctx = PaypasswdConvert.createFileFileContext(front,
                frontFileName);
            boolean success = ufsClient.putFile(ctx);
            String url = ctx.getDownloadUrl();
            return new UsfUploadFileResponse(success, url);
        } catch (CallFailException e) {
            logger.error(e.getMessage(), e.getCause(), e);
            throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage(), e.getCause(), e);
            throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        } catch (Exception e) {
            logger.error("上传文件{}失败", frontFileName, e);
            throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }

    @Override
    public String payBackPassWord(AuthInfoRequest info) throws BizException {
        AuthRequest request = PaypasswdConvert.createPayBackPassWordRequest(info);
        AuthResponse response = certFacade.authApply(request, getEnv(info.getClientIp()));
        if (ResponseCode.SUCCESS.getCode().equals(response.getResultCode())) {
            return response.getOrderNo();
        } else {
            throw new BizException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Override
    public boolean resetPayPasswordLock(PayPasswdRequest req) throws BizException {
        PayPwdLockRequest request = PaypasswdConvert.createPayPwdLockRequest(req);
        Response response = payPwdFacade.resetPayPasswordLock(getEnv(req.getClientIp()), request);
        if (ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean sendEmail(String mailAddress, String tplId, Map<String, Object> objParams)throws BizException {
        com.netfinworks.mns.client.Response response = mnsClient.sendMail(mailAddress, tplId,
            objParams);
        if (response.isSuccess()) {
            return true;
        } else {
            return false;
        }
    }

	@Override
	public boolean sendMobileMsg(String phoneNum, String tplId, Map<String, Object> objParams) throws BizException {
		com.netfinworks.mns.client.Response response = mnsClient.sendMobileMsg(phoneNum, tplId, objParams);
		if (response.isSuccess()) {
			return true;
		} else {
			return false;
		}
	}

}
