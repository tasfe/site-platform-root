/**
 * 
 */
package com.netfinworks.site.domainservice.trade;

import com.netfinworks.ma.service.base.model.BankAcctDetailInfo;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domain.exception.BizException;

/**
 * <p>提现服务</p>
 * @author fjl
 * @version $Id: DefaultWithdrawService.java, v 0.1 2013-11-29 下午3:55:02 fjl Exp $
 */
public interface DefaultWithdrawService {
    
    /**
     * 申请提现
     * <li>落地提现原始凭证</li>
     * @param currUser
     * @param env
     * @return 返回凭证号
     */
    public TradeRequestInfo applyWithdraw(BaseMember currUser, TradeType tradeType, TradeEnvironment env) throws BizException ;
    
    /**
     * 提交申请
     * @param req
     * @param bankcardId 提现会员绑定的银行卡
     * @return 成功失败
     */
    public boolean submitApply(TradeRequestInfo req,String bankcardId);
    
    /**
     * 提交提现申请
     * @param req
     * @param bankcardId
     * @return
     * @throws BizException
     */
    public boolean submitWithdraw(TradeRequestInfo req, String bankcardId, TradeEnvironment env) throws BizException;
    
    /**
     * 申请转账凭证
     * @param currUser
     * @param env
     * @return
     * @throws BizException
     */
    public TradeRequestInfo applyTransfer(BaseMember currUser, TradeType tradeType, TradeEnvironment env) throws BizException;
    
    /**
     * 永达互联网金融向银行转账
     * @param req
     * @param bankCard
     * @throws BizException
     */
    public void submitBankTransfer(TradeRequestInfo req, BankAcctDetailInfo bankCard) throws BizException;
}
