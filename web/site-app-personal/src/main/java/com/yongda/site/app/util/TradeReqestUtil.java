package com.yongda.site.app.util;

import com.netfinworks.common.util.money.Money;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.convert.ObjectConvert;
import com.netfinworks.site.domain.domain.info.PartyInfo;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.enums.TradeType;
import com.yongda.site.app.common.vo.Transfer;

/**
 *
 * <p>交易请求封装</p>
 * @author qinde
 * @autohr tangL
 * @version $Id: TradeReqestUtil.java, v 0.1 2013-12-2 下午12:52:12 qinde Exp $
 */
public class TradeReqestUtil {
	/**
	 * 会员对象转换成 交易参与方对象
	 */
	public static final ObjectConvert<BaseMember, PartyInfo> memberToPartyInfoConvert = new ObjectConvert<BaseMember, PartyInfo>(){
		@Override
		public PartyInfo convert(BaseMember source) {
			PartyInfo pay = new PartyInfo();
	        pay.setAccountId(source.getDefaultAccountId());
	        pay.setMemberId(source.getMemberId());
	        pay.setMemberName(source.getMemberName());
	        pay.setMobile(source.getMobile());
	        pay.setOperatorId(source.getOperatorId());
	        pay.setMemberType(source.getMemberType());
			return pay;
		}
	};
		
    /**
     * 转账交易请求封装
     * @param payer 付款方
     * @param payee 收款方
     * @param money 转账金额
     * @param memo 转账备注
     * @param fee 手续费
     * @param isSendMsg 是否发送短信,Y 发送，N不发送
     * @return
     */
    public static TradeRequestInfo createTransferTradeRequest(PartyInfo payer, PartyInfo payee, Money money, String memo, Money fee, String isSendMsg, TradeType tradeType) {
        TradeRequestInfo tradeReqest = new TradeRequestInfo();
        tradeReqest.setPayer(payer);
        tradeReqest.setPayee(payee);
        tradeReqest.setAmount(money);
        tradeReqest.setMemo(memo);
        tradeReqest.setFree(fee);
        tradeReqest.setTradeType(tradeType);
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
    public static PartyInfo createPay(String accountId, String memberId, String operatorId, String mobile, String memberName,MemberType memberType) {
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
	 * 生成交易请求
	 * @param payMember
	 * @param payeeMember
	 * @param form
	 * @param transfer
	 * @param tradeType
	 * @return
	 *//*
	public static TradeRequestInfo  createTradeReqest(BaseMember payMember, BaseMember payeeMember, KjtTransferForm form, Transfer transfer, TradeType tradeType) {
        //生成收款方
        PartyInfo payee = memberToPartyInfoConvert.convert(payeeMember);
        //生成付款方
        PartyInfo payer = memberToPartyInfoConvert.convert(payMember);
        payer.setAccountName(payMember.getLoginName());

        *//******************* 生成交易请求 *************//*
        String remark = transfer.getRemark(),
        	   bool   = form.getSendNoteMsg() == 0 ? CommonConstant.FALSE_STRING : CommonConstant.TRUE_STRING;
      
        *//**
         * 获取转账金额
         *//*
        Money money = new Money(transfer.getMoney()),
        	  fee  	= new Money();
        
        *//**
         * 创建交易请求
         *//*
        TradeRequestInfo tradeReqest = TradeReqestUtil.createTransferTradeRequest(
			payer, 
			payee, 
			money,
			remark,
			fee,
			bool,
			tradeType);
        
        return tradeReqest;	
	}*/
    
}
