/**
 * 
 */
package com.netfinworks.site.ext.integration.cashier;

import java.util.List;

import com.netfinworks.site.domain.domain.trade.BatchTradeRequestInfo;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.exception.BizException;
import com.yongda.site.domain.domain.cashier.PayLimit;

/**
 * <p>收银台</p>
 * @author fjl
 * @version $Id: CashierService.java, v 0.1 2013-11-28 下午5:59:12 fjl Exp $
 */
public interface CashierService {
    
    /**
     * 获取收银台地址
     * @param req
     * @return
     * @throws BizException
     */
    public String applyCashierUrl(TradeRequestInfo req) throws BizException;
    
    public String applyCashierUrl(TradeRequestInfo req,String accessChannel) throws BizException;
    
    /**
     * 获取收银台地址
     * @param req
     * @return
     * @throws BizException
     */
    public String applyCashierUrl(BatchTradeRequestInfo req) throws BizException;
    
    
    public List<PayLimit>  queryInstPayLimit(String instCode,String payChannel)throws BizException;
}
