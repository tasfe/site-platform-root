/**
 * 
 */
package com.netfinworks.site.domainservice.trade.impl;

import javax.annotation.Resource;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.netfinworks.fos.service.facade.FundoutFacade;
import com.netfinworks.fos.service.facade.request.FundoutRequest;
import com.netfinworks.fos.service.facade.response.FundoutResponse;
import com.netfinworks.ma.service.base.model.BankAcctDetailInfo;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.convert.TransferConvertor;
import com.netfinworks.site.domainservice.convert.WithdrawConvertor;
import com.netfinworks.site.domainservice.trade.DefaultWithdrawService;
import com.netfinworks.site.ext.integration.fundout.FundoutService;
import com.netfinworks.site.ext.integration.fundout.convert.FundoutConvertor;
import com.netfinworks.site.ext.integration.member.BankAccountService;
import com.netfinworks.site.ext.integration.voucher.VoucherService;
import com.netfinworks.voucher.common.utils.JsonUtils;

/**
 * <p>提现服务</p>
 * @author fjl
 * @version $Id: DefaultWithdrawServiceImpl.java, v 0.1 2013-11-29 下午4:08:53 fjl Exp $
 */
@Service("defaultWithdrawService")
public class DefaultWithdrawServiceImpl implements DefaultWithdrawService {
    Logger logger = LoggerFactory.getLogger(getClass());
    
    @Resource
    private VoucherService voucherService;
    
    @Resource
    private FundoutService fundoutService;
    
    @Resource
    private FundoutFacade      fundoutFacade;

    @Resource
    private BankAccountService bankAccountService;
    
    @Override
    public TradeRequestInfo applyWithdraw(BaseMember currUser, TradeType tradeType, TradeEnvironment env) throws BizException {
        TradeRequestInfo req = WithdrawConvertor.convertWithdrawApply(currUser, tradeType, env);
        
        //落地交易凭证
        String voucherNo = voucherService.recordTradeVoucher(req);

        req.setTradeVoucherNo(voucherNo);

        return req;
    }

    @Override
    public boolean submitApply(TradeRequestInfo req,String bankcardId) {
		try {
		    validate(req,bankcardId);
		    
		    //落地支付凭证放入回调接口
		    String voucherNo = voucherService.recordPaymentVoucher(req);
		    req.setPaymentVoucherNo(voucherNo);
		    //请求提现
		    return fundoutService.submitWithdraw(req, bankcardId);
		} catch (Exception e) {
		    logger.error("请求提现异常:tradeRequest:{},bankcardId:{}",req, bankcardId);
		    logger.error("请求提现异常:",e);
		    throw new RuntimeException(e.getMessage());
		}
    }
    
    @Override
    public boolean submitWithdraw(TradeRequestInfo req, String bankcardId, TradeEnvironment evn) throws BizException {
        try {
            validate(req);
            validate(bankcardId);

            long beginTime = 0L;
            BankAcctDetailInfo bankCard = bankAccountService.queryBankAccount(bankcardId);
            String bizProductCode = req.getTradeType().getBizProductCode();
            //付款到卡（T+N），付款到卡（实时）不验证卡主人的memberId
            if (!TradeType.PAY_TO_BANK.getBizProductCode().equals(bizProductCode)
                && !TradeType.PAY_TO_BANK_INSTANT.getBizProductCode().equals(bizProductCode)) {
                bizValidate(req, bankCard);
            }
            FundoutRequest request = FundoutConvertor.convert(req, bankCard);
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
                return false;
            }
        } catch (Exception e) {
            logger.error("提交申请提现异常:请求信息{},异常信息:{}", req, e);
            return false;
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
        String memberId = req.getPayer().getMemberId();
        String cardMemberId = card.getMemberId();
        Assert.isTrue(memberId.equals(cardMemberId), "提现方会员号与银行卡绑定的会员必须一致");
    }
    
    @Override
	public TradeRequestInfo applyTransfer(BaseMember currUser, TradeType tradeType, TradeEnvironment env) throws BizException {
         //落地交易凭证
    	 TradeRequestInfo req = TransferConvertor.converBankTransferApply(currUser, tradeType, env);
         String voucherNo = voucherService.recordTradeVoucher(req);
         req.setTradeVoucherNo(voucherNo);

         return req;
	}
    
    @Override
    public void submitBankTransfer(TradeRequestInfo req, BankAcctDetailInfo bankCard) throws BizException {
	    fundoutService.submitBankTransfer(req, bankCard);
    }
    
    private void validate(TradeRequestInfo req,String bankcardId){
        //参数验证
        Assert.notNull(req.getPayer());
        Assert.hasText(req.getPayer().getMemberId());
        Assert.notNull(req.getTradeType());
        Assert.hasText(req.getTradeVoucherNo());
        Assert.hasText(req.getTradeSourceVoucherNo());
        Assert.notNull(req.getAmount());
        Assert.hasText(bankcardId);
    }

}
