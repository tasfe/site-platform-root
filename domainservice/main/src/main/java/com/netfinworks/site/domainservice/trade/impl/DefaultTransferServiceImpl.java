/**
 * 
 */
package com.netfinworks.site.domainservice.trade.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.netfinworks.batchservice.facade.api.BatchServiceFacade;
import com.netfinworks.batchservice.facade.enums.ProductType;
import com.netfinworks.batchservice.facade.enums.SubmitType;
import com.netfinworks.batchservice.facade.model.BatchDetail;
import com.netfinworks.batchservice.facade.request.BatchRequest;
import com.netfinworks.batchservice.facade.response.BatchResponse;
import com.netfinworks.ma.service.facade.IMerchantFacade;
import com.netfinworks.site.core.common.util.RadomUtil;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.trade.DefaultTransferService;
import com.netfinworks.site.ext.integration.cashier.CashierService;
import com.netfinworks.site.ext.integration.trade.TradeService;
import com.netfinworks.site.ext.integration.voucher.VoucherService;
import com.netfinworks.tradeservice.facade.response.PaymentResponse;

/**
 * <p>钱包转账服务</p>
 * @author fjl
 * @version $Id: DefaultTransferServiceImpl.java, v 0.1 2013-11-29 下午1:51:27 fjl Exp $
 */
@Service("defaultTransferService")
public class DefaultTransferServiceImpl implements DefaultTransferService {

    @Resource
    private VoucherService voucherService;

    @Resource
    private CashierService cashierService;

    @Resource(name="tradeService")
    private TradeService   tradeService;

    @Resource(name = "merchantFacade")
    private IMerchantFacade merchantFacade;
    
    @Resource(name = "batchServiceFacade")
    private BatchServiceFacade batchServiceFacade;
    
	Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String transfer(TradeRequestInfo req) throws BizException {
        
        validate(req);

        req.setTradeType(TradeType.TRANSFER);
        //落地凭证
        String voucherNo = voucherService.recordTradeVoucher(req);
        req.setTradeVoucherNo(voucherNo);
        //落地交易订单
        tradeService.createOrder(req);
        //请求地址
        return cashierService.applyCashierUrl(req);
    }
    
    @Override
	public void etransfer(TradeRequestInfo req, TradeType tradeType) throws BizException {
        validate(req);

        req.setTradeType(tradeType);
        
        if ((req != null) && StringUtils.isEmpty(req.getTradeVoucherNo())) {
        	//落地凭证
            String voucherNo = voucherService.recordTradeVoucher(req);
            req.setTradeVoucherNo(voucherNo);
        }
        //落地交易订单
        tradeService.createOrder(req);
    }

    private void validate(TradeRequestInfo req) {
        Assert.notNull(req.getPayee());
        Assert.notNull(req.getPayer());
        Assert.notNull(req.getAmount());
        Assert.hasText(req.getPayee().getMemberId());
        Assert.hasText(req.getPayer().getMemberId());
        Assert.notNull(req.getPayer().getMemberType());
    }

    @Override
    public PaymentResponse pay(TradeRequestInfo req) throws BizException {
        return tradeService.pay(req);
    }

	@Override
	public void getTransferVoucherNo(TradeRequestInfo req) throws BizException {
		req.setTradeType(TradeType.TRANSFER);
	    //落地凭证
	    String voucherNo = voucherService.recordTradeVoucher(req);
	    req.setTradeVoucherNo(voucherNo);
	}

	@Override
	public String batchTransferApply(String batchNo,String sourceBatchNo, ProductType productType, EnterpriseMember user, List<BatchDetail> batchDetails, String totalAmount) throws Exception {
		BatchRequest req = new BatchRequest();
		req.setBatchNo(batchNo);
		req.setSourceBatchNo(sourceBatchNo);
		req.setSubmitTime(new Date());
		req.setPartnerId(user.getMemberId());
		req.setSubmitType(SubmitType.API.getCode());
		req.setProductType(productType.getCode());
		req.setSubmitId(user.getMemberId());
		req.setSubmitName(user.getMemberName());
		req.setTotalAmount(new BigDecimal(totalAmount));
        req.setTotalCount(batchDetails.size());
		req.setBatchDetails(batchDetails);
		
		BatchResponse resp = batchServiceFacade.create(req);
		if ((resp != null) && !"S001".equals(resp.getResultCode())) {
			throw new Exception(resp.getResultMessage());
		}
		
		return req.getBatchNo();
	}

	@Override
	public String batchTransferSubmit(String batchNo,String sourceBatchNo, ProductType productType, EnterpriseMember user, List<BatchDetail> batchDetails, String totalAmount) throws Exception {
	    if ((batchDetails == null) || (batchDetails.size() == 0)) {
	        throw new Exception("转账明细不能为空");
	    }
	    
		BatchRequest req = new BatchRequest();
		req.setBatchNo(batchNo);
		req.setSourceBatchNo(sourceBatchNo);
		req.setSubmitTime(new Date());
		req.setPartnerId(user.getMemberId());
		req.setSubmitType(SubmitType.API.getCode());
		req.setProductType(productType.getCode());
		req.setSubmitId(user.getMemberId());
		req.setSubmitName(user.getMemberName());
		req.setTotalAmount(new BigDecimal(totalAmount));
		req.setTotalCount(batchDetails.size());
		req.setBatchDetails(batchDetails);
		
		BatchResponse resp = batchServiceFacade.createAndPay(req);
		if ((resp != null) && !"S001".equals(resp.getResultCode())) {
			throw new Exception(resp.getResultMessage());
		}
		
		return req.getBatchNo();
	}

	@Override
	public String batchTransferPay(String batchNo) throws Exception {

		logger.info("批量支付批次号：" + batchNo);

		BatchResponse resp = batchServiceFacade.pay(batchNo);

		logger.info("批量支付响应：" + resp);

		if ((resp != null) && !"S001".equals(resp.getResultCode())) {
			throw new Exception(resp.getResultMessage());
		}
		return resp.getResultCode();
	}
	
    @Override
    public BatchResponse unfreezeBatch(String batchNo) throws Exception {
        return batchServiceFacade.cancel(batchNo);
    }
    
}
