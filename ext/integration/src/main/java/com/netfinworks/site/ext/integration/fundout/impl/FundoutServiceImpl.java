/**
 *
 */
package com.netfinworks.site.ext.integration.fundout.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.fos.service.facade.FundoutFacade;
import com.netfinworks.fos.service.facade.domain.FundoutInfo;
import com.netfinworks.fos.service.facade.domain.FundoutInfoSummary;
import com.netfinworks.fos.service.facade.request.FundoutQueryRequest;
import com.netfinworks.fos.service.facade.request.FundoutRequest;
import com.netfinworks.fos.service.facade.response.FundoutQueryResponse;
import com.netfinworks.fos.service.facade.response.FundoutResponse;
import com.netfinworks.fos.service.facade.response.FundoutSummaryQueryResponse;
import com.netfinworks.ma.service.base.model.BankAcctDetailInfo;
import com.netfinworks.site.core.common.util.StarUtil;
import com.netfinworks.site.domain.domain.fos.Fundout;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.trade.FundoutQuery;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.BankType;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.fundout.FundoutService;
import com.netfinworks.site.ext.integration.fundout.convert.FundoutConvertor;
import com.netfinworks.site.ext.integration.member.BankAccountService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.ues.UesServiceClient;
import com.netfinworks.site.ext.integration.voucher.VoucherService;
import com.netfinworks.voucher.common.utils.JsonUtils;

/**
 * <p>出款服务</p>
 * @author fjl
 * @version $Id: FundoutServiceImpl.java, v 0.1 2013-11-29 下午5:39:07 fjl Exp $
 */
@Service("fundoutService")
public class FundoutServiceImpl implements FundoutService {
    private Logger             logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private FundoutFacade      fundoutFacade;

    @Resource
    private BankAccountService bankAccountService;

    @Resource(name = "memberService")
    private MemberService      memberService;
    
    @Resource
    private VoucherService voucherService;
    
    @Resource(name = "uesService")
	private UesServiceClient uesServiceClient;

