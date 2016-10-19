/**
 *
 */
package com.netfinworks.site.domainservice.trade;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.fos.service.facade.domain.FundoutInfoSummary;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.trade.FundoutQuery;
import com.netfinworks.site.domain.exception.BizException;

/**
 * <p>出款服务</p>
 * @author fjl
 * @version $Id: FundoutService.java, v 0.1 2013-11-29 下午5:37:47 fjl Exp $
 */
public interface DefaultFundoutService {

    /**
     * 查询出款
     * @param req
     * @param env
     * @return
     * @throws BizException
     */
	public PageResultList queryFundoutInfo(FundoutQuery req, OperationEnvironment env) throws ServiceException;

	/**
	 * 查询出款信息(来源于fundout order)
	 * 
	 * @param req
	 * @param env
	 * @return
	 * @throws ServiceException
	 */
	public PageResultList queryFundoutOrderInfo(FundoutQuery req, OperationEnvironment env) throws ServiceException;
	
	 /**
     * 查询出款汇总
     * @param req
     * @param env
     * @return
     * @throws BizException
     */
	public FundoutInfoSummary queryFundoutOrderInfoSummary(FundoutQuery req, OperationEnvironment env) throws ServiceException;
	
	
	

}
