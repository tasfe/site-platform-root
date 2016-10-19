package com.netfinworks.site.web.util;

import com.netfinworks.common.util.money.Money;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.PartyInfo;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.enums.TradeType;

/**
 *
 * <p>交易请求封装</p>
 * @author qinde
 * @version $Id: TradeReqestUtil.java, v 0.1 2013-12-2 下午12:52:12 qinde Exp $
 */
public class TradeReqestUtil {
    /**
     * 交易请求封装
     * @param payer 付款方
     * @param payee 收款方
     * @param money 转账金额
     * @param memo 转账备注
     * @param fee 手续费
     * @param isSendMsg 是否发送短信,Y 发送，N不发送
     * @return
     */
    public static TradeRequestInfo createTransferTradeRequest(PartyInfo payer, PartyInfo payee, Money money,
                                                 String memo, Money fee, String isSendMsg) {
        TradeRequestInfo tradeReqest = new TradeRequestInfo();
        tradeReqest.setPayer(payer);
        tradeReqest.setPayee(payee);
        tradeReqest.setAmount(money);
        tradeReqest.setMemo(memo);
        tradeReqest.setFree(fee);
        tradeReqest.setTradeType(TradeType.TRANSFER);
        if(CommonConstant.TRUE_STRING.equals(isSendMsg)){
            tradeReqest.setSendMessage(true);
        }else{
            tradeReqest.setSendMessage(false);
        }
        return tradeReqest;
    }

    /**
     * 交易请求对象封装
     * @param accountId
     * @param memberId
     * @param operatorId
     * @return
     */
    public static PartyInfo createPay(String accountId, String memberId, String operatorId, String mobile, String memberName, MemberType memberType) {
        PartyInfo pay = new PartyInfo();
        pay.setAccountId(accountId);
        pay.setMemberId(memberId);
        pay.setMemberName(memberName);
        pay.setMobile(mobile);
        pay.setOperatorId(operatorId);
        pay.setMemberType(memberType);
        return pay;
    }
    /**
     * 交易请求对象封装
     * @param accountId
     * @param memberId
     * @param operatorId
     * @return
     */
    public static PartyInfo createPay(String accountId, String memberId, String operatorId, String mobile, String memberName,String email, MemberType memberType) {
        PartyInfo pay = new PartyInfo();
        pay.setAccountId(accountId);
        pay.setMemberId(memberId);
        pay.setMemberName(memberName);
        pay.setMobile(mobile);
        pay.setOperatorId(operatorId);
        pay.setEmail(email);
        pay.setMemberType(memberType);
        return pay;
    }
}
