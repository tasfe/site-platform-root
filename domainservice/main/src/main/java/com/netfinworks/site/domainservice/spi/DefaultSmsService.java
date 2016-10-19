package com.netfinworks.site.domainservice.spi;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;

/**
 *
 * <p>短信网关</p>
 * @author qinde
 * @version $Id: DefaultSmsService.java, v 0.1 2013-12-4 下午4:13:57 qinde Exp $
 */
public interface DefaultSmsService {

    /**
     *发送验证码
     *
     * @param mobile
     * @return
     * @throws ServiceException
     */
    public boolean sendMessage(AuthCodeRequest req, OperationEnvironment env)
                                                                            throws ServiceException;

    /**
     * 验证用户输入的验证码
     * @param req
     * @param env
     * @return
     * @throws ServiceException
     */
    public boolean verifyMobileAuthCode(AuthCodeRequest req, OperationEnvironment env)
                                                                                      throws ServiceException;

}
