/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.domainservice.trade.impl;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.trade.TradeRequest;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.trade.DefaultTradeQueryService;
import com.netfinworks.site.ext.integration.trade.TradeService;
import com.netfinworks.tradeservice.facade.response.TradeDetailQueryResponse;

/**
 * 通用说明：
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-13 下午6:14:22
 *
 */
@Service("defaultTradeQueryService")
public class DefaultTradeQueryServiceImpl implements DefaultTradeQueryService {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Resource(name = "tradeService")
    private TradeService tradeService;

    /**
     * 查询交易基本信息
     * @param paramTradeBasicQueryRequset
     * @param paramOperationEnvironment
     * @return
     */
    @Override
    public PageResultList queryTradeList(TradeRequest req, OperationEnvironment env ) throws ServiceException {
        try {
            return tradeService.queryTradeList(req, env);
        } catch (BizException e) {
            logger.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 退出交易查询
     * @param req
     * @param env
     * @return
     * @throws BizException
     */
    public PageResultList queryRefundList(TradeRequest req, OperationEnvironment env)
                                                                                     throws ServiceException {
        try {
            return tradeService.queryRefundList(req, env);
        } catch (BizException e) {
            logger.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }
    /**
     * 查询交易明细
     * @param paramString
     * @param paramOperationEnvironment
     * @return
     */
    @Override
    public TradeDetailQueryResponse queryDetail(String paramString,
                                                OperationEnvironment paramOperationEnvironment)
                                                                                               throws ServiceException {
        return tradeService.queryDetail(paramString, paramOperationEnvironment);
    }
}
