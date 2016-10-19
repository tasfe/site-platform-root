package com.netfinworks.site.ext.integration.smsgateway;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.exception.BizException;

/**
 *
 * <p>短信网关</p>
 * @author qinde
 * @version $Id: MemberService.java, v 0.1 2013-12-4 下午3:37:38 qinde Exp $
 */
public interface SmsService {

    /**
     * 发送验证码
     * @param req
     * @param env
     * @return
     * @throws BizException
     */
    public boolean sendAuthCode(AuthCodeRequest req, OperationEnvironment env) throws BizException;

    /**
     * 验证手机验证码
     * @param req
     * @param env
     * @return
     * @throws BizException
     */
    public boolean verifyMobileAuthCode(AuthCodeRequest req, OperationEnvironment env)
                                                                                      throws BizException;

}
