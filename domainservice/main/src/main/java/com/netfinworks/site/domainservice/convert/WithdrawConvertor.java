/**
 * 
 */
package com.netfinworks.site.domainservice.convert;

import org.springframework.beans.BeanUtils;

import com.netfinworks.site.domain.domain.info.PartyInfo;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.TradeType;

/**
 * <p>提现请求对象转换</p>
 * @author fjl
 * @version $Id: WithdrawConvertor.java, v 0.1 2013-11-29 下午4:15:16 fjl Exp $
 */
public class WithdrawConvertor {

    /**
     * 转换成交易请求对象
     * @param currUser
     * @param env
     * @return
     */
    public static TradeRequestInfo convertWithdrawApply(BaseMember currUser, TradeType tradeType, TradeEnvironment env){
        TradeRequestInfo req = new TradeRequestInfo();
        
        BeanUtils.copyProperties(env, req);
        PartyInfo payer = new PartyInfo();
        payer.setMemberId(currUser.getMemberId());
        payer.setAccountId(currUser.getDefaultAccountId());
        payer.setOperatorId(currUser.getOperatorId());
        payer.setMemberType(currUser.getMemberType());
        req.setPayer(payer);
        req.setTradeType(tradeType);
        return req;
    }
}
