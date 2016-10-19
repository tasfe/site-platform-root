package com.netfinworks.site.domainservice.spi.impl;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.ext.integration.smsgateway.SmsService;

/**
 *
 * <p>短信网关</p>
 * @author qinde
 * @version $Id: DefaultSmsServiceImpl.java, v 0.1 2013-12-4 下午4:15:34 qinde Exp $
 */
@Service("defaultSmsService")
public class DefaultSmsServiceImpl implements DefaultSmsService {
    protected Logger   log = LoggerFactory.getLogger(getClass());

    @Resource(name = "smsService")
    private SmsService smsService;

    @Override
    public boolean sendMessage(AuthCodeRequest req, OperationEnvironment env)
                                                                            throws ServiceException {
        try {
            return smsService.sendAuthCode(req, env);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }
    @Override
    public boolean verifyMobileAuthCode(AuthCodeRequest req, OperationEnvironment env)
            throws ServiceException {
        try {
            return smsService.verifyMobileAuthCode(req, env);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

}
