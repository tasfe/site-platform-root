package com.netfinworks.site.ext.integration.smsgateway.impl;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.ClientEnvironment;
import com.netfinworks.site.ext.integration.smsgateway.SmsService;
import com.netfinworks.site.ext.integration.smsgateway.convert.SmsConvert;
import com.netfinworks.smsgateway.service.facade.api.IAuthCodeFacade;
import com.netfinworks.smsgateway.service.facade.domain.request.SendAuthCodeRequest;
import com.netfinworks.smsgateway.service.facade.domain.request.VerifyMobileAuthCodeRequest;
import com.netfinworks.smsgateway.service.facade.domain.response.SendAuthCodeResponse;
import com.netfinworks.smsgateway.service.facade.domain.response.VerifyMobileAuthCodeResponse;

/**
 *
 * <p>短信网关远程服务</p>
 * @author qinde
 * @version $Id: MemberServiceImpl.java, v 0.1 2013-12-4 下午3:38:10 qinde Exp $
 */
@Service("smsService")
public class SmsServiceImpl extends ClientEnvironment implements SmsService {

    private Logger          logger = LoggerFactory.getLogger(SmsServiceImpl.class);

    @Resource(name = "authCodeFacade")
    private IAuthCodeFacade authCodeFacade;

    /**
     * 发送验证码
     *
     * @param paramSendAuthCodeRequest
     * @param env
     * @return
     * @throws BizException
     */
    @Override
    public boolean sendAuthCode(AuthCodeRequest req, OperationEnvironment env) throws BizException {

        try {
            long beginTime = 0L;
            if (logger.isInfoEnabled()) {
                logger.info("发送验证码，请求参数：{}", req);
                beginTime = System.currentTimeMillis();
            }
            SendAuthCodeRequest request = SmsConvert.createSendAuthCodeRequest(req);
            SendAuthCodeResponse response = authCodeFacade.sendAuthCode(request, env);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger
                    .info("远程发送验证码， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
            }
            if (ResponseCode.NEW_SUCCESS.getCode().equals(response.getResultCode())) {
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error("发送验证码 {}信息异常:请求信息", req.getMobile(), e);
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
        }
    }

    /**
     * 验证用户输入的验证码
     *
     * @param paramSendAuthCodeRequest
     * @param env
     * @return
     * @throws BizException
     */
    @Override
    public boolean verifyMobileAuthCode(AuthCodeRequest req, OperationEnvironment env)
                                                                                      throws BizException {

        try {
            long beginTime = 0L;
            if (logger.isInfoEnabled()) {
                logger.info("验证手机验证码，请求参数：{}", req);
                beginTime = System.currentTimeMillis();
            }
            VerifyMobileAuthCodeRequest request = SmsConvert.createVerifyMobileAuthCodeRequest(req);
            VerifyMobileAuthCodeResponse response = authCodeFacade.verifyMobileAuthCode(request,
                env);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("远程验证手机验证， 耗时:{} (ms); 响应结果:{} ",
                    new Object[] { consumeTime, response });
            }
            if (ResponseCode.NEW_SUCCESS.getCode().equals(response.getResultCode())) {
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            logger.error("验证手机验证 手机号： {}信息异常:请求信息", req.getMobile(), e);
            throw new BizException(ErrorCode.SYSTEM_ERROR);
        }
    }

}