    @Override
    public PageResultList queryFundoutInfo(FundoutQuery req, OperationEnvironment env)
                                                                                      throws BizException {
        try {
            long beginTime = 0L;
            if (logger.isInfoEnabled()) {
                logger.info("查询出款服务，请求参数：{}", req);
                beginTime = System.currentTimeMillis();
            }
            FundoutQueryRequest paramFundoutQueryRequest = FundoutConvertor
                .convertFundoutQueryRequest(req);
            FundoutQueryResponse response = fundoutFacade.queryFundoutInfo(
                paramFundoutQueryRequest, env);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("远程查询出款服务， 耗时:{} (ms); 响应结果:{} ",
                    new Object[] { consumeTime, response });
            }
            if (ResponseCode.NEW_SUCCESS.getCode().equals(response.getReturnCode())) {
                PageResultList page = new PageResultList();
                List<FundoutInfo> list = response.getFundoutInfoList();
                List<Fundout> result = new ArrayList<Fundout>();
                if ((list != null) && (list.size() > 0)) {
                    for (FundoutInfo info : list) {
                        Fundout fundout = new Fundout();
                        BeanUtils.copyProperties(info, fundout);
                        fundout.setAccountNo("");//该字段为账户号，安全因素不给传
                        String cardNo = uesServiceClient.getDataByTicket(info.getCardNo());
                        fundout.setCardNo(StarUtil.getMaskData(cardNo));
                        result.add(fundout);
                    }
                }
                page.setInfos(result);
                QueryBase queryBase = new QueryBase();
                BeanUtils.copyProperties(response, queryBase);
                page.setPageInfo(queryBase);
                return page;
            } else {
                logger.error("查询出款信息失败:请求信息{}", req);
                return null;
            }
        } catch (Exception e) {
            logger.error("提交申请提现异常:请求信息" + req, e);
            throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(),e);
        }
    }

	@Override
	public PageResultList queryFundoutOrderInfo(FundoutQuery req, OperationEnvironment env) throws BizException {
		try {
			long beginTime = 0L;
			if (logger.isInfoEnabled()) {
				logger.info("查询出款信息(来源于fundout order)服务，请求参数：{}", req);
				beginTime = System.currentTimeMillis();
			}
			FundoutQueryRequest paramFundoutQueryRequest = FundoutConvertor.convertFundoutQueryRequest(req);
			FundoutQueryResponse response = fundoutFacade.queryFundoutOrderInfo(paramFundoutQueryRequest, env);
			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("远程查询出款服务， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
			}
			if ((response != null) && (response.getReturnCode() != null) && response.getReturnCode().startsWith("S")) {
				PageResultList page = new PageResultList();
				List<FundoutInfo> list = response.getFundoutInfoList();
				List<Fundout> result = new ArrayList<Fundout>();
				if ((list != null) && (list.size() > 0)) {
					for (FundoutInfo info : list) {
						Fundout fundout = new Fundout();
						BeanUtils.copyProperties(info, fundout);
						fundout.setAccountNo("");// 该字段为账户号，安全因素不给传
						String cardNo = uesServiceClient.getDataByTicket(info.getCardNo());
						fundout.setCardNo(StarUtil.getMaskData(cardNo));
						String productName = TradeType.getByBizProductCode(fundout.getProductCode()).getMessage();
						fundout.setProductName(productName);
						result.add(fundout);
					}
				}
				page.setInfos(result);
				QueryBase queryBase = new QueryBase();
				BeanUtils.copyProperties(response, queryBase);
				page.setPageInfo(queryBase);
				return page;
			} else {
				logger.error("查询出款信息失败:请求信息{}", req);
				return null;
			}
		} catch (Exception e) {
			logger.error("查询出款信息异常:请求信息" + req, e);
			throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e);
		}
	}
	
	
	@Override
	public FundoutInfoSummary queryFundoutOrderInfoSummary(FundoutQuery req,
			OperationEnvironment env) throws BizException {
		
		try {
            long beginTime = 0L;
            if (logger.isInfoEnabled()) {
                logger.info("查询出款服务汇总，请求参数：{}", req);
                beginTime = System.currentTimeMillis();
            }
            FundoutQueryRequest paramFundoutQueryRequest = FundoutConvertor
                .convertFundoutQueryRequest(req);
            FundoutSummaryQueryResponse response = fundoutFacade.querySummary(paramFundoutQueryRequest, env);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("远程查询出款服务汇总， 耗时:{} (ms); 响应结果:{} ",
                    new Object[] { consumeTime, response });
            }
            if (ResponseCode.NEW_SUCCESS.getCode().equals(response.getReturnCode())) {                               
                FundoutInfoSummary fundoutInfoSummary=response.getFundoutInfoSummary();   
                return fundoutInfoSummary;
            } else {
                logger.error("查询出款汇总信息失败:请求信息{}", req);
                return null;
            }
        } catch (Exception e) {
            logger.error("查询出款服务汇总异常:请求信息" + req, e);
            throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(),e);
        }
	}

    @Override
    public boolean submitWithdraw(TradeRequestInfo req, String bankcardId) throws BizException {
        try {
            validate(req);
            validate(bankcardId);

            long beginTime = 0L;
            BankAcctDetailInfo bankCard = bankAccountService.queryBankAccount(bankcardId);
            bankCard.setRealName("test");
            String bizProductCode = req.getTradeType().getBizProductCode();
            //付款到卡（T+N），付款到卡（实时）不验证卡主人的memberId
            if (!TradeType.PAY_TO_BANK.getBizProductCode().equals(bizProductCode)
                && !TradeType.PAY_TO_BANK_INSTANT.getBizProductCode().equals(bizProductCode)) {
                bizValidate(req, bankCard);
            }
            FundoutRequest request = FundoutConvertor.convert(req, bankCard);
            OperationEnvironment evn = new OperationEnvironment();
            BeanUtils.copyProperties(req, evn);
            //生成支付凭证号开始
            req.setTradeExtension(JsonUtils.toJson(request));
            String paymentVoucherNo = voucherService.recordPaymentVoucher(req);
            request.setPaymentOrderNo(paymentVoucherNo);
            req.setPaymentVoucherNo(paymentVoucherNo);
            //生成支付凭证号结束
            if (logger.isInfoEnabled()) {
                logger.info("提交申请提现，请求参数：{}", request);
                beginTime = System.currentTimeMillis();
            }
            FundoutResponse resp = fundoutFacade.submit(request, evn);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("远程提交申请提现， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, resp });
            }
            if (ResponseCode.APPLY_SUCCESS.getCode().equals(resp.getReturnCode())
                || resp.getReturnCode().startsWith("S")) {
                return true;
            } else {
                logger.info("远程提交申请提现，响应结果为失败:{},{}", resp.getReturnCode(), resp.getReturnMessage());
                throw new Exception(resp.getReturnMessage());
            }
        } catch (Exception e) {
            logger.error("提交申请提现异常:请求信息{},异常信息:{}" , req, e);
            return false;
        }
    }
    
	@Override
	public void submitBankTransfer(TradeRequestInfo req, BankAcctDetailInfo bankCard) throws BizException {
		validate(req);

		long beginTime = 0L;
		bizValidate(req, bankCard);
		if (StringUtils.isEmpty(bankCard.getBankCode())) {
			bankCard.setBankCode(BankType.getByMsg(bankCard.getBankName()).getCode());
		}
		
		FundoutRequest request = FundoutConvertor.convert(req, bankCard);
		OperationEnvironment evn = new OperationEnvironment();
		BeanUtils.copyProperties(req, evn);

		// 生成支付凭证号开始
		req.setTradeExtension(JsonUtils.toJson(request));
		String paymentVoucherNo = voucherService.recordPaymentVoucher(req);
		request.setPaymentOrderNo(paymentVoucherNo);
		req.setPaymentVoucherNo(paymentVoucherNo);

		// 生成支付凭证号结束
		logger.info("提交申请提现，请求参数：{}", request);
		beginTime = System.currentTimeMillis();
		FundoutResponse resp = fundoutFacade.submit(request, evn);
		long consumeTime = System.currentTimeMillis() - beginTime;
		logger.info("远程提交申请提现， 耗时:{} (ms); 响应结果:{} ", new Object[] {
				consumeTime, resp });

		// 检查返回结果
		if (ResponseCode.APPLY_SUCCESS.getCode().equals(resp.getReturnCode())
				|| resp.getReturnCode().startsWith("S")) {
		} else {
			logger.error("远程提交申请提现，响应结果为失败:{},{}", resp.getReturnCode(),
					resp.getReturnMessage());
			throw new BizException(ErrorCode.PAYMENT_FAILURE,
					resp.getReturnMessage());
		}
	}
    
    private void validate(TradeRequestInfo req) {
        //参数验证
        Assert.notNull(req.getPayer());
        Assert.hasText(req.getPayer().getMemberId());
        Assert.notNull(req.getTradeType());
    }

    private void validate(String bankCardId) {
        //参数验证
        Assert.hasText(bankCardId);
    }

    private void bizValidate(TradeRequestInfo req, BankAcctDetailInfo card) {
        Assert.notNull(card);
        if (TradeType.WITHDRAW.getBizProductCode().equals(req.getTradeType().getBizProductCode())
        		&& TradeType.WITHDRAW_INSTANT.getBizProductCode().equals(req.getTradeType().getBizProductCode())) {
        	String memberId = req.getPayer().getMemberId();
            String cardMemberId = card.getMemberId();
            Assert.isTrue(memberId.equals(cardMemberId), "提现方会员号与银行卡绑定的会员必须一致");
        }
    }

	

}
