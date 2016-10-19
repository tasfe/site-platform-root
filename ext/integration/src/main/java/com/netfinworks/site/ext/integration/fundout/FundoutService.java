/**
 *
 */
package com.netfinworks.site.ext.integration.fundout;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.fos.service.facade.domain.FundoutInfoSummary;
import com.netfinworks.ma.service.base.model.BankAcctDetailInfo;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.trade.FundoutQuery;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.exception.BizException;

/**
 * <p>出款服务</p>
 * @author fjl
 * @version $Id: FundoutService.java, v 0.1 2013-11-29 下午5:37:47 fjl Exp $
 */
public interface FundoutService {

    /**
     * 查询出款
     * @param req
     * @param env
     * @return
     * @throws BizException
     */
    public PageResultList queryFundoutInfo(FundoutQuery req, OperationEnvironment env)
                                                                                       throws BizException;

	/**
	 * 查询出款信息(来源于fundout order)
	 * 
	 * @param req
	 * @param env
	 * @return
	 * @throws BizException
	 */
	public PageResultList queryFundoutOrderInfo(FundoutQuery req, OperationEnvironment env) throws BizException;
	
	
	/**
	 * 查询出款信息(来源于fundout order)
	 * 
	 * @param req
	 * @param env
	 * @return
	 * @throws BizException
	 */
	public FundoutInfoSummary queryFundoutOrderInfoSummary(FundoutQuery req, OperationEnvironment env) throws BizException;
	

    /**
     * 提交提现申请
     * @param req
     * @param bankCardId 会员绑定的银行卡id
     * @return
     */
    public boolean submitWithdraw(TradeRequestInfo req, String bankCardId) throws BizException;
    
    /**
     * 永达互联网金融向银行转账
     * @param req 交易请求对象
     * @param bankCard 银行卡信息
     * @throws BizException
     */
    public void submitBankTransfer(TradeRequestInfo req, BankAcctDetailInfo bankCard) throws BizException;
}
