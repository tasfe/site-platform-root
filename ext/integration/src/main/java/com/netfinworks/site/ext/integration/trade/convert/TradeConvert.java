/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.ext.integration.trade.convert;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

import com.meidusa.fastjson.JSON;
import com.meidusa.fastjson.TypeReference;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.deposit.api.domain.DepositInfo;
import com.netfinworks.payment.common.v2.enums.PayMode;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.util.JsonMapUtil;
import com.netfinworks.site.domain.domain.info.BaseInfo;
import com.netfinworks.site.domain.domain.info.DepositBasicInfo;
import com.netfinworks.site.domain.domain.info.PartyInfo;
import com.netfinworks.site.domain.domain.info.TradeInfo;
import com.netfinworks.site.domain.domain.response.CoTradeQueryResponse;
import com.netfinworks.site.domain.domain.trade.CoTradeRequest;
import com.netfinworks.site.domain.domain.trade.DownloadBillRequest;
import com.netfinworks.site.domain.domain.trade.OrderSettleInfo;
import com.netfinworks.site.domain.domain.trade.PayWaterInfo;
import com.netfinworks.site.domain.domain.trade.RefundWaterInfo;
import com.netfinworks.site.domain.domain.trade.TradeRefundRequset;
import com.netfinworks.site.domain.domain.trade.TradeRequest;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.domain.trade.TransferInfo;
import com.netfinworks.site.domain.enums.AccessChannel;
import com.netfinworks.site.domain.enums.AcqTradeType;
import com.netfinworks.site.domain.enums.SplitTag;
import com.netfinworks.site.domain.enums.TradeTypeRequest;
import com.netfinworks.site.ext.integration.util.TradeState;
import com.netfinworks.tradeservice.facade.enums.TimeQueryType;
import com.netfinworks.tradeservice.facade.enums.TradeType;
import com.netfinworks.tradeservice.facade.model.AcquiringTradeItemDetail;
import com.netfinworks.tradeservice.facade.model.PaymentInfo;
import com.netfinworks.tradeservice.facade.model.SplitParameter;
import com.netfinworks.tradeservice.facade.model.TradeItemDetail;
import com.netfinworks.tradeservice.facade.model.paymethod.BalancePayMethod;
import com.netfinworks.tradeservice.facade.model.paymethod.PayMethod;
import com.netfinworks.tradeservice.facade.model.query.RefundTradeBasicInfo;
import com.netfinworks.tradeservice.facade.model.query.TradeBasicInfo;
import com.netfinworks.tradeservice.facade.request.PaymentRequest;
import com.netfinworks.tradeservice.facade.request.RefundQueryRequest;
import com.netfinworks.tradeservice.facade.request.RefundRequest;
import com.netfinworks.tradeservice.facade.request.TradeBasicQueryRequest;

/**
 * 通用说明：
 * 
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 * 
 * @version 1.0.0 2013-11-14 上午11:11:27
 * 
 */
public class TradeConvert {

	/**
	 * 创建交易查询的request
	 * 
	 * @return
	 */
	public static TradeBasicQueryRequest createTradeBasicQueryRequset(
			TradeRequest req) {
		TradeBasicQueryRequest request = new TradeBasicQueryRequest();
		List<TradeType> tradeTypeList = new ArrayList<TradeType>();
        tradeTypeList.add(TradeType.INSTANT_TRASFER);
		request.setMemberId(req.getMemberId());
		request.setSellerId(req.getSellerId());
		request.setBuyerId(req.getBuyerId());
		request.setQueryBase(req.getQueryBase());
		request.setPartnerId(req.getPartnerId());
		request.setTradeSourceVoucherNo(req.getTradeSourceVoucherNo());
		request.setTradeStatus(req.getTradeStatus());
		request.setTradeVoucherNo(req.getTradeVoucherNo());
		request.setGmtEnd(req.getGmtEnd());
		request.setGmtStart(req.getGmtStart());
		request.setProductCodes(req.getProductCodes());//产品码
		List<TradeTypeRequest> types = req.getTradeType();
		if ((types != null) && (types.size() > 0)) {
			List<TradeType> tradeType = new ArrayList<TradeType>();
			for (TradeTypeRequest type : types) {
				tradeType.add(TradeType.getByCode(type.getCode()));
			}
			request.setTradeType(tradeType);
		}
		return request;
	}

