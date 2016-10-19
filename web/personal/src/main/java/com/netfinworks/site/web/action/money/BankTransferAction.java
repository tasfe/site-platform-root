/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年7月8日
 */
package com.netfinworks.site.web.action.money;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.ma.service.base.model.BankAcctDetailInfo;
import com.netfinworks.pbs.service.facade.PayPartyFeeFacade;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.PartyInfo;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.CardAttr;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.account.DefaultAccountService;
import com.netfinworks.site.domainservice.audit.AuditServiceImpl;
import com.netfinworks.site.domainservice.payment.FundsControlService;
import com.netfinworks.site.domainservice.spi.DefaultBankAccountService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.domainservice.spi.DefaultPayPasswdService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.trade.DefaultWithdrawService;
import com.netfinworks.site.domainservice.trade.impl.DefaultTransferServiceImpl;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.action.form.BankTransferForm;
import com.netfinworks.site.web.common.constant.MemoType;
import com.netfinworks.site.web.util.CommonUtils;
import com.netfinworks.site.web.util.TradeReqestUtil;

/**
 * 转账到银行卡
 * @author xuwei
 * @date 2014年8月29日
 */
@Controller
@RequestMapping("bTransfer")
public class BankTransferAction extends BaseAction {
	@Resource(name = "defaultAccountService")
	private DefaultAccountService defaultAccountService;
	
	@Resource(name = "defaultBankAccountService")
	private DefaultBankAccountService defaultBankAccountService;
	
	@Resource(name = "payPartyFeeFacade")
    private PayPartyFeeFacade payPartyFeeFacade;
	
	@Resource(name = "defaultPayPasswdService")
    private DefaultPayPasswdService defaultPayPasswdService;
	
	@Resource(name = "defaultMemberService")
    private DefaultMemberService defaultMemberService;
	
	@Resource(name = "auditService")
    private AuditServiceImpl auditService;
	
	@Resource(name = "defaultWithdrawService")
    private DefaultWithdrawService    defaultWithdrawService;
	
	@Resource(name = "defaultSmsService")
    private DefaultSmsService       defaultSmsService;

    @Resource(name = "defaultUesService")
    private DefaultUesService       defaultUesService;
    
    @Resource(name = "defaultTransferService")
    private DefaultTransferServiceImpl defaultTransferService;
    
    @Resource(name = "fundsControlService")
    private FundsControlService fundsControlService;
    
    @Resource(name = "memberService")
    private MemberService     memberService;
    
    @Resource(name="webResource")
    private WebDynamicResource webResource;
    
	/**
	 * 进入银行转账页面
	 * @return
	 */
	@RequestMapping("/toTransferBank")
	public ModelAndView toTransferBank(HttpServletRequest request,
			HttpServletResponse resp, TradeEnvironment env) {
		ModelAndView mv = new ModelAndView();
		PersonMember user = this.getUser(request);
		
		// 实名认证
		if (user.getMemberId().startsWith("2")) {
			if (!AuthResultStatus.PASS.getCode().equals(user.getNameVerifyStatus().getCode())) {
				logger.error("登陆账户未进行实名认证");
				mv.addObject("message", "对不起，您的账户尚未实名认证，无法进行银行卡转账!");
				mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
				return mv;
			}
		}
		
		MemberAccount account;
		try {
			account = defaultAccountService.queryAccountById(user.getDefaultAccountId(), env);
			mv.addObject("avaBalance", account.getAvailableBalance());
		} catch (ServiceException e) {
			logger.error("查询账户信息失败", e);
		}
		
		mv.addObject("memberType", user.getMemberType().getCode());
		mv.setViewName(CommonConstant.URL_PREFIX + "/bankTransfer/transfer-bank");
		return mv;
	}
	
