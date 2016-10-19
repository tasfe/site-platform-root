/**
 *
 */
package com.netfinworks.site.domainservice.trade;

import java.util.List;

import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.enums.AcqTradeType;

/**
 * <p>钱包支付服务</p>
 * @author fjl
 * @version $Id: DefaultPaymentService.java, v 0.1 2013-12-4 下午1:29:39 fjl Exp $
 */
public interface DefaultPaymentService {

    /**
     * 申请付款
     * @param currUser 当前会员
     * @param tradeVoucherNo 未完成的交易凭证号
     * @param env
     * @return 收银台地址
     */
    public String applyPayment(BaseMember currUser,List<String> tradeVoucherNo,AcqTradeType acqTradeType, TradeEnvironment env) throws ServiceException;
}