	/**
	 * 创建退款交易查询的request
	 * 
	 * @return
	 */
	public static RefundQueryRequest createRefundQueryRequest(TradeRequest req) {
		RefundQueryRequest request = new RefundQueryRequest();
		request.setBuyerId(req.getBuyerId());
		request.setQueryBase(req.getQueryBase());
		request.setPartnerId(req.getPartnerId());
		request.setSellerId(req.getSellerId());
		request.setTradeSourceVoucherNo(req.getTradeSourceVoucherNo());
		request.setTradeVoucherNo(req.getTradeVoucherNo());
		request.setGmtCreateStart(req.getGmtCreateStart());//创建时间
		request.setGmtCreateEnd(req.getGmtCreateEnd());
		request.setGmtEnd(req.getGmtEnd());
		request.setGmtStart(req.getGmtStart());
		request.setOrigTradeVoucherNo(req.getOrigTradeVoucherNo());
		request.setTradeStatus(req.getTradeStatus());
		request.setBatchNo(req.getBatchNo());
		request.setOrigTradeSourceVoucherNo(req.getOrigTradeSourceVoucherNo());
		request.setOrigTradeVoucherNo(req.getOrigTradeVoucherNo());
		request.setSourceBatchNo(req.getSourceBatchNo());
		request.setIgnoreProductCodes(req.getIgnoreProductCodes());//Pos排除
		request.setProductCodes(req.getProductCodes());//Pos包含
		return request;
	}

	public static com.netfinworks.tradeservice.facade.request.TradeRequest convertCreateTradeOrder(
			TradeRequestInfo reqinfo, String notifyTemplate,
			String notifyAppTemplate) {
		com.netfinworks.tradeservice.facade.request.TradeRequest req = new com.netfinworks.tradeservice.facade.request.TradeRequest();
		PartyInfo payer = reqinfo.getPayer();
		PartyInfo payee = reqinfo.getPayee();

		req.setBuyerId(payer.getMemberId());
		// PaymentInfo info = new PaymentInfo();
		// info.setBuyerAccountNo(payer.getAccountId());
		// req.setPaymentInfo(info);
		// req.setPartnerId(CommonConstant.TRADE_PARTNER_ID);
		req.setAccessChannel(AccessChannel.WEB.getCode());
		req.setGmtSubmit(new Date());

		AcquiringTradeItemDetail detail = new AcquiringTradeItemDetail();
		detail.setTradeVoucherNo(reqinfo.getTradeVoucherNo());
		detail.setTradeSourceVoucherNo(reqinfo.getTradeSourceVoucherNo());
		detail.setTradeType(TradeType.INSTANT_TRASFER);
		detail.setBizProductCode(reqinfo.getTradeType().getBizProductCode());
		detail.setTradeAmount(reqinfo.getAmount());
		detail.setSellerId(payee.getMemberId());
		detail.setSellerAccountNo(payee.getAccountId());
		detail.setBizNo(reqinfo.getTradeVoucherNo());
		// 转账交易备注
		if (StringUtils.hasText(reqinfo.getMemo())) {
			detail.setTradeMemo(reqinfo.getMemo());
		} else {
			detail.setTradeMemo(reqinfo.getTradeType().getMessage());
		}
		// 转账交易短信通知手机号
		if (reqinfo.isSendMessage() /* && StringUtils.hasText(payee.getMobile()) */) {
			Map<String, Object> map = new HashMap<String, Object>();
			Map<String, String> data = new HashMap<String, String>();
			Map<String, Object> param = new HashMap<String, Object>();
			// data.put(CommonConstant.TRADE_NOTIFY_MOBILE_KEY,
			// payee.getMobile());
			data.put(CommonConstant.TRADE_NOTIFY_MOBILE_KEY,
					payee.getMemberId());
			data.put(CommonConstant.TRADE_NOTIFY_TEMPLATE_KEY, notifyTemplate);
			data.put(CommonConstant.TRADE_NOTIFY_MEMO_KEY, reqinfo.getMemo());
			// data.put(CommonConstant.TRADE_NOTIFY_APP_TEMPLATE_KEY,
			// notifyAppTemplate);//not used
			param.put(CommonConstant.NOTIFY_PARAM_IDENTITY_TYPE,
					CommonConstant.NOTIFY_PARAM_IDENTITY_TYPE_MEMBER);
			map.put(CommonConstant.TRADE_NOTIFY_KEY, data);
			map.put(CommonConstant.TRADE_NOTIFY_PARAM,
					JsonMapUtil.mapToJson(param));// 按要求必须先转为string
			detail.setExtension(JsonMapUtil.mapToJson(map));
		}
		
		// TODO 如果有扩展参数则设置
		if (org.apache.commons.lang.StringUtils.isNotBlank(reqinfo.getTradeExtension())) {
			detail.setExtension(reqinfo.getTradeExtension());
		}
				
		detail.setPartnerId(CommonConstant.TRADE_PARTNER_ID);

		List<TradeItemDetail> details = new ArrayList<TradeItemDetail>(1);
		details.add(detail);
		req.setTradeItemDetailList(details);
		return req;
	}

