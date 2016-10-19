/**
 *
 */
package com.netfinworks.site.domainservice.trade.impl;

import java.util.List;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.trade.BatchTradeRequestInfo;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.enums.AcqTradeType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.convert.RechargeConvertor;
import com.netfinworks.site.domainservice.trade.DefaultPaymentService;
import com.netfinworks.site.ext.integration.cashier.CashierService;

/**
 * <p>钱包支付服务</p>
 * @author fjl
 * @version $Id: DefaultPaymentServiceImpl.java, v 0.1 2013-12-4 下午3:01:41 fjl Exp $
 */
@Service("defaultPaymentService")
public class DefaultPaymentServiceImpl implements DefaultPaymentService {
    protected Logger       log = LoggerFactory.getLogger(getClass());

    @Resource
    private CashierService cashierService;

    @Override
    public String applyPayment(BaseMember currUser, List<String> tradeVoucherNo,
                               AcqTradeType acqTradeType, TradeEnvironment env) throws ServiceException {
        try {
            BatchTradeRequestInfo req = RechargeConvertor.convertPaymentTradeReq(currUser,
                tradeVoucherNo, acqTradeType, env);
            //请求地址
            return cashierService.applyCashierUrl(req);
        } catch (BizException e) {
            log.error(e.getMessage(), e.getCause());
            throw new ServiceException(e.getMessage(), e.getCause());
        }
    }

}
