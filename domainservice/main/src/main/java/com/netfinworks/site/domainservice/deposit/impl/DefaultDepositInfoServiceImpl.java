package com.netfinworks.site.domainservice.deposit.impl;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.deposit.api.domain.DepositInfo;
import com.netfinworks.deposit.api.domain.DepositListRequest;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.domain.info.DepositBasicInfo;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.deposit.DefaultDepositInfoService;
import com.netfinworks.site.ext.integration.deposit.DepositInfoService;

@Service("defaultDepositInfoService")
public class DefaultDepositInfoServiceImpl implements DefaultDepositInfoService {
    protected Logger           log = LoggerFactory.getLogger(getClass());

    @Resource(name = "depositInfoService")
    private DepositInfoService depositInfoService;

    @Override
    public PageResultList<DepositBasicInfo> queryList(DepositListRequest req,
                                       OperationEnvironment paramOperationEnvironment)
                                                                                      throws ServiceException {
        try {
            return depositInfoService.queryList(req, paramOperationEnvironment);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

}
