/**
 * 
 */
package com.netfinworks.site.domainservice.convert;

import java.util.List;

import org.springframework.beans.BeanUtils;

import com.netfinworks.site.domain.domain.info.PartyInfo;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.trade.BatchTradeRequestInfo;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.AcqTradeType;
import com.netfinworks.site.domain.enums.TradeType;

/**
 * <p>充值请求对象转换</p>
 * @author fjl
 * @version $Id: RechargeConverter.java, v 0.1 2013-11-28 下午5:31:45 fjl Exp $
 */
public class RechargeConvertor {

    /**
     * 转换成交易请求对象
     * @param currUser
     * @param env
     * @return
     */
    public static TradeRequestInfo convertRechargeReq(BaseMember currUser,TradeEnvironment env){
        TradeRequestInfo req = new TradeRequestInfo();
        
        BeanUtils.copyProperties(env, req);
        PartyInfo payer = new PartyInfo();
        payer.setMemberId(currUser.getMemberId());
        payer.setAccountId(currUser.getDefaultAccountId());
        payer.setOperatorId(currUser.getOperatorId());
        payer.setMemberType(currUser.getMemberType());
        req.setPayer(payer);
        req.setPayee(payer);
        req.setTradeType(TradeType.RECHARGE);
        return req;
    }
    
    /**
     * 转换成批量付款交易请求对象
     * @param currUser
     * @param tradeVoucherNo
     * @param env
     * @return
     */
    public static BatchTradeRequestInfo convertPaymentTradeReq(BaseMember currUser,List<String> tradeVoucherNo,AcqTradeType acqTradeType,TradeEnvironment env){
        BatchTradeRequestInfo req = new BatchTradeRequestInfo();
        
        BeanUtils.copyProperties(env, req);
        PartyInfo payer = new PartyInfo();
        payer.setMemberId(currUser.getMemberId());
        payer.setAccountId(currUser.getDefaultAccountId());
        payer.setOperatorId(currUser.getOperatorId());
        payer.setMemberType(currUser.getMemberType());
        req.setPayer(payer);
        req.setExtension(env.getExtension());
        if(AcqTradeType.INSTANT_TRASFER.equals(acqTradeType)){
            req.setTradeType(TradeType.TRANSFER);
        }else{
            req.setTradeType(TradeType.PAYMENT);
        }
        req.setTradeVoucherNo(tradeVoucherNo);
        return req;
    }
}
