/**
 * 
 */
package com.netfinworks.site.domainservice.trade.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.convert.RechargeConvertor;
import com.netfinworks.site.domainservice.trade.DefaultRechargeService;
import com.netfinworks.site.ext.integration.cashier.CashierService;
import com.netfinworks.site.ext.integration.voucher.VoucherService;

/**
 * <p>充值服务实现</p>
 * @author fjl
 * @version $Id: RechargeServiceImpl.java, v 0.1 2013-11-28 上午10:52:53 fjl Exp $
 */
@Service("defaultRechargeService")
public class DefaultRechargeServiceImpl implements DefaultRechargeService {

    @Resource
    private VoucherService voucherService;
    
    @Resource
    private CashierService cashierService;
    
    @Override
    public String applyRecharge(BaseMember currUser, TradeEnvironment env) throws BizException {
        return applyRecharge(currUser,env,"WEB");
    }

	@Override
	public String applyRecharge(BaseMember currUser, TradeEnvironment env,
			String accessChannel) throws BizException {
		TradeRequestInfo req = RechargeConvertor.convertRechargeReq(currUser, env);
        
        //落地凭证
        String voucherNo = voucherService.recordTradeVoucher(req);
        
        req.setTradeVoucherNo(voucherNo);
        req.getPayer().setMemberType(currUser.getMemberType());
        
        //请求地址
        return cashierService.applyCashierUrl(req,accessChannel);
	}
}