	/**
	 * 转换交易结果
	 * 
	 * @param list
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static List<TradeInfo> convertTradList(List<TradeBasicInfo> list)
			throws IllegalAccessException, InvocationTargetException {
		if ((list != null) && (list.size() > 0)) {
			List<TradeInfo> result = new ArrayList<TradeInfo>();
			for (TradeBasicInfo info : list) {
				TradeInfo tradeInfo = new TradeInfo();
				BeanUtils.copyProperties(info, tradeInfo,
						new String[] { "tradeType" });
				
				AcqTradeType type = info.getTradeType() == null ? null 
				        : AcqTradeType.getByCode(info.getTradeType().getCode());
				tradeInfo.setTradeType(type);
				tradeInfo.setGmtPaid(info.getGmtPaid());
				tradeInfo.setPayerAccountNo(info.getPayerAccountNo());
				tradeInfo.setPayeeFee(info.getPayeeFee());
				tradeInfo.setPayerFee(info.getPayerFee());
				tradeInfo.setGmtModified(info.getGmtModified());
				result.add(tradeInfo);
			}
			return result;
		}
		return null;

	}

	/**
	 * 转换退款交易结果
	 * 
	 * @param list
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static List<TradeInfo> convertRefundList(
			List<RefundTradeBasicInfo> list) throws IllegalAccessException,
			InvocationTargetException {
		if ((list != null) && (list.size() > 0)) {
			List<TradeInfo> result = new ArrayList<TradeInfo>();
			for (RefundTradeBasicInfo info : list) {
				TradeInfo tradeInfo = new TradeInfo();
				BeanUtils.copyProperties(info, tradeInfo,
						new String[] { "tradeType" });
				AcqTradeType type = AcqTradeType.getByCode(info.getTradeType()
						.getCode());
				tradeInfo.setTradeType(type);
				tradeInfo.setTradeSourceVoucherNo(info.getTradeSourceVoucherNo());
				tradeInfo.setOrigTradeSourceVoucherNo(info.getOrigTradeSourceVoucherNo());
				tradeInfo.setSourceBatchNo(info.getSourceBatchNo());
				tradeInfo.setOrigTradeVoucherNo(info.getOrigTradeVoucherNo());
				result.add(tradeInfo);
			}
			return result;
		}
		return null;

	}

	/**
	 * 转换充值交易结果
	 * 
	 * @param list
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static List<DepositBasicInfo> convertRechargeList(
			List<DepositInfo> list) throws IllegalAccessException,
			InvocationTargetException {
		if ((list != null) && (list.size() > 0)) {
			List<DepositBasicInfo> result = new ArrayList<DepositBasicInfo>();
			for (DepositInfo info : list) {
				DepositBasicInfo tradeInfo = new DepositBasicInfo();
				BeanUtils.copyProperties(info, tradeInfo);
				result.add(tradeInfo);
			}
			return result;
		}
		return null;

	}
	
	/**
	 * 转换支付流水结果
	 * 
	 * @param list
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static List<PayWaterInfo> convertPayWaterList(
			List<TradeBasicInfo> list) throws IllegalAccessException,
			InvocationTargetException {
		List<PayWaterInfo> result = new ArrayList<PayWaterInfo>();
		if ((list != null) && (list.size() > 0)) {
			for (TradeBasicInfo info : list) {
				PayWaterInfo tradeInfo = new PayWaterInfo();
				tradeInfo.setWalletTradeId(info.getTradeVoucherNo());
				tradeInfo.setMerchantOrderId(info.getTradeSourceVoucherNo());
				tradeInfo.setPaymentAmount(info.getPayAmount());
				tradeInfo.setTradeSubmitTime(info.getGmtSubmit());
				tradeInfo.setTradeTime(info.getGmtPaid());
				tradeInfo.setPaymentState(TradeState
						.getTradeStateForTrade(Integer.parseInt(info
								.getStatus())));
				// 商品名称
				tradeInfo.setCommodityName(info.getProdDesc());
				// 订金
				tradeInfo.setDepositAmount(info.getPrepaidAmount());
				// 付款方账户
				tradeInfo.setPayerAccount(info.getBuyerName());
				// 收款方账户
				tradeInfo.setPayeeAccount(info.getSellerName());
				// 付款金币
				tradeInfo.setPaymentGold(info.getCoinAmount());
				// 订单编号
				tradeInfo.setOrderCode(info.getBizNo());
				tradeInfo.setTradeType(info.getTradeType().getCode());
				result.add(tradeInfo);
			}
		}
		return result;

	}

	/**
	 * 转换订单结果
	 * 
	 * @param list
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static List<OrderSettleInfo> convertOrderSettleList(
			List<TradeBasicInfo> list) throws IllegalAccessException,
			InvocationTargetException {
		if ((list != null) && (list.size() > 0)) {
			List<OrderSettleInfo> result = new ArrayList<OrderSettleInfo>();
			for (TradeBasicInfo info : list) {
				OrderSettleInfo tradeInfo = new OrderSettleInfo();
				tradeInfo.setWalletTradeId(info.getTradeVoucherNo());
				tradeInfo.setMerchantOrderId(info.getBizNo());
				tradeInfo.setPaymentAmount(info.getPayAmount());
				tradeInfo.setTradeOverTime(info.getGmtModified());
				// 付款金币
				tradeInfo.setPaymentGold(info.getCoinAmount());
				// 订金金额
				tradeInfo.setDepositAmount(info.getPrepaidAmount());
				// 商户订单款
				Money setAmount = info.getSettledAmount();
				Money reSetAmount = info.getRefundInstSettledAmount();

				if (setAmount == null) {
					setAmount = new Money();
				}
				if (reSetAmount == null) {
					reSetAmount = new Money();
				}
				tradeInfo.setSettledAmount(setAmount.subtract(reSetAmount));
				// 佣金款
				List<SplitParameter> parList = info.getSplitParameter();
				for (SplitParameter splitParameter : parList) {
					if (splitParameter.getSplitTag().equals(
							SplitTag.REBAT.getCode())) {
						tradeInfo.setCommission(splitParameter.getAmount());
					} else if (splitParameter.getSplitTag().equals(
							SplitTag.COIN.getCode())) {
						// 金币款
						tradeInfo.setGoldAmount(splitParameter.getAmount());
					}
				}
				result.add(tradeInfo);
			}
			return result;
		}
		return null;

	}

	/**
	 * 转换退款流水结果
	 * 
	 * @param list
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static List<RefundWaterInfo> convertrefundWaterList(
			List<RefundTradeBasicInfo> list) throws IllegalAccessException,
			InvocationTargetException {
		if ((list != null) && (list.size() > 0)) {
			List<RefundWaterInfo> result = new ArrayList<RefundWaterInfo>();
			for (RefundTradeBasicInfo info : list) {
				RefundWaterInfo tradeInfo = new RefundWaterInfo();
				tradeInfo.setWalletTradeId(info.getTradeVoucherNo());
				tradeInfo.setMerchantOrderId(info.getTradeSourceVoucherNo());
				// 商户退单ID
				tradeInfo.setMerchantRefundOrderId(info.getOrigTradeVoucherNo());
				// 商品名称
				tradeInfo.setCommodityName(info.getProdDesc());
				// 退款金额
				tradeInfo.setRefundAmount(info.getPayAmount());
				// Money zero = new Money();
				// Money a1= info.getRefundEnsureAmount()== null?
				// zero:info.getRefundEnsureAmount();
				// Money a2 = info.getRefundCoinAmount()== null ?
				// zero:info.getRefundCoinAmount();
				// Money a3 = info.getRefundPrepayAmount()== null ?
				// zero:info.getRefundPrepayAmount();
				// Money sum = a1.add(a2).add(a3);
				// tradeInfo.setRefundAmountNum(sum.toString());
				// 退担保金额
				tradeInfo.setRefundGuarantAmount(info.getRefundEnsureAmount());
				// 退金币金额
				tradeInfo.setRefundGoldAmount(info.getRefundCoinAmount());
				// 退订金金额
				tradeInfo.setRefundDepositAmount(info.getRefundPrepayAmount());
				// 退款提交时间
				tradeInfo.setRefundSubmitTime(info.getGmtSubmit());
				// 退款时间
				tradeInfo.setRefundTime(info.getGmtModified());
				// 退款状态
				tradeInfo.setRefundState(TradeState.getTradeState(Integer.parseInt(info.getStatus())));
				result.add(tradeInfo);
			}
			return result;
		}
		return null;

	}

	/**
	 * 转换转账交易
	 * 
	 * @param list
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static List<TransferInfo> convertTransferList(
			List<TradeBasicInfo> list, DownloadBillRequest reqInfo)
			throws IllegalAccessException, InvocationTargetException {
		if ((list != null) && (list.size() > 0)) {
			List<TransferInfo> result = new ArrayList<TransferInfo>();
			for (TradeBasicInfo info : list) {
				TransferInfo tradeInfo = new TransferInfo();
				//交易订单号
				tradeInfo.setTradeSourceVoucherNo(info.getTradeSourceVoucherNo());
				tradeInfo.setOrderId(info.getTradeVoucherNo());
				/** 订单号--交易凭证号 */
				tradeInfo.setTransferAmount(info.getTradeAmount());
				/** 转账金额---交易金额 */
				tradeInfo.setTransferTime(info.getGmtSubmit());
				/** 转账时间-- 交易提交时间 */
				tradeInfo.setBuyId(info.getBuyerId());
				tradeInfo.setSellId(info.getSellerId());
				tradeInfo.setBuyerName(info.getBuyerName());
				tradeInfo.setSellerName(info.getSellerName());
				tradeInfo.setTradeType(info.getTradeType().toString());
				tradeInfo.setPayeeFee(info.getPayeeFee());
				tradeInfo.setPayerFee(info.getPayerFee());
				tradeInfo.setMemberId(reqInfo.getMemberId());
				// 1:卖方 2：买方
				if (info.getBuyerId().equals(reqInfo.getMemberId())) {
					tradeInfo.setPlamId("2");
				} else if (info.getSellerId().equals(reqInfo.getMemberId())) {
					tradeInfo.setPlamId("1");
				}
				tradeInfo.setState(TradeState.getTradeState(Integer
						.parseInt(info.getStatus())));
				tradeInfo.setTradeMemo(info.getTradeMemo());
				tradeInfo.setGmtPaid(info.getGmtPaid());//支付时间
				tradeInfo.setBatchNo(info.getBatchNo());// 批次号
				tradeInfo.setBizProductCode(info.getBizProductCode());
				if(org.apache.commons.lang.StringUtils.isNotBlank(info.getExtention())){
				    tradeInfo.setExtention(info.getExtention());//扩张字段放有关于买方/卖方的账户类型    
					Map<String, String> extMap = JSON.parseObject(info.getExtention(), new TypeReference<Map<String, String>>() {});
					tradeInfo.setSourceBatchNo(extMap.get("sourceBatchNo"));
				}
				result.add(tradeInfo);
			}
			return result;
		}
		return null;

	}

	/**
	 * 账单下载转换request 支付流水
	 * 
	 * @param reqInfo
	 * @return
	 */
	public static TradeBasicQueryRequest convertDownloadBillQueryRequset(
			DownloadBillRequest reqInfo) {

		TradeBasicQueryRequest request = new TradeBasicQueryRequest();
		request.setGmtStart(reqInfo.getBeginTime());
		request.setGmtEnd(reqInfo.getEndTime());
		request.setTimeQueryType(TimeQueryType.PAID);
		request.setSellerId(reqInfo.getMemberId());
		// request.setPartnerId(reqInfo.getMemberId());
		request.setQueryBase(reqInfo.getQueryBase());
		request.setTradeStatus(reqInfo.getTradeStatus());
		List<TradeType> tradeTypeList = new ArrayList<TradeType>();
		tradeTypeList.add(TradeType.INSTANT_ACQUIRING);
		tradeTypeList.add(TradeType.ENSURE_ACQUIRING);
		tradeTypeList.add(TradeType.PREPAY_ACQUIRING);
		tradeTypeList.add(TradeType.REFUND_ACQUIRING);
		request.setTradeType(tradeTypeList);
		return request;
	}

	/**
	 * request 订单结算
	 * 
	 * @param reqInfo
	 * @return
	 */
	public static TradeBasicQueryRequest convertDownloadBillQueryOrderRequset(
			DownloadBillRequest reqInfo) {

		TradeBasicQueryRequest request = new TradeBasicQueryRequest();
		request.setGmtStart(reqInfo.getBeginTime());
		request.setGmtEnd(reqInfo.getEndTime());
		request.setTimeQueryType(TimeQueryType.MODIFIED);
		request.setSellerId(reqInfo.getMemberId());// 切换到查询卖家的退款信息
		// request.setPartnerId(reqInfo.getMemberId());
		request.setQueryBase(reqInfo.getQueryBase());
		request.setTradeStatus(reqInfo.getTradeStatus());
		List<TradeType> tradeTypeList = new ArrayList<TradeType>();
		tradeTypeList.add(TradeType.INSTANT_ACQUIRING);
		tradeTypeList.add(TradeType.ENSURE_ACQUIRING);
		// tradeTypeList.add(TradeType.PREPAY_ACQUIRING);
		tradeTypeList.add(TradeType.REFUND_ACQUIRING);
		request.setTradeType(tradeTypeList);
		return request;
	}

	/**
	 * 转账转换request
	 * 
	 * @param reqInfo
	 * @return
	 */
	public static TradeBasicQueryRequest convertTransferQueryRequset(
			DownloadBillRequest reqInfo) {
		List<TradeType> tradeTypeList = new ArrayList<TradeType>();
		tradeTypeList.add(TradeType.INSTANT_TRASFER);
		TradeBasicQueryRequest request = new TradeBasicQueryRequest();
		request.setGmtStart(reqInfo.getBeginTime());
		request.setGmtEnd(reqInfo.getEndTime());
		request.setQueryBase(reqInfo.getQueryBase());
		request.setTradeType(tradeTypeList);
		request.setTradeVoucherNo(reqInfo.getTradeVoucherNo());//交易订单号
		request.setProductCodes(reqInfo.getProductCodes());//产品码
		request.setTradeStatus(reqInfo.getTradeStatus());
		// 查询时（转账） 企业为付款方，对应交易的买方
		// request.setBuyerId(reqInfo.getSellerId());
		request.setMemberId(reqInfo.getMemberId());
		request.setBatchNo(reqInfo.getBatchNo());
		request.setSourceBatchNo(reqInfo.getSourceBatchNo());
		request.setTradeSourceVoucherNo(reqInfo.getTradeSourceVoucherNo());
		return request;
	}

	/**
	 * 账单下载转换request 退款流水
	 * 
	 * @param reqInfo
	 * @return
	 */
	public static RefundQueryRequest convertDownloadBillQueryRefundRequset(
			DownloadBillRequest reqInfo) {

		RefundQueryRequest request = new RefundQueryRequest();
		request.setGmtStart(reqInfo.getBeginTime());
		request.setGmtEnd(reqInfo.getEndTime());
		// request.setPartnerId(reqInfo.getMemberId());//切换到查询卖家的退款信息
		request.setSellerId(reqInfo.getMemberId());
		request.setQueryBase(reqInfo.getQueryBase());
		List<String>  tradeStatus = new ArrayList<String>();
		tradeStatus.add(CommonConstant.REFUND_SUCCESS);
		request.setTradeStatus(tradeStatus);
		return request;
	}

	/**
	 * zhangyun.m 查询交易记录转换
	 * 
	 * @param req
	 * @return
	 */
	public static TradeBasicQueryRequest convertTradeRequset(CoTradeRequest req) {

		TradeBasicQueryRequest request = new TradeBasicQueryRequest();
		// 企业Id
		if (req.getMemberId() != null) {
			request.setMemberId(req.getMemberId());
		}
		// 卖方Id
		if (req.getSellerId() != null) {
			request.setMemberId(req.getMemberId());
			request.setSellerId(req.getSellerId());
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
		// 汇总
		request.setNeedSummary(req.isNeedSummary());
		//交易凭证号
		request.setTradeVoucherNo(req.getTradeVoucherNo());
		//交易订单号--交易原始凭证号 
		request.setTradeSourceVoucherNo(req.getTradeSourceVoucherNo());
		if(CollectionUtils.isNotEmpty(req.getTradeType())) {
			List<TradeType> tradeType = new ArrayList<TradeType>();
			for(TradeTypeRequest type : req.getTradeType()) {
				tradeType.add(TradeType.getByCode(type.getCode()));
			}
			request.setTradeType(tradeType);
		}
		if(req.getProductCodes() != null) {
			request.setProductCodes(req.getProductCodes());
		}
		if(req.getSourceBatchNo() != null) {
			request.setSourceBatchNo(req.getSourceBatchNo());
		}
		return request;
	}

	/**
	 * 查询交易记录结果转换 zhangyun.m
	 * 
	 * @param list
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static List<BaseInfo> convertTradeReponseList(
			List<TradeBasicInfo> list, String memberId)
			throws IllegalAccessException, InvocationTargetException {

		List<BaseInfo> result = null;
		if ((list != null) && (list.size() > 0)) {
			result = new ArrayList<BaseInfo>();
			BaseInfo baseInfo = null;
			for (TradeBasicInfo info : list) {
				baseInfo = new BaseInfo();
				// 时间
				baseInfo.setGmtModified(info.getGmtModified());
				// 流水号
				baseInfo.setSerialNumber(info.getTradeSourceVoucherNo());
				// 产品编码
				baseInfo.setOrderNumber(info.getBizProductCode());
				// 订单金额
				baseInfo.setOrderMoney(info.getTradeAmount());
				// 订单状态
				baseInfo.setOrderState(info.getStatus());
				// 交易类型
				baseInfo.setTradeType(info.getTradeType().toString());
				// 付款金额
				baseInfo.setPayAmount(info.getPayAmount());
				// 交易备注
				baseInfo.setTradeMemo(info.getTradeMemo());
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
				// 交易凭证号
				baseInfo.setTradeVoucherNo(info.getTradeVoucherNo());
				// 支付时间-zhaozhenqiang
				baseInfo.setGmtpaid(info.getGmtPaid());
				//卖家手续费
				baseInfo.setPayeeFee(info.getPayeeFee());
				// 产品编码
				baseInfo.setBizProductCode(info.getBizProductCode());
				//扩张字段
				baseInfo.setExtention(info.getExtention());
				//商户批次号
				baseInfo.setSourceBatchNo(info.getSourceBatchNo());
				//批次号
				baseInfo.setBatchNo(info.getBatchNo());
				result.add(baseInfo);
			}
			return result;
		}
		return null;
	}

	public static PaymentRequest convertCreatePay(TradeRequestInfo info) {
		PaymentRequest request = new PaymentRequest();
		PartyInfo payer = info.getPayer();
		request.setAccessChannel(AccessChannel.WEB.getCode());
		request.setBuyerId(payer.getMemberId());
		request.setGmtSubmit(new Date());

		PaymentInfo payment = new PaymentInfo();
		payment.setBuyerAccountNo(payer.getAccountId());
		payment.setPaymentSourceVoucherNo(info.getTradeSourceVoucherNo());
		payment.setPaymentVoucherNo(info.getPaymentVoucherNo());

		List<PayMethod> payMethodList = new ArrayList<PayMethod>();
		BalancePayMethod method = new BalancePayMethod();
		method.setPayMode(PayMode.BALANCE.getCode());
		method.setPayChannel("01");// 01:余额
		method.setAmount(info.getAmount());
		method.setPayerId(payer.getMemberId());
		method.setPayerAccountNo(payer.getAccountId());
		payMethodList.add(method);
		payment.setPayMethodList(payMethodList);
		request.setPaymentInfo(payment);

		List<String> tradeVoucherNoList = new ArrayList<String>();
		tradeVoucherNoList.add(info.getTradeVoucherNo());
		request.setTradeVoucherNoList(tradeVoucherNoList);

		return request;
	}
	
	/**
	 * 网关交易--退款功能
	 *  
	 * */
	public static RefundRequest convertRefundRequest(BaseInfo td,TradeRefundRequset tr) {
		RefundRequest refundRequest=new RefundRequest();
		/**origTradeVoucherNo是查到的交易的tradeVoucherNo*/
		refundRequest.setOrigTradeVoucherNo(td.getTradeVoucherNo());
		/**退款交易凭证号*/
		refundRequest.setTradeVoucherNo(tr.getTradeVoucherNo());
		/** 退款交易原始凭证号 */
		refundRequest.setTradeSourceVoucherNo(tr.getTradeSourceVoucherNo());
		/** 外部业务号 */
		refundRequest.setBizNo(td.getBizNo());
		/** 退款总金额 */
		refundRequest.setRefundAmount(tr.getRefundAmount());
		/** 退款提交时间 */
		refundRequest.setGmtSubmit(new Date());
	    /** 退款分账信息 */
//	    private List<SplitParameter> splitParameterList;import com.netfinworks.tradeservice.facade.model.SplitParameter;
		/** 终端类型 */
//		private String               accessChannel;
		refundRequest.setAccessChannel(tr.getAccessChannel());
		/** 备注 */
		refundRequest.setMemo(tr.getRemarks());
		/** 扩展信息 */
//		refundRequest.setExtension(td.getBaseInfoL);
		return refundRequest;
	}
	
	/**
	 * HYJ  退款审核  2014-08-19 16:54:00
	 * @param td
	 * @param tr
	 * @return
	 */
	public static RefundRequest convertRefundRequest(CoTradeQueryResponse td,TradeRefundRequset tr) {
		RefundRequest refundRequest=new RefundRequest();
		/**origTradeVoucherNo是查到的交易的tradeVoucherNo*/
		refundRequest.setOrigTradeVoucherNo(td.getBaseInfoList().get(0).getTradeVoucherNo());
		/**退款交易凭证号*/
		refundRequest.setTradeVoucherNo(tr.getTradeVoucherNo());
		/** 退款交易原始凭证号 */
		refundRequest.setTradeSourceVoucherNo(tr.getTradeSourceVoucherNo());
		/** 外部业务号 */
		refundRequest.setBizNo(td.getBaseInfoList().get(0).getBizNo());
		/** 退款总金额 */
		refundRequest.setRefundAmount(tr.getRefundAmount());
		/** 退款提交时间 */
		refundRequest.setGmtSubmit(new Date());
	    /** 退款分账信息 */
//	    private List<SplitParameter> splitParameterList;import com.netfinworks.tradeservice.facade.model.SplitParameter;
		/** 终端类型 */
//		private String               accessChannel;
		/** 备注 */
		refundRequest.setMemo(tr.getRemarks());
		/** 扩展信息 */
//		refundRequest.setExtension(td.getBaseInfoL);
		return refundRequest;
	}

}
