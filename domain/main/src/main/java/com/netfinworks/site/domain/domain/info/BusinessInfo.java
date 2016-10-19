package com.netfinworks.site.domain.domain.info;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.common.util.money.Money;

/**
 * 
 * <p>
 * 买卖记录
 * </p>
 * 
 * @author Guan Xiaoxu
 * @version $Id: BusinessInfo.java, v 0.1 2013-11-28 下午6:07:06 Guanxiaoxu Exp $
 */
public class BusinessInfo extends BaseInfo {

	private static final long serialVersionUID = -4867237469904727615L;

	public BusinessInfo() {
		super();
	}

	public BusinessInfo(String serialNumber, String orderNumber,
			Date gmtModified, String tradeType, Money orderMoney,
			String orderType, String orderState, String area, String shopName,
			String otherParty, Money partyMoney, String tradeMemo,
			String bizNo, String buyerName, String sellerName, String buyerId,
			String sellerId, String memberId, Date gmtSubmit, Money coinAmount,
			Money splitRebateAmount, Money splitCoinAmount,
			Money settledAmount, String tradeState,String tradeVoucherNo,Money refundAmount) {
		super(serialNumber, orderNumber, gmtModified, tradeType, orderMoney,
				orderType, orderState, partyMoney, tradeMemo, bizNo, buyerName,
				sellerName, buyerId, sellerId, memberId, gmtSubmit, coinAmount,
				splitRebateAmount, splitCoinAmount, settledAmount, tradeState,tradeVoucherNo,refundAmount);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
