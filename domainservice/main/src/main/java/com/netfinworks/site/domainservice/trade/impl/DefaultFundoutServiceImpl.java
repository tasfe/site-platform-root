package com.netfinworks.site.domainservice.trade.impl;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.fos.service.facade.domain.FundoutInfoSummary;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.trade.FundoutQuery;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.trade.DefaultFundoutService;
import com.netfinworks.site.ext.integration.fundout.FundoutService;

@Service("defaultFundoutService")
public class DefaultFundoutServiceImpl implements DefaultFundoutService {
    protected Logger       log = LoggerFactory.getLogger(getClass());

    @Resource(name = "fundoutService")
    private FundoutService fundoutService;

    @Override
    public PageResultList queryFundoutInfo(FundoutQuery req, OperationEnvironment env)
                                                                                         throws ServiceException {
        try {
            return fundoutService.queryFundoutInfo(req, env);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

	@Override
	public PageResultList queryFundoutOrderInfo(FundoutQuery req, OperationEnvironment env) throws ServiceException {
		try {
			return fundoutService.queryFundoutOrderInfo(req, env);
		} catch (BizException e) {
			log.error(e.getMessage(), e.getCause());
			throw new ServiceException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public FundoutInfoSummary queryFundoutOrderInfoSummary(FundoutQuery req,
			OperationEnvironment env) throws ServiceException {
		try {
			return fundoutService.queryFundoutOrderInfoSummary(req, env);
		} catch (BizException e) {
			log.error(e.getMessage(), e.getCause());
			throw new ServiceException(e.getMessage(), e.getCause());
		}
	}

	

	

}
