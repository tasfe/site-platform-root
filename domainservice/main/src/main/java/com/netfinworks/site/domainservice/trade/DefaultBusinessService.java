package com.netfinworks.site.domainservice.trade;

import java.util.List;

import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.domain.info.BusinessInfo;
import com.netfinworks.site.domain.domain.trade.BusinessRequest;


/**
 * 
 * <p>
 * 买卖记录接口
 * </p>
 * 
 * @author Guan Xiaoxu
 * @version $Id: BusinessService.java, v 0.1 2013-11-28 下午5:40:04 Guanxiaoxu Exp
 *          $
 */
public interface DefaultBusinessService {
	
    /**
     * 查询 买卖记录信息
     * @param req
     * @return
     * @throws ServiceException
     */
	public List<BusinessInfo> queryBusinessInfoList(BusinessRequest req) throws ServiceException;

}
