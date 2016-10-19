package com.netfinworks.site.ext.integration.trade.impl;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.BaseInfo;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.response.CoTradeQueryResponse;
import com.netfinworks.site.domain.domain.trade.CoTradeRequest;
import com.netfinworks.site.domain.domain.trade.TradeRefundRequset;
import com.netfinworks.site.domain.domain.trade.TradeRequest;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.ClientEnvironment;
import com.netfinworks.site.ext.integration.trade.TradeService;
import com.netfinworks.site.ext.integration.trade.convert.SettlementConvert;
import com.netfinworks.site.ext.integration.trade.convert.TradeConvert;
import com.netfinworks.site.ext.integration.voucher.VoucherService;
import com.netfinworks.tradeservice.facade.api.TradeProcessFacade;
import com.netfinworks.tradeservice.facade.api.TradeQueryFacade;
import com.netfinworks.tradeservice.facade.model.paymethod.PayMethod;
import com.netfinworks.tradeservice.facade.model.query.RefundTradeBasicInfo;
import com.netfinworks.tradeservice.facade.model.query.TradeBasicInfo;
import com.netfinworks.tradeservice.facade.request.PaymentRequest;
import com.netfinworks.tradeservice.facade.request.RefundQueryRequest;
import com.netfinworks.tradeservice.facade.request.RefundRequest;
import com.netfinworks.tradeservice.facade.request.TradeBasicQueryRequest;
import com.netfinworks.tradeservice.facade.response.PayMethodQueryResponse;
import com.netfinworks.tradeservice.facade.response.PaymentResponse;
import com.netfinworks.tradeservice.facade.response.RefundQueryResponse;
import com.netfinworks.tradeservice.facade.response.RefundResponse;
import com.netfinworks.tradeservice.facade.response.TradeBasicQueryResponse;
import com.netfinworks.tradeservice.facade.response.TradeDetailQueryResponse;
import com.netfinworks.voucher.common.utils.JsonUtils;

/**
 * 
 * <p>
 * 交易查询
 * </p>
 * 
 * @author qinde
 * @version $Id: TradeServiceImpl.java, v 0.1 2013-12-5 上午10:41:33 qinde Exp $
 */
@Service("tradeService")
public class TradeServiceImpl extends ClientEnvironment implements TradeService {
	private Logger logger = LoggerFactory.getLogger(TradeServiceImpl.class);

	@Resource(name = "tradeQueryFacade")
	private TradeQueryFacade tradeQueryFacade;

	@Resource(name = "tradeProcessFacade")
	private TradeProcessFacade tradeProcessFacade;

	// 转账通过短信模板
	@Resource(name = "transfer_notify_template")
	private String transfer_notify_template;
	// 转账通过app短信模板
	@Resource(name = "transfer_notify_template_app")
	private String transfer_notify_template_app;

	@Resource
    private VoucherService voucherService;
	