	/**
	 * 验证限额限次
	 * @param request
	 * @param cashingForm
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/validateLflt")
	public RestResponse validateLflt(HttpServletRequest request, BankTransferForm form, OperationEnvironment env) {
		RestResponse response = new RestResponse();
		
		// 获取用户信息
		PersonMember user = getUser(request);
		
		// 提现申请金额
		Money totalMoney = new Money(form.getTotalMoney());
		
		if (totalMoney.compareTo(new Money("0")) <= 0) {
			response.setMessage("您的转账金额不正确，请重新输入！");
			return response;
		}
		
		// 验证限额限次
		if (!super.validateLflt(user.getMemberId(), "",totalMoney, TradeType.PAY_TO_BANK, null, response, env)) {
			logger.error("限额限次验证失败");
			return response;
		}
		
		response.setSuccess(true);
		return response;
	}
	
	/**
	 * 银行转账确认
	 * @return
	 */
	@RequestMapping("/toConfirmTransferBank")
	public ModelAndView toConfirmTransferBank(BankTransferForm form, HttpServletRequest request,
			HttpServletResponse resp, HttpSession session, TradeEnvironment env) {
		ModelAndView mv = new ModelAndView();
		
		// 校验提交参数
        String errorMsg = validator.validate(form);
        if (StringUtils.isNotEmpty(errorMsg)) {
            mv.addObject("message", errorMsg);
            mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
            return mv;
        }
		
		// 获得用户信息
		PersonMember user = this.getUser(request);
		MemberAccount account = null;
		try {
			// 检查账户
			account = defaultAccountService.queryAccountById(user.getDefaultAccountId(), env);
			mv.addObject("user", user);
			mv.addObject("account", account);
			
			// 检查余额是否足够
			Money avaBalance = account.getAvailableBalance();
			Money totalMoney = new Money(form.getTotalMoney());
			
			// 验证限额限次
			if (!super.validateLflt(user.getMemberId(), "",totalMoney, TradeType.PAY_TO_BANK, mv, null, env)) {
				mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
				return mv;
			}
			
			if (avaBalance.compareTo(totalMoney) == -1) {
				logger.error("账户[{}]余额不足", account.getAccountId());
				mv.addObject("message", "账户余额不足");
				mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
				return mv;
			}
			mv.addObject("avaBalance", avaBalance);
		} catch (Exception e) {
			logger.error("查询账户失败", e);
			mv.addObject("message", "无法获取到您的账户信息!");
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		
		try {
			//获取统一凭证号
			TradeRequestInfo tradeReqest = defaultWithdrawService.applyTransfer(user, TradeType.PAY_TO_BANK, env);
			tradeReqest.setMemo(form.getRemark());
			
			//保存原始凭证信息到session中
	        setJsonAttribute(session, CommonConstant.SESSION_ATTR_NAME_CURRENT_TRANSFER, tradeReqest);
		} catch (BizException e) {
			logger.error("创建提现统一凭证号失败", e);
		}
		
		// 目标账户详细信息
		BankAcctDetailInfo bankAcctInfo = convertToAcctDetailInfo(form);
		
		// 个人会员只能对私转账
		if (form.getAccountType() == 0) {
			bankAcctInfo.setCardAttribute(CardAttr.COMPANY.getCode());
		} else {
			bankAcctInfo.setCardAttribute(CardAttr.PERSONAL.getCode());
		}
		
		if (MemoType.TYPE_OTHERS.getCode() != form.getRemarkType()) {
			// 选择其他时，获取用户输入的备注
			form.setRemark(MemoType.getDesc(form.getRemarkType()));
		}
		//form.setOperLoginName(user.getOperator_login_name());
		form.setAvaBalance(account.getAvailableBalance().toString());
		form.setRecvAcctName(account.getAccountName());
		this.setJsonAttribute(session, "bankTransferForm", form);
		this.setJsonAttribute(session, "bankAcctInfo", bankAcctInfo);
		
		// 将转账相关信息放入页面视图
		mv.addObject("bankAcctInfo", bankAcctInfo);
		form.setAccountNo(CommonUtils.getMaskData(form.getAccountNo()));
		mv.addObject("form", form);
		
		// 硬证书是否激活
		if (this.isCertActive(request, "hard", env) || this.isCertActive(request, "soft", env)) {
			mv.addObject("isCertActive", "yes");
		} else {
			mv.addObject("isCertActive", "no");
		}
		
		mv.setViewName(CommonConstant.URL_PREFIX + "/bankTransfer/transfer-bankcard-confirm");
		return mv;
	}
	
	/**
	 * 确认直接转账
	 * @param request
	 * @param session
	 * @param env
	 * @return
	 */
	@RequestMapping("/confirmTransferBank")
	public ModelAndView confirmTransferBank(HttpServletRequest request, HttpSession session, TradeEnvironment env) {
		ModelAndView mv = new ModelAndView();
		
		// 获得用户信息
		PersonMember user = this.getUser(request);
		
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
			} catch (Exception e) {
				logger.error("验证证书时编码错误", e);
				mv.addObject("message", "您未插入快捷盾或未安装数字证书！");
				mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
				return mv;
			}
		}
		
