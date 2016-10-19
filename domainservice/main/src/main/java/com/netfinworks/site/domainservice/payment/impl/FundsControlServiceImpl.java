/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-domainservice-main
 * @date 2014年8月11日
 */
package com.netfinworks.site.domainservice.payment.impl;

import java.util.Date;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.payment.common.v2.enums.FundPropType;
import com.netfinworks.pfs.service.payment.FundsControlFacade;
import com.netfinworks.pfs.service.payment.request.FundsFreezeRequest;
import com.netfinworks.pfs.service.payment.request.FundsUnFreezeRequest;
import com.netfinworks.pfs.service.payment.response.PaymentResponse;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domainservice.payment.FundsControlService;
import com.netfinworks.voucher.service.facade.VoucherFacade;

/**
 * 资金冻结、解冻服务实现
 * @author xuwei
 * @date 2014年8月11日
 */
@Service("fundsControlService")
public class FundsControlServiceImpl implements FundsControlService {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
    @Resource(name = "fundsControlFacade")
    private FundsControlFacade fundsControlFacade;

    @Resource
    VoucherFacade              voucherFacade;

	@Override
	public String freeze(String memberId, String accountId, Money freezeAmount, OperationEnvironment env) {
		try {
			String vourceNo = StringUtils.replace(String.valueOf(UUID.randomUUID()), "-", "");
			FundsFreezeRequest freezeReq = new FundsFreezeRequest();
			freezeReq.setMemberId(memberId);
			freezeReq.setAccountNo(accountId);
			freezeReq.setFundPropType(FundPropType.DEBIT_FUND);
			freezeReq.setFreezeAmount(freezeAmount);
			freezeReq.setBizProductCode(TradeType.FREEZE.getBizProductCode());
			freezeReq.setPaymentVoucherNo(vourceNo);
			freezeReq.setGmtPaymentInitiate(new Date());
			PaymentResponse paymentResp = fundsControlFacade.freeze(freezeReq, env);
			if (paymentResp != null) {
				if (paymentResp.isSuccess()) {
					return vourceNo;
				} else {
					logger.error("资金冻结失败，原因：{}", paymentResp.getReturnMessage());
				}
				
			}
		} catch (Exception e) {
			logger.error("资金冻结失败");
			throw new RuntimeException("资金冻结失败");
		}
		
		
		return null;
	}

	@Override
	public boolean unfreeze(String origVourceNo, Money unFreezeAmount, OperationEnvironment env) {
		try {
			String vourceNo = StringUtils.replace(String.valueOf(UUID.randomUUID()), "-", "");
			FundsUnFreezeRequest unFreezeReq = new FundsUnFreezeRequest();
			unFreezeReq.setPaymentVoucherNo(vourceNo);
			unFreezeReq.setOrigPaymentVoucherNo(origVourceNo);
			unFreezeReq.setUnfreezeAmount(unFreezeAmount);
			unFreezeReq.setGmtPaymentInitiate(new Date());
			PaymentResponse paymentResp = fundsControlFacade.unfreeze(unFreezeReq, env);
			if (paymentResp != null) {
				if (paymentResp.isSuccess()) {
					return true;
				} else {
					logger.error("资金解冻失败，原因：{}", paymentResp.getReturnMessage());
				}
				return paymentResp.isSuccess();
			}
		} catch (Exception e) {
			logger.error("资金解冻失败");
			throw new RuntimeException("资金解冻失败");
		}
		
		return false;
	}

}
