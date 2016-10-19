/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年7月7日
 */
package com.netfinworks.site.web.action.money;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.meidusa.fastjson.JSONObject;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.ma.service.base.model.BankAccountInfo;
import com.netfinworks.ma.service.base.model.BankAcctInfo;
import com.netfinworks.pbs.service.facade.PayPartyFeeFacade;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.audit.Audit;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;
import com.netfinworks.site.domain.domain.info.PartyInfo;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.OperateTypeEnum;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.account.DefaultAccountService;
import com.netfinworks.site.domainservice.audit.AuditServiceImpl;
import com.netfinworks.site.domainservice.payment.FundsControlService;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.trade.DefaultWithdrawService;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.action.form.CashingForm;
import com.netfinworks.site.web.common.constant.AuditStatus;
import com.netfinworks.site.web.common.constant.AuditType;
import com.netfinworks.site.web.common.constant.CashingType;
import com.netfinworks.site.web.common.constant.FunctionType;
import com.netfinworks.site.web.common.util.ObjectUtils;
import com.netfinworks.site.web.util.CommonUtils;
import com.netfinworks.site.web.util.LogUtil;
import com.netfinworks.site.web.util.TradeReqestUtil;

/**
 * 提现
 * @author xuwei
 * @date 2014年7月7日
 */
@Controller
@RequestMapping("/cash")
public class CashingAction extends BaseAction {
	@Resource(name = "defaultBankAccountService")
	private DefaultBankAccountService defaultBankAccountService;
	
	@Resource(name = "defaultAccountService")
	private DefaultAccountService defaultAccountService;
	
	@Resource(name = "defaultWithdrawService")
    private DefaultWithdrawService    defaultWithdrawService;
	
	@Resource(name = "defaultPayPasswdService")
    private DefaultPayPasswdService   defaultPayPasswdService;

    @Resource(name = "auditService")
    private AuditServiceImpl       auditService;
    
    @Resource(name = "payPartyFeeFacade")
    private PayPartyFeeFacade payPartyFeeFacade;
    
    @Resource(name = "defaultMemberService")
    private DefaultMemberService defaultMemberService;
    
    @Resource(name = "fundsControlService")
    private FundsControlService fundsControlService;
    
