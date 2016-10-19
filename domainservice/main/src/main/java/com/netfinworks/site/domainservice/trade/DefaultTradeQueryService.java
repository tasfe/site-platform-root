/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.domainservice.trade;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.trade.TradeRequest;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.tradeservice.facade.response.TradeDetailQueryResponse;

/**
 * 通用说明：交易信息服务
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-12 上午9:17:08
 *
 */
public interface DefaultTradeQueryService {

    /**
     * 查询 个人钱包会员交易信息
     * @param req
     * @param env
     * @return
     * @throws BizException
     */
    public PageResultList queryTradeList(TradeRequest req, OperationEnvironment env)
                                                                                    throws ServiceException;

    /**
     * 退出交易查询
     * @param req
     * @param env
     * @return
     * @throws BizException
     */
    public PageResultList queryRefundList(TradeRequest req, OperationEnvironment env)
                                                                                     throws ServiceException;

    public TradeDetailQueryResponse queryDetail(String paramString,
                                                OperationEnvironment paramOperationEnvironment)
                                                                                               throws ServiceException;
}
