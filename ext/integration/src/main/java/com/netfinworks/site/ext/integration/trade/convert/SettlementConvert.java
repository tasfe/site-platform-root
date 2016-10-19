package com.netfinworks.site.ext.integration.trade.convert;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.netfinworks.common.util.money.Money;
import com.netfinworks.site.core.common.util.JsonMapUtil;
import com.netfinworks.site.domain.domain.info.BaseInfo;
import com.netfinworks.site.domain.domain.info.CoSplitParameter;
import com.netfinworks.site.domain.domain.info.SummaryInfo;
import com.netfinworks.site.domain.domain.trade.CoTradeRequest;
import com.netfinworks.site.domain.enums.TradeTypeRequest;
import com.netfinworks.site.ext.integration.util.MoneyFormat;
import com.netfinworks.site.ext.integration.util.TradeState;
import com.netfinworks.tradeservice.facade.enums.TimeQueryType;
import com.netfinworks.tradeservice.facade.enums.TradeType;
import com.netfinworks.tradeservice.facade.model.SplitParameter;
import com.netfinworks.tradeservice.facade.model.query.RefundTradeBasicInfo;
import com.netfinworks.tradeservice.facade.model.query.TradeBasicInfo;
import com.netfinworks.tradeservice.facade.model.query.TradeInfoSummary;
import com.netfinworks.tradeservice.facade.request.TradeBasicQueryRequest;

/**
 * <p>
 * 请求查询结算转换
 * </p>
 * 
 * @author zhangyun.m
 * @version $Id: SettlementConvert.java, v 0.1 2013-12-6 下午6:53:15 HP Exp $
 */
public class SettlementConvert {

	/**
	 * 请求查询结算流水明细list
	 * 
	 * @param req
	 * @return
	 */
	public static TradeBasicQueryRequest convertSettlementRequset(
			CoTradeRequest req) {

		TradeBasicQueryRequest request = new TradeBasicQueryRequest();
		List<TradeType> tType = new ArrayList<TradeType>();
		
		request.setTimeQueryType(TimeQueryType.BASE_MODIFIED);

		// 企业Id
		if (req.getMemberId() != null) {
//			request.setMemberId(req.getMemberId());
			request.setSellerId(req.getMemberId());
		}
		// 查询开始时间
		if (req.getBeginTime() != null) {
			request.setGmtStart(req.getBeginTime());
		}
		// 查询结束时间
		if (req.getEndTime() != null) {
			request.setGmtEnd(req.getEndTime());
		}
		// 分页
		request.setQueryBase(req.getQueryBase());
		// 交易状态
		request.setTradeStatus(req.getStatus());
		// 产品编码
		if (req.getProductCodes() != null) {
		    request.setProductCodes(req.getProductCodes());
		}
		
		if (req.getIgnoreProductCodes() != null) {
			request.setIgnoreProductCodes(req.getIgnoreProductCodes());
		}
		// 交易类型(即时到账收单交易,担保收单交易,收单退款交易)
		
		List<TradeTypeRequest> types=req.getTradeType();
		if(null==types){
			tType.add(TradeType.INSTANT_ACQUIRING);
			tType.add(TradeType.ENSURE_ACQUIRING);
			tType.add(TradeType.REFUND_ACQUIRING);
		}else{
			 if(types.size()==1 && types.get(0).getCode().equals(TradeType.INSTANT_TRASFER.getCode())){
			    	tType.add(TradeType.getByCode(types.get(0).getCode()));
			    	request.setBuyerId(req.getMemberId());
			    	request.setSellerId(null);
			    	List<String> productCode=new ArrayList<String>();
					productCode.add(com.netfinworks.site.domain.enums.TradeType.TRANSFER.getBizProductCode());
					request.setProductCodes(productCode);
			 }else{
			    	for(TradeTypeRequest type:types){
						tType.add(TradeType.getByCode(type.getCode()));
					}
			 }		
		}		
		request.setTradeType(tType);
		// 汇总
		request.setNeedSummary(req.isNeedSummary());

		// 是否发生过结算
		request.setHasSettled(req.isHasSettled());

		return request;
	}