	/**
	 * 进入提现第一步，输入提现相关信息，
	 * 包括选择银行卡，填写金额等
	 * @return
	 */
	@RequestMapping("/toInputCashInfo.htm")
	public ModelAndView toInputCashInfo(HttpServletRequest request, OperationEnvironment env) {
		ModelAndView mv = new ModelAndView();
		// 先获取用户信息
		EnterpriseMember user = getUser(request);

		// 实名认证
		if (!AuthResultStatus.PASS.getCode().equals(user.getNameVerifyStatus().getCode())) {
			logger.error("登陆账户未进行实名认证");
			mv.addObject("message", "对不起，您的账户尚未实名认证，无法进行提现!");
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		
		try {
			// 根据用户ID查询会员账户
			MemberAccount account = defaultAccountService.queryAccountById(user.getDefaultAccountId(), env);
			mv.addObject("account", account);
			super.setJsonAttribute(request.getSession(), "account", account);
		} catch (Exception e) {
			logger.error("查询可用余额、冻结资金失败", e);
		}
		
		// 查询每月的限额限次
		queryLfltOfMonth(request, user, TradeType.WITHDRAW, mv, null, env);
		
		BankAccRequest req = new BankAccRequest();
		req.setMemberId(this.getMemberId(request));
		req.setClientIp(request.getRemoteAddr());
		req.setPayAttribute("normal");
		
		// 查询永达互联网金融合作银行
		List<BankAccountInfo> baList = null;
		try {
			baList = defaultBankAccountService.queryBankAccount(req);
		} catch (ServiceException e) {
			logger.error("查询银行卡信息失败", e);
		}
		mv.addObject("baList", baList);
		mv.addObject("defaultCardId", getDefaultBankCard(baList));
		
		mv.setViewName(CommonConstant.URL_PREFIX + "/cashing/getcash");
		return mv;
	}
	
	/**
	 * 查询提现的服务费用
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/queryServiceCharge", method = RequestMethod.POST)
	public RestResponse queryServiceCharge(HttpServletRequest request) {
		RestResponse response = new RestResponse();
		String money = request.getParameter("money");
		logger.debug("money={}", money);
		
		String tradeFlag = request.getParameter("tradeFlag");
		String productCodeString = "0".equals(tradeFlag) ? TradeType.WITHDRAW.getBizProductCode() 
				: TradeType.WITHDRAW_INSTANT.getBizProductCode();
		
		getServiceFee(request, money, productCodeString, response);
		return response;
	}
	
	/**
	 * 验证限额限次
	 * @param request
	 * @param cashingForm
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/validateLflt")
	public RestResponse validateLflt(HttpServletRequest request, CashingForm cashingForm) {
		RestResponse response = new RestResponse();
		
		// 获取用户信息
		EnterpriseMember user = getUser(request);
		
		// 提现申请金额
		Money money = new Money(cashingForm.getMoney());
		
		// 服务费用
		String servCharge = cashingForm.getServiceCharge();
		if (StringUtils.isEmpty(servCharge) || "0".equals(servCharge)) {
			servCharge = "0";
			cashingForm.setServiceCharge("0.00");
		}
		Money totalMoney = money.addTo(new Money(servCharge));
		
		// 验证限额限次
		TradeType tradeType = cashingForm.getType() == CashingType.TYPE_T1.getCode() ? TradeType.WITHDRAW : TradeType.WITHDRAW_INSTANT;
		if (!super.validateLflt(user.getMemberId(), "", totalMoney, tradeType, null, response, env)) {
			logger.error("限额限次验证失败");
			return response;
		}
		
		response.setSuccess(true);
		return response;
	}

	/**
	 * 进入提现第二步，提现审核（申请/确认）
	 * 
	 * @return
	 */
	@RequestMapping(value = "/toAuditCashInfo", method = RequestMethod.POST)
	public ModelAndView toAuditCashInfo(CashingForm cashingForm,  
			HttpServletRequest request, HttpSession session, TradeEnvironment env) {
		ModelAndView mv = new ModelAndView();
		
		// 校验提交参数
        String errorMsg = validator.validate(cashingForm);
        if (StringUtils.isNotEmpty(errorMsg)) {
            mv.addObject("message", errorMsg);
            mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
            return mv;
        }
		
		// 获取用户信息
		EnterpriseMember user = getUser(request);
		
		// 记录提现日志
        logger.info(LogUtil.generateMsg(OperateTypeEnum.WITHDRAW_APPLY, user, env,
                CashingType.getDesc(cashingForm.getType())));
		
		// 提现申请金额
		Money money = new Money(cashingForm.getMoney());
		
		// 服务费用
		String servCharge = cashingForm.getServiceCharge();
		if (StringUtils.isEmpty(servCharge) || "0".equals(servCharge)) {
			servCharge = "0";
			cashingForm.setServiceCharge("0.00");
		}
		Money totalMoney = money.addTo(new Money(servCharge));
		
		// 验证限额限次
		TradeType tradeType = cashingForm.getType() == CashingType.TYPE_T1.getCode() ? TradeType.WITHDRAW : TradeType.WITHDRAW_INSTANT;
		if (!super.validateLflt(user.getMemberId(), "", totalMoney, tradeType, mv, null, env)) {
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		
		String bankCode = cashingForm.getBankCode();
		
		// 提现金额大于提现转账余额
		MemberAccount account = super.getJsonAttribute(session, "account", MemberAccount.class);
		if (account != null && totalMoney.compareTo(account.getWithdrawBalance()) == 1) {
			logger.info("提现金额{}大于提现转账余额{}", cashingForm.getMoney(), account.getWithdrawBalance());
			mv.addObject("success", false);
			mv.addObject("message", "提现金额大于账户可用余额");
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		mv.addObject("account", account);
		
		// 查询提现账户
		BankAccRequest req = new BankAccRequest();
		req.setMemberId(this.getMemberId(request));
		req.setClientIp(request.getRemoteAddr());
		req.setBankCode(bankCode);
		BankAccountInfo bankAcctInfo = null;
		try {
			List<BankAccountInfo> baList = defaultBankAccountService.queryBankAccount(req);
			if (ObjectUtils.isListNotEmpty(baList)) {
				for (BankAccountInfo bankAccount : baList) {
					if (bankCode.equals(bankAccount.getBankcardId())) {
						bankAcctInfo = bankAccount;
					}
				}
				
			} else {
				logger.error("查询银行卡信息失败");
				throw new Exception("您的账户未绑定银行卡");
			}
		} catch (Exception e) {
			logger.error("查询银行卡信息失败", e);
			mv.addObject("message", "您的账户未绑定银行卡");
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		
		// 提现方式
		int type = cashingForm.getType();
		String desc = CashingType.getDesc(type);
		cashingForm.setTypeDesc(desc);
		logger.info("提现方式：" + desc);
		
		// 账户信息
		cashingForm.setBankCardId(bankAcctInfo.getBankcardId());
		cashingForm.setBankName(bankAcctInfo.getBankName());
		cashingForm.setAccountNo(bankAcctInfo.getBankAccountNumMask());
		
		mv.addObject("bankAcctInfo", bankAcctInfo);
		mv.addObject("cashingForm", cashingForm);
		mv.addObject("memberName", user.getEnterpriseName());
		
		// 银行账户信息放入会话
		this.setJsonAttribute(session, "bankAcctInfo", bankAcctInfo);
		// 表单参数加入会话
		this.setJsonAttribute(session, "cashingForm", cashingForm);
		
		try {
			//获取提现统一凭证号
			TradeRequestInfo tradeReqest = defaultWithdrawService.applyWithdraw(user, tradeType, env);
			
			//保存原始凭证信息到session中
	        setJsonAttribute(session, CommonConstant.SESSION_ATTR_NAME_CURRENT_CASHING, tradeReqest);
		} catch (BizException e) {
			logger.error("创建提现统一凭证号失败", e);
		}
		
		// 检查用户提现审核权限
		if (auth(request, FunctionType.EW_MY_APPROVE.getCode())) {
			// 有审核提现的权限
			mv.setViewName(CommonConstant.URL_PREFIX + "/cashing/getcash-confirm");
		} else {
			// 自己没有审核权限
			mv.setViewName(CommonConstant.URL_PREFIX + "/cashing/getcash-apply");
		}
		
		// 硬证书是否激活
		if (this.isCertActive(request, "hard", env) || this.isCertActive(request, "soft", env)) {
			mv.addObject("isCertActive", "yes");
		} else {
			mv.addObject("isCertActive", "no");
		}
		
		return mv;
	}

	/**
	 * 完成提现申请
	 * 
	 * @return
	 */
	@RequestMapping(value="/confirmApply", method = RequestMethod.POST)
	public ModelAndView confirmApply(HttpServletRequest request, HttpSession session, TradeEnvironment env) {
		ModelAndView mv = new ModelAndView();
		
		// 先获取用户信息
		EnterpriseMember user = this.getUser(request);
		
		// 获取session中的信息
		CashingForm cashingForm = this.getJsonAttribute(session, "cashingForm", CashingForm.class);
		BankAcctInfo bankAcctInfo = this.getJsonAttribute(session, "bankAcctInfo", BankAcctInfo.class);
		
		// 提现金额大于提现转账余额
		MemberAccount account = super.getJsonAttribute(session, "account", MemberAccount.class);
		if (account != null && new Money(cashingForm.getMoney()).compareTo(account.getWithdrawBalance()) == 1) {
			logger.info("提现金额{}大于提现转账余额{}", cashingForm.getMoney(), account.getWithdrawBalance());
			mv.addObject("message", "提现金额大于账户可用余额");
            mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		mv.addObject("account", account);
		
		// 检查支付密码
		if(!validatePayPassword(request, user, null, mv)) {
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
        
		//生成收款方
        logger.info("转账,生成收款方");
        PartyInfo payee = new PartyInfo();
        payee.setAccountName(bankAcctInfo.getRealName());
        payee.setIdentityNo(bankAcctInfo.getBankAccountNumMask());

        //生成付款方
        PartyInfo payer = TradeReqestUtil.createPay(user.getDefaultAccountId(),
            user.getMemberId(), user.getCurrentOperatorId(), user.getMobile(),
            user.getMemberName(),user.getMemberType());
        payer.setAccountName(user.getLoginName());
        payer.setEnterpriseName(user.getEnterpriseName());
        logger.info("转账,生成付款方");
		
        // 获取原始凭证
        TradeRequestInfo tradeReq = getTradeRequest(request, user, cashingForm.getMoney());
        if (tradeReq == null) {
        	mv.addObject("success", false);
            mv.addObject("message", "提现订单已提交过，请勿重复提交！");
            mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
            return mv;
        }
        tradeReq.setPayee(payee);
        tradeReq.setPayer(payer);
		
        String money = cashingForm.getMoney();
		try {
			Money freezeAmount = null;
			if (StringUtils.isEmpty(money)) {
				throw new Exception("转账总金额不能为空");
			}
			freezeAmount = new Money(money);
			
			// 冻结金额
			String bizPaymentSeqNo = fundsControlService.freeze(user.getMemberId(), user.getDefaultAccountId(), 
					freezeAmount, env);
			if (StringUtils.isEmpty(bizPaymentSeqNo)) {
				logger.error("资金冻结失败");
				throw new Exception("您的账户余额不足！");
			}
			
        	// 保存审核信息
            this.saveAuditInfo(request, tradeReq, money, cashingForm, bankAcctInfo, 
            		user, AuditStatus.AUDIT_WAITING.getCode(), bizPaymentSeqNo);
		} catch (Exception e) {
			logger.error("保存审核信息失败", e);
			mv.addObject("message", e.getMessage());
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
			
		}
		
		mv.addObject("cashingForm", cashingForm);
		mv.addObject("bankAcctInfo", bankAcctInfo);
		mv.addObject("memberName", user.getEnterpriseName());
		mv.setViewName(CommonConstant.URL_PREFIX + "/cashing/getcash-apply-complete");
		return mv;
	}
	
	/**
	 * 直接提现确认
	 * @param request
	 * @param session
	 * @param env
	 * @return
	 */
	@RequestMapping("/completeApply")
	public ModelAndView completeApply(HttpServletRequest request, HttpSession session, TradeEnvironment env) {
		ModelAndView mv = new ModelAndView();
		EnterpriseMember user = getUser(request);
		
		String mobileCaptcha = request.getParameter("mobileCaptcha");
		if (StringUtils.isNotEmpty(mobileCaptcha)) {
			// 检查手机验证码
			if(!validateCaptcha(request, user, null, mv, env)) {
				mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
				return mv;
			}
		} else {
			// 验证硬证书签名
			String payPassword = request.getParameter("payPassword");
			String signedData = request.getParameter("signedData");
			try {
				if(!validateSignature(request, payPassword, signedData, null, mv, env)) {
					mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
					return mv;
				}
			} catch (UnsupportedEncodingException e) {
				logger.error("验证证书时编码错误", e);
				mv.addObject("message", "您未插入快捷盾或证书已经过期！");
				mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
				return mv;
			}
		}
		
		// 检查支付密码
		if(!validatePayPassword(request, user, null, mv)) {
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}

		// 获取session中的信息
		CashingForm cashingForm = this.getJsonAttribute(session, "cashingForm", CashingForm.class);
		BankAcctInfo bankAcctInfo = this.getJsonAttribute(session, "bankAcctInfo", BankAcctInfo.class);
		
		// 检查用户权限
		RestResponse restP = new RestResponse();
		Map<String, String> errorMap = new HashMap<String, String>();
		ModelAndView checkMv = checkUser(user, errorMap, restP);
		if (checkMv != null) {
			return checkMv;
		}
		
		// 查询绑定银行卡,判断银行卡有无联行号
		BankAccRequest req = new BankAccRequest();
		req.setMemberId(user.getMemberId());
		req.setClientIp(request.getRemoteAddr());
		
		// 查询并检查银行账户
		List<BankAccountInfo> bankInfoList = null;
		try {
			bankInfoList = defaultBankAccountService.queryBankAccount(req);
		} catch (ServiceException e) {
			logger.error("查询银行账户信息失败", e);
			mv.addObject("message", "请完善所选银行卡信息！");
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		if (ObjectUtils.isListEmpty(bankInfoList)) {
			logger.info("没有查询到该用户[{}]的银行账户", user.getMemberId());
			mv.addObject("message", "请完善所选银行卡信息！");
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		
		String bankcardId = cashingForm.getBankCardId();
		for (BankAccountInfo bankAccountInfo : bankInfoList) {
			if (bankcardId.equals(bankAccountInfo.getBankcardId())) {
				// 检查银行信息是否完善
				if (StringUtils.isEmpty(bankAccountInfo.getBankBranch())) {
					mv.addObject("message", "请完善所选银行卡信息！");
					mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
					return mv;
				}
			}
		}

		// 提现金额大于提现转账余额
		MemberAccount account = super.getJsonAttribute(session, "account", MemberAccount.class);
		if (account != null && new Money(cashingForm.getMoney()).compareTo(account.getWithdrawBalance()) == 1) {
			logger.info("提现金额{}大于提现转账余额{}", cashingForm.getMoney(), account.getWithdrawBalance());
			mv.addObject("message", "提现金额大于账户可用余额");
            mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		
		mv.addObject("account", account);
		mv.addObject("bankAcctInfo", bankAcctInfo);
		mv.addObject("cashingForm", cashingForm);
		mv.addObject("memberName", user.getEnterpriseName());
		
		//生成收款方
        logger.info("转账,生成收款方");
        PartyInfo payee = new PartyInfo();
        payee.setAccountName(bankAcctInfo.getRealName());
        payee.setIdentityNo(bankAcctInfo.getBankAccountNumMask());

        //生成付款方
        PartyInfo payer = TradeReqestUtil.createPay(user.getDefaultAccountId(),
            user.getMemberId(), user.getCurrentOperatorId(), user.getMobile(),
            user.getMemberName(),user.getMemberType());
        payer.setAccountName(user.getLoginName());
        payer.setEnterpriseName(user.getEnterpriseName());
        logger.info("转账,生成付款方");
		
		//获取原始凭证
        TradeRequestInfo tradeReqest = super.getJsonAttribute(session, CommonConstant.SESSION_ATTR_NAME_CURRENT_CASHING, 
        		TradeRequestInfo.class);
        if (tradeReqest == null) {
        	mv.addObject("success", false);
            mv.addObject("message", "提现订单已提交过，请勿重复提交！");
            mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
            return mv;
        }
        request.getSession().removeAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_CASHING);
        tradeReqest.setAmount(new Money(cashingForm.getMoney()));
        if (cashingForm.getType() == 0) {
        	// 普通提现（T+N）
        	tradeReqest.setTradeType(TradeType.WITHDRAW);
        } else {
        	// 快速提现
        	tradeReqest.setTradeType(TradeType.WITHDRAW_INSTANT);
        }
        tradeReqest.setPayee(payee);
        tradeReqest.setPayer(payer);
		
        String money = cashingForm.getMoney();
		try {
        	// 保存审核信息
            this.saveAuditInfo(request, tradeReqest, money, cashingForm, bankAcctInfo, 
            		user, AuditStatus.AUDIT_PASSED.getCode(), StringUtils.EMPTY);
		} catch (Exception e) {
			logger.error("保存审核信息失败", e);
			mv.addObject("message", e.getMessage());
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
        
		// 调用提现接口
        try {
        	boolean flag = defaultWithdrawService.submitWithdraw(tradeReqest, bankcardId, env);
        	if (!flag) {
            	mv.addObject("message", "提现失败");
            	mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
            	return mv;
            }
		} catch (Exception e) {
			mv.addObject("message", e.getMessage());
        	mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
        	return mv;
		}
        
		mv.setViewName(CommonConstant.URL_PREFIX + "/cashing/getcash-confirm-complete");
		return mv;
	}
	
	/**
	 * 获取原始凭证
	 * @param request 请求
	 * @param user 用户信息
	 * @param money 转账金额
	 * @return 原始凭证
	 */
	private TradeRequestInfo getTradeRequest(HttpServletRequest request,
			EnterpriseMember user, String money) {
		TradeRequestInfo tradeReqest = getJsonAttribute(request.getSession(),
				CommonConstant.SESSION_ATTR_NAME_CURRENT_CASHING,
				TradeRequestInfo.class);
		request.getSession().removeAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_CASHING);
		if (tradeReqest != null) {
			tradeReqest.setAmount(new Money(money));
			com.netfinworks.site.domain.domain.info.PartyInfo payer = tradeReqest.getPayer();
			if (payer != null) {
				payer.setAccountName(user.getLoginName());
				payer.setEnterpriseName(user.getEnterpriseName());
			}
			tradeReqest.setPayer(payer);
		}

		return tradeReqest;
	}
	
	/**
	 * 保存审核信息
	 * @param tradeReqest
	 * @param money
	 * @param cashingForm
	 * @param bankAcctInfo
	 * @param user
	 * @param payee
	 * @param payer
	 * @param status
	 * @param freezeVourceNo
	 */
	private void saveAuditInfo(HttpServletRequest request, TradeRequestInfo tradeReqest, String money, CashingForm cashingForm, BankAcctInfo bankAcctInfo,
			EnterpriseMember user, String status, String freezeVourceNo) {
		// 提交审核申请
		Audit audit = new Audit();
		audit.setTranVoucherNo(tradeReqest.getTradeVoucherNo());
		audit.setAuditType(AuditType.AUDIT_WITHDRAW.getCode());
		audit.setAmount(new Money(money));
		if (StringUtils.isNotEmpty(money)) {
			String feeStr = super.getServiceFee(request, money, tradeReqest.getTradeType().getBizProductCode(), null);
			audit.setFee(new Money(StringUtils.isEmpty(feeStr) ? "0" : feeStr));
		}
		audit.setMemberId(user.getMemberId());
		audit.setOperatorName(user.getOperator_login_name());
		audit.setOperatorId(user.getCurrentOperatorId());
		audit.setStatus(status);
		if(AuditStatus.AUDIT_PASSED.getCode().equals(status)){
			audit.setAuditorName(user.getOperator_login_name());
			audit.setGmtModified(new Date());
		}
		audit.setGmtCreated(new Date());
		audit.setPayeeNo(CommonUtils.getEmptyStr(bankAcctInfo.getBankAccountNumMask()) + "|" 
				+ CommonUtils.getEmptyStr(bankAcctInfo.getRealName()));
		audit.setPayeeBankInfo(bankAcctInfo.getBankBranch());
		audit.setExt(StringUtils.isEmpty(freezeVourceNo) ? StringUtils.EMPTY : freezeVourceNo);
		
		// 验证银行卡有无联行号
		if (StringUtils.isEmpty(bankAcctInfo.getBankBranch())) {
			throw new RuntimeException("请完善所选银行卡信息！");
		}
		
		// 付款信息作为tradeReqest的属性
		Map<String, Object> dataMap = new HashMap<String, Object>();
		if (cashingForm.getType() == 0) {
        	// 普通提现（T+N）
			tradeReqest.setTradeType(TradeType.WITHDRAW);
        } else {
        	// 快速提现（实时）
        	tradeReqest.setTradeType(TradeType.WITHDRAW_INSTANT);
        }
		dataMap.put("tradeReqest", tradeReqest);
		dataMap.put("bankCardId", cashingForm.getBankCardId());
		dataMap.put("payeeBankAcctInfo", bankAcctInfo);
		audit.setAuditData(JSONObject.toJSONString(dataMap));
		auditService.addAudit(audit);
	}
	
	@ResponseBody
	@RequestMapping("/checkPayPassword.htm")
	public RestResponse checkPayPassword(HttpServletRequest request) {
		RestResponse resp = new RestResponse();
		
		// 检查支付密码
		validatePayPassword(request, this.getUser(request), resp, null);
		return resp;
	}
}
