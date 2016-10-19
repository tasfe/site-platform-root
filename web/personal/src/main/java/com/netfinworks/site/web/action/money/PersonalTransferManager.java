package com.netfinworks.site.web.action.money;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.netfinworks.common.util.money.Money;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.util.JsonMapUtil;
import com.netfinworks.site.core.common.util.ValidUtils;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.AcqTradeType;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.account.DefaultAccountService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.domainservice.trade.DefaultPaymentService;
import com.netfinworks.site.domainservice.trade.impl.DefaultTransferServiceImpl;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.form.KjtTransferForm;
import com.netfinworks.site.web.action.form.TransferVo;
import com.netfinworks.site.web.common.vo.Transfer;
import com.netfinworks.site.web.util.TradeReqestUtil;

// TODO 这里暂时放在action包里
/**
 * 个人转账管理类
 * @author tangL
 * @date 2015-02-06
 *
 */
@Service("personalTransferManager")
public class PersonalTransferManager {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
    @Autowired
    private DefaultAccountService defaultAccountService;
    @Autowired
    private DefaultMemberService defaultMemberService;
    @Autowired
    private WebDynamicResource webResource;
    @Autowired
    private DefaultTransferServiceImpl defaultTransferService;
    @Autowired
    private DefaultPaymentService defaultPaymentService;
	
    /**
     * 提交所有转账并 获取跳转地址
     * @param transferVo
     * @param payUser
     * @param tradeType
     * @param env
     * @return
     * @throws ServiceException
     */
	public String applyTransferUrl(TransferVo transferVo, PersonMember payUser, TradeType tradeType, TradeEnvironment env) throws ServiceException{
		logger.info("执行转账并返回url");
		
		Assert.notNull(transferVo, "transferVo is null");
		Assert.notNull(transferVo.getTransForm(), "transForm  is null");
		Assert.notEmpty(transferVo.getTranserfers(), "transerfers is empty");
		
		String defaultAccountId = payUser.getDefaultAccountId();
		Assert.hasText(defaultAccountId, "defaultAccountId is null");
	
		MemberAccount account = payUser.getAccount();
		Assert.notNull(account," account is null");
		
		KjtTransferForm form = transferVo.getTransForm();		
		List<String> voucherNoList = new ArrayList<String>(); // 凭证
		List<Transfer> transferList = transferVo.getTranserfers(); // 转账信息
		Integer transferCount = transferList == null ? 0 : transferList.size();	
		form.setTransferCount(transferCount);
		
//		/**
//		 * 获取当前账号可用余额和转账总合
//		 */
//		Money avaiableMoney = account.getAvailableBalance(),
//			  transferMoney = new Money(form.getTotalTransMoney());
//		
//		ValidUtils.valid(avaiableMoney.compareTo(transferMoney) < 0, "余额不足");
		
		/**
		 * 批量处理转账 处理收款人信息和创建交易请求
		 */
		for (Transfer transfer : transferList) {
			TradeRequestInfo tradeReqest = this.createTradeAndGenerateRecord(transfer, form, payUser, tradeType, env);
			voucherNoList.add(tradeReqest.getTradeVoucherNo());
		}
		return defaultPaymentService.applyPayment(payUser, voucherNoList, AcqTradeType.INSTANT_TRASFER, env);
	}	

	/**
	 * 创建交易请求和生成转账记录
	 * @param transfer
	 * @param form
	 * @param payUser
	 * @param tradeType
	 * @param env
	 * @return
	 */
	public TradeRequestInfo createTradeAndGenerateRecord(Transfer transfer, KjtTransferForm form, BaseMember payUser, TradeType tradeType, TradeEnvironment env) {
		BaseMember payeeUser = transfer.getMember(); // 获取收款人
		boolean isSameAccount = payeeUser.getMemberId().equals(payUser.getMemberId());
		ValidUtils.valid(isSameAccount, "相同的账号不能转帐[%s]", payUser.getLoginName());
		// 创建交易请求
		TradeRequestInfo tradeReqest = TradeReqestUtil.createTradeReqest(payUser, payeeUser, form, transfer, tradeType);
        /********************************************/
        // 这里用的Spring BeanUtils 与apahce BeanUtils的顺序是相反的
		BeanUtils.copyProperties(env, tradeReqest);
		
		/** 创建和设置扩展参数  */
		Long payAccountType    = payUser.getAccount().getAccountType(),
			 payeeAccountType  = payeeUser.getAccount().getAccountType();
		
		Map<String, Object> extMap = new HashMap<String, Object>();
		extMap.put(CommonConstant.ACCOUNT_TYPE, payAccountType +"," + payeeAccountType);
		
		tradeReqest.setTradeExtension(JsonMapUtil.mapToJson(extMap));
		
		/********************* 生成转账记录 *******************/
        try {
        	defaultTransferService.etransfer(tradeReqest, tradeType);
		} catch (BizException e) {
			logger.error("转账到账户{}的操作失败", transfer.getContact(), e);
			throw new RuntimeException("转账失败", e);
		}
        return tradeReqest;
	}
}