	/**
	 * 查询结算流水明细结果转换
	 * 
	 * @param list
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static List<BaseInfo> convertSettlementReponseList(
			List<TradeBasicInfo> list, String memberId)
			throws IllegalAccessException, InvocationTargetException {

		List<BaseInfo> result = null;
		// 分账信息集
		List<CoSplitParameter> csp = null;
		
		if ((list != null) && (list.size() > 0)) {
			result = new ArrayList<BaseInfo>();
			BaseInfo baseInfo = null;
			for (TradeBasicInfo info : list) {
				baseInfo = new BaseInfo();
				if (info instanceof RefundTradeBasicInfo) {
					RefundTradeBasicInfo a = (RefundTradeBasicInfo) info;
					baseInfo.setOrigTradeSourceVoucherNo(a.getOrigTradeSourceVoucherNo());
				}
				baseInfo.setGmtpaid(info.getGmtPaid());
				// 时间
				baseInfo.setGmtModified(info.getGmtModified());
				// 流水号
				baseInfo.setSerialNumber(info.getTradeSourceVoucherNo());
				// 产品编码
				baseInfo.setOrderNumber(info.getBizProductCode());
				// 订单金额
				baseInfo.setOrderMoney(info.getTradeAmount());
				// 订单状态（数字）
				baseInfo.setOrderState(info.getStatus());
				// 交易类型
				baseInfo.setTradeType(info.getTradeType().toString());
				// 付款金额
				baseInfo.setPayAmount(info.getPayAmount());
				// 交易备注
				baseInfo.setTradeMemo(info.getTradeMemo());
				// 分账信息集
				csp = SettlementConvert.convertSplitParameterReponse(info
						.getSplitParameter());
				baseInfo.setSplitParameter(csp);
				if ((csp != null) && (csp.size() > 0)) {
					for (CoSplitParameter cspInfo : csp) {
						if ("coin".equals(cspInfo.getSplitTag())) {
							// 分账 ： 金币金额
							baseInfo.setSplitCoinAmount(cspInfo.getAmount());
						} else if ("rebate".equals(cspInfo.getSplitTag())) {
							// 分账 ：佣金金额
							baseInfo.setSplitRebateAmount(cspInfo.getAmount());
						}
					}
				}

				// 订单号
				baseInfo.setBizNo(info.getBizNo());
				// 买家ID
				baseInfo.setBuyerId(info.getBuyerId());
				// 买家名称
				baseInfo.setBuyerName(info.getBuyerName());
				// 卖家ID
				baseInfo.setSellerId(info.getSellerId());
				// 卖家名称
				baseInfo.setSellerName(info.getSellerName());
				// 会员Id
				baseInfo.setMemberId(memberId);
				// 交易提交时间
				baseInfo.setGmtSubmit(info.getGmtSubmit());
				// 金币金额
				baseInfo.setCoinAmount(info.getCoinAmount());
				// 商户订单款
				Money setAmount = info.getSettledAmount();
				Money reSetAmount = info.getRefundInstSettledAmount();

				if (setAmount == null) {
					setAmount = new Money();
				}
				if (reSetAmount == null) {
					reSetAmount = new Money();
				}
				baseInfo.setSettledAmount(setAmount.subtract(reSetAmount));
				// 交易状态（汉字）
				baseInfo.setTradeState(TradeState.getTradeStateForTrade(Integer
						.parseInt(info.getStatus())));
				//手续费
				baseInfo.setPayeeFee(info.getPayeeFee());
				//交易凭证号
				baseInfo.setTradeVoucherNo(info.getTradeVoucherNo());
				//退款金额
				baseInfo.setRefundInstSettledAmount(info.getRefundInstSettledAmount());

				baseInfo.setPayChannel(info.getPayChannel());
				//商户订单批次号
				baseInfo.setSourceBatchNo(info.getSourceBatchNo());
			
				result.add(baseInfo);
			}
			return result;
		}
		return null;
	}

	/**
	 * 结算汇总转换
	 * 
	 * @param tradeInfoSummary
	 * @return
	 */
	public static SummaryInfo convertSummaryReponse(
			TradeInfoSummary tradeInfoSummary, String companyName) {
		SummaryInfo summaryInfo = new SummaryInfo();
		// 公司名称
		summaryInfo.setCompanyName(companyName);
		// 退单笔数
		summaryInfo.setTotalRefundCount(tradeInfoSummary.getTotalRefundCount());
		// 退单金额
		summaryInfo.setTotalRefundAmount(tradeInfoSummary
				.getTotalRefundAmount());
		// 结算金额
		summaryInfo.setTotalSettledAmount(tradeInfoSummary
				.getTotalSettledAmount());
		// 订单金额
		summaryInfo.setTotalTradeAmount(tradeInfoSummary.getTotalTradeAmount());
		// 订单笔数
		summaryInfo.setTotalTradeCount(tradeInfoSummary.getTotalTradeCount());
		// 退单结算金额
		summaryInfo.setTotalRefundSettledAmount(tradeInfoSummary
				.getTotalRefundSettledAmount());
		// 用json 包装的Map<String,Money>,key=splitTag，value=金额
		String amoutMapJson = tradeInfoSummary.getSplitTagAmoutMapJson();
		summaryInfo.setSplitTagAmoutMapJson(amoutMapJson);

		Map<String, Object> splitTagMap = JsonMapUtil.jsonToMap(amoutMapJson);
		// 金币
		Map<String, Object> coinAmount = null;
		Money coin = null;
		// 佣金
		Map<String, Object> rebateAmount = null;
		Money rebate = null;
		if ((splitTagMap != null) && (splitTagMap.size() > 0)) {
		    
		    Object coinStr = splitTagMap.get("coin");
		    if(coinStr != null){
		        coinAmount = JsonMapUtil.jsonToMap(coinStr.toString());
		        if(coinAmount != null){
		            coin = new Money(coinAmount.get("amount").toString());
		        }
		    }
		    
		    Object rebateStr =  splitTagMap.get("rebate");
		    if(rebateStr != null){
		        rebateAmount = JsonMapUtil.jsonToMap(rebateStr.toString());
		        if(rebateAmount != null){
		            rebate = new Money(rebateAmount.get("amount").toString());
		        }
		    }
		} 
		coin = MoneyFormat.convertMoney(coin);
		rebate = MoneyFormat.convertMoney(rebate);
		
		summaryInfo.setCoin(coin);
		summaryInfo.setRebate(rebate);

		// 实收金额 = 结算金额(totalSettledAmount) - 佣金(rebate) - 金币(coin) -
		// 退结算(totalRefundSettledAmount)
		Money sRealAmount = MoneyFormat
				.convertMoney(tradeInfoSummary.getTotalSettledAmount())
				.subtract(coin)
				.subtract(rebate)
				.subtract(
						MoneyFormat.convertMoney(tradeInfoSummary
								.getTotalRefundSettledAmount()));
		// 实收金额
		summaryInfo.setRealAmount(sRealAmount);		
		/** 总计手续费笔数 */
		summaryInfo.setTotalFeeCount(tradeInfoSummary.getTotalFeeCount());		
		/** 总计手续费金额 */
		summaryInfo.setTotalFeeAmount(tradeInfoSummary.getTotalFeeAmount());
		/** 总计退还手续费笔数 */
		summaryInfo.setTotalRfdFeeCount(tradeInfoSummary.getTotalRfdFeeCount());
		/** 总计退还手续费金额 */
		summaryInfo.setTotalRfdFeeAmount(tradeInfoSummary.getTotalRfdFeeAmount());	

		return summaryInfo;
	}