	@Override
	public PageResultList queryTradeList(TradeRequest req,
			OperationEnvironment env) throws BizException {
		try {
			TradeBasicQueryRequest request = TradeConvert
					.createTradeBasicQueryRequset(req);
			long beginTime = 0L;
			if (logger.isInfoEnabled()) {
				logger.info("查询交易信息，请求参数：{}", request);
				beginTime = System.currentTimeMillis();
			}
			TradeBasicQueryResponse response = tradeQueryFacade.queryList(
					request, env);
			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("远程调用查询交易信息， 耗时:{} (ms); 响应结果:{} ", new Object[] {
						consumeTime, response });
			}
			if (response.isSuccess()) {
				PageResultList page = new PageResultList();
				List<TradeBasicInfo> result = response.getTradeBasicInfoList();
				if ((result != null) && (result.size() > 0)) {
					page.setInfos(TradeConvert.convertTradList(result));
				}
				page.setPageInfo(response.getQueryBase());
				return page;
			} else {
				return null;
			}
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error("查询交易 {}信息异常:请求信息" + req, e);
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}
		}
	}

	@Override
	public PageResultList queryRefundList(TradeRequest req,
			OperationEnvironment env) throws BizException {
		try {
			RefundQueryRequest request = TradeConvert
					.createRefundQueryRequest(req);
			long beginTime = 0L;
			if (logger.isInfoEnabled()) {
				logger.info("查询退款交易信息，请求参数：{}", request);
				beginTime = System.currentTimeMillis();
			}
			RefundQueryResponse response = tradeQueryFacade.queryRefundList(
					request, env);
			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("远程调用查询退款交易信息， 耗时:{} (ms); 响应结果:{} ", new Object[] {
						consumeTime, response });
			}
			if (response.isSuccess()) {
				PageResultList page = new PageResultList();
				List<RefundTradeBasicInfo> result = response
						.getRefundTradeInfos();
				if ((result != null) && (result.size() > 0)) {
					page.setInfos(TradeConvert.convertRefundList(result));
				}
				page.setPageInfo(response.getQueryBase());
				return page;
			} else {
				PageResultList page = new PageResultList();
				page.setPageInfo(response.getQueryBase());
				return page;
			}
		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error("查询退款交易 {}信息异常:请求信息" + req, e);
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}
		}
	}

	@Override
	public TradeDetailQueryResponse queryDetail(String paramString,
			OperationEnvironment paramOperationEnvironment) {
		return tradeQueryFacade.queryDetail(paramString,
				paramOperationEnvironment);
	}

	@Override
	public void createOrder(TradeRequestInfo req) throws BizException {
		try {
			validate(req);

			long beginTime = 0L;
			com.netfinworks.tradeservice.facade.request.TradeRequest request = TradeConvert
					.convertCreateTradeOrder(req, transfer_notify_template,transfer_notify_template_app);

			if (logger.isInfoEnabled()) {
				logger.info("落地交易订单，请求参数：{}", request);
				beginTime = System.currentTimeMillis();
			}

			PaymentResponse resp = tradeProcessFacade.createAndPay(request, req);

			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("远程落地交易订单， 耗时:{} (ms); 响应结果:{} ", consumeTime, resp);
			}
			if (!resp.isSuccess()) {
				throw new BizException(ErrorCode.SYSTEM_ERROR,
						resp.getResultMessage());
			}
		} catch (Exception e) {
			logger.error("落地交易订单异常:请求信息" + req, e);
			throw new BizException(ErrorCode.SYSTEM_ERROR);
		}
	}

	private void validate(TradeRequestInfo req) {
		Assert.notNull(req.getPayee());
		Assert.notNull(req.getPayer());
		Assert.notNull(req.getAmount());
		Assert.hasText(req.getPayee().getMemberId());
		Assert.hasText(req.getPayer().getMemberId());
	}

	/*
	 * zhangyun.m查询交易记录
	 */
	@Override
	public CoTradeQueryResponse queryList(CoTradeRequest req)
			throws BizException {
		try {
			long beginTime = 0L;
			// 请求查询结算流水明细转换
			TradeBasicQueryRequest requestList = TradeConvert
					.convertTradeRequset(req);
			if (logger.isInfoEnabled()) {
				logger.info("查询交易记录，请求参数：{}", requestList);
				beginTime = System.currentTimeMillis();
			}
			OperationEnvironment oe = getEnv(req.getClientIp());
			// 请求返回list
			TradeBasicQueryResponse response = tradeQueryFacade.queryList(
					requestList, oe);

			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("远程查询交易记录， 耗时:{} (ms); 响应结果:{} ", new Object[] {
						consumeTime, response });
			}

			if (response.isSuccess()) {
				List<TradeBasicInfo> result = response.getTradeBasicInfoList();
				CoTradeQueryResponse rep = new CoTradeQueryResponse();
				if ((result != null) && (result.size() > 0)) {
					// 对结算流水list进行转换
					rep.setBaseInfoList(TradeConvert.convertTradeReponseList(
							result, req.getMemberId()));
				}
				if (response.getTradeInfoSummary() != null) {
					rep.setSummaryInfo(SettlementConvert.convertSummaryReponse(response.getTradeInfoSummary(), "kjt"));
				}
				rep.setQueryBase(response.getQueryBase());
				return rep;
			} else {
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}

		} catch (Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error("查询交易记录 " + req.getMemberId() + "信息异常:请求信息" + req,
						e);
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}
		}
	}

    @Override
    public PaymentResponse pay(TradeRequestInfo request) throws BizException {
        PaymentResponse response = new PaymentResponse();
        try {
            validate(request);
            long beginTime = 0L;

            PaymentRequest req = TradeConvert.convertCreatePay(request);
            OperationEnvironment evn = new OperationEnvironment();
            BeanUtils.copyProperties(request, evn);
            request.setTradeExtension(JsonUtils.toJson(req));
            //生成支付凭证号
            String paymentVoucherNo = voucherService.recordPaymentVoucher(request);
            
            /*调用交易系统支付接口*/
            OperationEnvironment env = new OperationEnvironment();
            env.setClientId(CommonConstant.SOURCE_CODE);
            req.getPaymentInfo().setPaymentVoucherNo(paymentVoucherNo);
            request.setPaymentVoucherNo(paymentVoucherNo);
            if (logger.isInfoEnabled()) {
                logger.info("调用支付接口，请求参数：{}", request);
                beginTime = System.currentTimeMillis();
            }
            response = tradeProcessFacade.pay(req, env);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("调用支付接口响应， 耗时:{} (ms); 响应结果:{} ",
                    new Object[] { consumeTime, response });
            }
            
            if (!response.isSuccess()) {
                throw new BizException(ErrorCode.PAYMENT_FAILURE,
                    response.getResultMessage());
            }
        } catch (Exception e) {
            logger.error("调用支付接口异常:请求信息" + request, e);
            throw new BizException(ErrorCode.SYSTEM_ERROR);
        }
        return response;
    }
    
    /**
     * 网关交易--退款功能
     * 
     * */
    @Override
    public RefundResponse tradeRefund(BaseInfo rep,TradeRefundRequset tr,OperationEnvironment env) {
    	long beginTime = 0L;
    	RefundResponse rr=new RefundResponse();
    	try {
    	RefundRequest refundRequest=TradeConvert.convertRefundRequest(rep,tr);
    	 if (logger.isInfoEnabled()) {
             logger.info("调用退款接口，请求参数：{}", refundRequest);
             beginTime = System.currentTimeMillis();
         }
    	 rr=tradeProcessFacade.refund(refundRequest, env);
    	 if (logger.isInfoEnabled()) {
             long consumeTime = System.currentTimeMillis() - beginTime;
             logger.info("调用退款接口响应， 耗时:{} (ms); 响应结果:{} ",
                 new Object[] { consumeTime, rr });
         }
    	} catch (Exception e) {
            logger.error("网关交易-退款service" + rep, e);
        }
		return rr;
	}

    /**
     * 网关交易--退款功能
     * 
     * */
    @Override
    public RefundResponse tradeRefund(CoTradeQueryResponse rep,TradeRefundRequset tr,OperationEnvironment env) {
    	long beginTime = 0L;
    	RefundResponse rr=new RefundResponse();
    	try {
    	RefundRequest refundRequest=TradeConvert.convertRefundRequest(rep,tr);
    	 if (logger.isInfoEnabled()) {
             logger.info("调用退款接口，请求参数：{}", refundRequest);
             beginTime = System.currentTimeMillis();
         }
    	 rr=tradeProcessFacade.refund(refundRequest, env);
    	 if (logger.isInfoEnabled()) {
             long consumeTime = System.currentTimeMillis() - beginTime;
             logger.info("调用退款接口响应， 耗时:{} (ms); 响应结果:{} ",
                 new Object[] { consumeTime, rr });
         }
    	} catch (Exception e) {
            logger.error("网关交易-退款service"+rep, e);
        }
		return rr;
	}

    /**
     * 简单退款功能
     * 
     * */
    @Override
    public RefundResponse simpleRefund(RefundRequest refundRequest,OperationEnvironment env) {
    	long beginTime = 0L;
    	RefundResponse rr=new RefundResponse();
    	try {
    	 if (logger.isInfoEnabled()) {
             logger.info("调用退款接口，请求参数：{}", refundRequest);
             beginTime = System.currentTimeMillis();
         }
    	 rr=tradeProcessFacade.refund(refundRequest, env);
    	 if (logger.isInfoEnabled()) {
             long consumeTime = System.currentTimeMillis() - beginTime;
             logger.info("调用退款接口响应， 耗时:{} (ms); 响应结果:{} ",
                 new Object[] { consumeTime, rr });
         }
    	} catch (Exception e) {
            logger.error("简单退款service" + refundRequest, e);
        }
		return rr;
	}

	@Override
	public String queryPayMethods(String tradeVoucherNo,
			OperationEnvironment env) {
		long beginTime = 0L;
		try {
	    	 if (logger.isInfoEnabled()) {
	             logger.info("调用查询支付方式接口，请求参数：{}", tradeVoucherNo);
	             beginTime = System.currentTimeMillis();
	         }
	    	 PayMethodQueryResponse response = tradeQueryFacade.queryPayMethods(tradeVoucherNo, env);
	    	 if (logger.isInfoEnabled()) {
	             long consumeTime = System.currentTimeMillis() - beginTime;
	             logger.info("调用退款接口响应， 耗时:{} (ms); 响应结果:{} ",
	                 new Object[] { consumeTime, response });
	         }
	    	 if(response.isSuccess()){
	    		 if(response.getPayMethodList().size()!=0){
	    			 List<PayMethod> payMethodList = response.getPayMethodList(); 
	    			 return payMethodList.get(0).getPayChannel();
	    		 }
	    		 return null;
	    	 }
	    	} catch (Exception e) {
	            logger.error("调用查询支付方式接口，请求参数：{}" + tradeVoucherNo, e);
	        }
		return null;
	}

	@Override
	public boolean queryIfExistsRefundApi(String tradeVoucherNo,
			OperationEnvironment env) {
		long beginTime = 0L;
		boolean isExist = false;
		try {
	    	 if (logger.isInfoEnabled()) {
	             logger.info("调用查询是否有退款接口，请求参数：{}", tradeVoucherNo);
	             beginTime = System.currentTimeMillis();
	         }
	    	 String refundSource = "merchant";//refundSource app:app端,merchant:商户站
	    	 isExist = tradeQueryFacade.queryIfExistsRefundApi(tradeVoucherNo, refundSource, env);
	    	 if (logger.isInfoEnabled()) {
	             long consumeTime = System.currentTimeMillis() - beginTime;
	             logger.info("调用查询是否有退款接口响应， 耗时:{} (ms); 响应结果:{} ",
	                 new Object[] { consumeTime, isExist });
	         }
	    	} catch (Exception e) {
	            logger.error("调用查询是否有退款接口，请求参数：{}" + tradeVoucherNo, e);
	        }
		return isExist;
	}
}
