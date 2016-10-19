package com.netfinworks.site.ext.integration.trade;

import com.netfinworks.site.domain.domain.response.CoTradeQueryResponse;
import com.netfinworks.site.domain.domain.trade.CoTradeRequest;
import com.netfinworks.site.domain.exception.BizException;

/**
 * 
 * <p>
 * 买卖记录接口
 * </p>
 * 
 * @author zhangyun.m
 * @version $Id: BusinessService.java, v 0.1 2013-11-28 下午5:40:04 Guanxiaoxu Exp
 *          $
 */
public interface SettlementService {
	/**
	 * 查询结算对账单
	 * 
	 * @param req
	 * @return
	 * @throws BizException
	 */
	public CoTradeQueryResponse queryList(CoTradeRequest listReq,
			CoTradeRequest sumReq) throws BizException;

}
