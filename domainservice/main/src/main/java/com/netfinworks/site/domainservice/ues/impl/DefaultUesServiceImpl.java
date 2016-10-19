/**
 *
 */
package com.netfinworks.site.domainservice.ues.impl;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.ues.UesServiceClient;

/**
 * <p>调用UES</p>
 * @author fjl
 * @version $Id: RemoteUesServiceFacade.java 21711 2012-10-25 08:13:29Z fangjilue $
 */
@Service("defaultUesService")
public class DefaultUesServiceImpl implements DefaultUesService {
    private Logger           log = LoggerFactory.getLogger(getClass());

    @Resource(name = "uesService")
    private UesServiceClient uesService;

    @Override
    public String encryptData(String src) throws ServiceException {

        try {
            return uesService.encryptData(src);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public String getDataByTicket(String ticket) throws ServiceException {
        try {
            return uesService.getDataByTicket(ticket);
        }
        catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }
}