	/**
	 * 转换分账信息集
	 * 
	 * @param sp
	 * @return
	 */
	public static List<CoSplitParameter> convertSplitParameterReponse(
			List<SplitParameter> list) {
		List<CoSplitParameter> cList = new ArrayList<CoSplitParameter>();
		CoSplitParameter csp = null;
		for (SplitParameter info : list) {
			csp = new CoSplitParameter();
			csp.setAmount(info.getAmount());
			csp.setMemo(info.getMemo());
			csp.setPayeeAccountNo(info.getPayeeAccountNo());
			csp.setPayeeId(info.getPayeeId());
			csp.setPayerAccountNo(info.getPayerAccountNo());
			csp.setPayerId(info.getPayerId());
			csp.setSplitTag(info.getSplitTag());
			cList.add(csp);
		}
		return cList;
	}
	
	/**
	 * 初始化金额
	 * @param tradeInfoSummary
	 * @return
	 */
	public static TradeInfoSummary initSummaryInfo(TradeInfoSummary tradeInfoSummary){
		tradeInfoSummary.setTotalRefundAmount(new Money("0.00"));
		tradeInfoSummary.setTotalTradeAmount(new Money("0.00"));
		tradeInfoSummary.setTotalFeeAmount(new Money("0.00"));
		tradeInfoSummary.setTotalSettledAmount(new Money("0.00"));
		tradeInfoSummary.setTotalRefundSettledAmount(new Money("0.00"));
		return tradeInfoSummary;
	}
	
	
}