		// 检查支付密码
		if(!validatePayPassword(request, user, null, mv)) {
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		
		try {
			// 从会话中获取到转账信息
			BankTransferForm form = this.getJsonAttribute(session, "bankTransferForm", BankTransferForm.class);
			BankAcctDetailInfo bankAcctInfo = this.getJsonAttribute(session, "bankAcctInfo", BankAcctDetailInfo.class);
			if (MemoType.TYPE_OTHERS.getCode() != form.getRemarkType()) {
				// 选择其他时，获取用户输入的备注
				form.setRemark(MemoType.getDesc(form.getRemarkType()));
			}
			
			//生成收款方
			Money transMoney = new Money(form.getTransMoney());
	        logger.info("转账,生成收款方");
	        PartyInfo payee = new PartyInfo();
	        payee.setAccountName(bankAcctInfo.getRealName());
	        payee.setIdentityNo(bankAcctInfo.getBankAccountNum());
	        
	        //生成付款方
	        PartyInfo payer = TradeReqestUtil.createPay(user.getDefaultAccountId(),
	            user.getMemberId(), user.getCurrentOperatorId(), user.getMobile(),
	            user.getMemberName(),user.getMemberType());
	        payer.setAccountName(user.getLoginName());
	        payer.setIdentityNo(user.getLoginName());
	        
	        TradeRequestInfo tradeReq = super.getJsonAttribute(session, CommonConstant.SESSION_ATTR_NAME_CURRENT_TRANSFER, TradeRequestInfo.class);
			tradeReq.setMemo(form.getRemark());
            env.setSuccessDispalyUrl(webResource.getWalletAddress() + "/my/home.htm");
            logger.info("转账,生成交易请求");
			
	        tradeReq.setAmount(transMoney);
	        tradeReq.setTradeType(TradeType.PAY_TO_BANK);
	        tradeReq.setSendMessage(form.getSendNoteMsg() == 0 ? true : false);
	        bankAcctInfo.setBranchNo(form.getBranchNo());
	        
	        // 调用提现接口实现转账到银行
	        defaultWithdrawService.submitBankTransfer(tradeReq, bankAcctInfo);
	        
	        form.setAccountNo(CommonUtils.getMaskData(form.getAccountNo()));
	        mv.addObject("form", form);
			mv.addObject("bankAcctInfo", bankAcctInfo);
			mv.setViewName(CommonConstant.URL_PREFIX + "/bankTransfer/transfer-bankcard-success");
		} catch (Exception e) {
			logger.error("提交转账请求失败！", e);
			mv.addObject("success", false);
            mv.addObject("message", e.getMessage());
            mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
            return mv;
		}
		
		return mv;
	}
	
	/**
	 * 获取账户详细信息
	 * @param form
	 * @return
	 */
	private BankAcctDetailInfo convertToAcctDetailInfo(BankTransferForm form) {
		// 目标账户详细信息
		BankAcctDetailInfo bankAcctInfo = new BankAcctDetailInfo();
		bankAcctInfo.setBankName(form.getBankName());
		bankAcctInfo.setRealName(form.getRecvAcctName());
		bankAcctInfo.setBankAccountNum(getTicketFromData(form.getAccountNo()));
		bankAcctInfo.setBankBranch(form.getBranchName());
		bankAcctInfo.setProvince(form.getProvince());
		bankAcctInfo.setCity(form.getCity());
		bankAcctInfo.setMobileNum(form.getMobile());
		bankAcctInfo.setBranchNo(form.getBranchNo());
		bankAcctInfo.setCardAttribute(form.getAccountType());
		return bankAcctInfo;	
	}
	
	/**
	 * 查询银行转账的服务费用
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/queryServiceCharge", method = RequestMethod.POST)
	public RestResponse queryServiceCharge(HttpServletRequest request) {
		RestResponse response = new RestResponse();
		String money = request.getParameter("money");
		logger.debug("money={}", money);
		
		getServiceFee(request, money, TradeType.PAY_TO_BANK.getBizProductCode(), response);
		return response;
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
