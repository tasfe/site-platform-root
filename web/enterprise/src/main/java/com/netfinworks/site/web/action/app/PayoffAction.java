/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年8月5日
 */
package com.netfinworks.site.web.action.app;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.meidusa.fastjson.JSONArray;
import com.netfinworks.batchservice.facade.enums.ProductType;
import com.netfinworks.batchservice.facade.model.BankInfo;
import com.netfinworks.batchservice.facade.model.BatchDetail;
import com.netfinworks.batchservice.facade.model.BatchPayDetail;
import com.netfinworks.batchservice.facade.model.MemberInfo;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.fos.service.facade.enums.CompanyOrPersonal;
import com.netfinworks.mns.client.domain.PageInfo;
import com.netfinworks.payment.common.v2.enums.PartyRole;
import com.netfinworks.pbs.service.context.vo.PartyFeeInfo;
import com.netfinworks.pbs.service.context.vo.PayPricingReq;
import com.netfinworks.pbs.service.context.vo.PaymentPricingResponse;
import com.netfinworks.pbs.service.facade.PayPartyFeeFacade;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.constants.RegexRule;
import com.netfinworks.site.core.common.util.RadomUtil;
import com.netfinworks.site.domain.domain.audit.Audit;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.BankType;
import com.netfinworks.site.domain.enums.CardAttr;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.enums.OperateTypeEnum;
import com.netfinworks.site.domain.enums.PlatformTypeEnum;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.account.DefaultAccountService;
import com.netfinworks.site.domainservice.audit.AuditServiceImpl;
import com.netfinworks.site.domainservice.payment.FundsControlService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.domainservice.trade.DefaultWithdrawService;
import com.netfinworks.site.domainservice.trade.impl.DefaultTransferServiceImpl;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.voucher.VoucherService;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.action.form.PayoffBatchForm;
import com.netfinworks.site.web.action.form.TransferBankBatchForm;
import com.netfinworks.site.web.action.form.TransferKjtBatchForm;
import com.netfinworks.site.web.common.constant.AuditStatus;
import com.netfinworks.site.web.common.constant.AuditSubType;
import com.netfinworks.site.web.common.constant.AuditType;
import com.netfinworks.site.web.common.constant.ErrorMsg;
import com.netfinworks.site.web.common.constant.FunctionType;
import com.netfinworks.site.web.common.constant.PayChannel;
import com.netfinworks.site.web.common.constant.PaymentType;
import com.netfinworks.site.web.common.constant.PayoffType;
import com.netfinworks.site.web.common.util.ObjectUtils;
import com.netfinworks.site.web.common.vo.BankTransfer;
import com.netfinworks.site.web.common.vo.SessionPage;
import com.netfinworks.site.web.common.vo.Transfer;
import com.netfinworks.site.web.util.CommonUtils;
import com.netfinworks.site.web.util.LogUtil;
import com.netfinworks.voucher.service.facade.domain.enums.VoucherInfoType;

/**
 * 应用中心代发工资
 * @author xuwei
 * @date 2014年8月5日
 */
@Controller
@RequestMapping("/payoff")
public class PayoffAction extends BaseAction {
	
    @Resource(name = "defaultAccountService")
    private DefaultAccountService      defaultAccountService;

    @Resource(name = "defaultWithdrawService")
    private DefaultWithdrawService     defaultWithdrawService;

    @Resource(name = "payPartyFeeFacade")
    private PayPartyFeeFacade          payPartyFeeFacade;

    @Resource(name = "webResource")
    private WebDynamicResource         webResource;

    @Resource(name = "defaultMemberService")
    private DefaultMemberService       defaultMemberService;

    @Resource(name = "defaultTransferService")
    private DefaultTransferServiceImpl defaultTransferService;

    @Resource(name = "auditService")
    private AuditServiceImpl           auditService;

    @Resource(name = "fundsControlService")
    private FundsControlService        fundsControlService;

    @Resource(name = "memberService")
    private MemberService              memberService;

    @Resource(name = "voucherService")
    private VoucherService             voucherService;
    
    @Resource(name = "defaultUesService")
    private DefaultUesService          defaultUesService;
	
	/**
	 * 进入代发工资页面
	 * @param request
	 * @return
	 */
	@RequestMapping("/toPayoff.htm")
	public ModelAndView toPayoff(HttpServletRequest request) {
		ModelAndView mv = new ModelAndView();
		// 先获取用户信息
		EnterpriseMember user = getUser(request);
		
		// 实名认证
		if (!AuthResultStatus.PASS.getCode().equals(user.getNameVerifyStatus().getCode())) {
			logger.error("登陆账户未进行实名认证");
			mv.addObject("message", "对不起，您的账户尚未实名认证，无法代发工资!");
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		
		try {
			// 根据用户ID查询会员账户
			MemberAccount account = defaultAccountService.queryAccountById(user.getDefaultAccountId(), env);
			mv.addObject("availableBalance", account.getAvailableBalance());
		} catch (Exception e) {
			logger.error("查询可用余额、冻结资金失败", e);
		}
		
		mv.setViewName(CommonConstant.URL_PREFIX + "/payoff/payoff");
		return mv;
	}
	
	/**
	 * 导入代发工资明细
	 * @param request
	 * @param session
	 * @param batchFile
	 * @param env
	 * @return
	 */
	@RequestMapping("/importBatchPayoff.htm")
	public ModelAndView importBatchPayoff(HttpServletRequest request, HttpSession session, 
			@RequestParam("batchFile") MultipartFile batchFile, TradeEnvironment env) {
		ModelAndView mv = new ModelAndView();
		Workbook xwb = null;
		
		EnterpriseMember user = this.getUser(request);
		if (StringUtils.isEmpty(user.getMobile())) {
		    mv.addObject("message", "请先绑定快捷盾或手机！");
            mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
            return mv;
		}
		
		// 批量转账账户信息
		int transferCount = 0;
		List<PayoffBatchForm> payoffList = new ArrayList<PayoffBatchForm>();
		List<PayoffBatchForm> errorList = new ArrayList<PayoffBatchForm>();
		PayoffBatchForm totalForm = new PayoffBatchForm();
		List<BankTransfer> bankTransferList = new ArrayList<BankTransfer>();
		List<Transfer> kjtTransferList = new ArrayList<Transfer>();
		
		try {
			xwb = WorkbookFactory.create(batchFile.getInputStream());
			
			// 循环工作表Sheet,解析出代发工资明细
			int numOfSheets = xwb.getNumberOfSheets();
			for (int i=0; i<numOfSheets; i++) {
				parseOneBankSheet(request, payoffList, errorList, xwb.getSheetAt(i));
			}
			
			if (errorList.size() != 0) {
			    this.setJsonAttribute(session, "errorList", errorList);
				SessionPage<PayoffBatchForm> sessionPage = new SessionPage<PayoffBatchForm>();
				super.setSessionPage(errorList, sessionPage);
				mv.addObject("sessionPage", sessionPage);
				mv.setViewName(CommonConstant.URL_PREFIX + "/payoff/payoff-confirm-fail");
				return mv;
			} else {
			    session.removeAttribute("errorList");
			}

			if (payoffList.size() == 0) {
				mv.addObject("message", "代发工资明细为空！");
				mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
				return mv;
			}

			// 生成转账信息
			transferCount = generateBatchPayoffInfo(totalForm, this.getUser(request), 
					payoffList, mv, bankTransferList, kjtTransferList, request, env);
			if (transferCount == 0) {
				throw new Exception("您导入的文件未包含有效数据！");
			}
		} catch (BizException e) {
            logger.error("导入Excel文件失败", e);
            mv.addObject("message", e.getMessage());
            mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
            return mv;
        } catch (Exception e) {
			logger.error("导入Excel文件失败", e);
			mv.addObject("message", ErrorMsg.ERROR_EXCEL_FORMAT.getDesc());
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		
		// 获得用户信息
		MemberAccount account = null;
		try {
			// 检查账户
			account = defaultAccountService.queryAccountById(user.getDefaultAccountId(), env);
			mv.addObject("user", user);
		} catch (Exception e) {
			logger.error("查询账户失败", e);
			mv.addObject("message", "您的账户未绑定银行卡");
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		
		// 检查余额是否足够
		Money avaBalance = account.getAvailableBalance();
		Money totalMoney = null;
		try {
			totalMoney = new Money(totalForm.getTotalMoney());
			
			// 验证限额限次
//			if (!super.validateLflt(user.getMemberId(), totalMoney, TradeType.pay, mv, null, env)) {
//				mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
//				return mv;
//			}
		} catch (Exception e) {
			logger.error("金额过大或格式不正确", e);
			mv.addObject("message", "账户余额不足[批量转账支出金额" + totalForm.getTotalMoney() + "大于账户余额" + avaBalance + "]");
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		
		if (avaBalance.compareTo(totalMoney) == -1) {
			logger.error("账户[{}]余额不足", account.getAccountId());
			mv.addObject("message", "账户余额不足[批量转账支出金额" + totalMoney + "大于账户余额" + avaBalance + "]");
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		
		mv.addObject("avaBalance", avaBalance);
		
		// 把相关信息放到Session中
		this.setJsonAttribute(session, "payoffTotalForm", totalForm);
		this.setJsonAttribute(session, "bankTransferList", bankTransferList);
		this.setJsonAttribute(session, "kjtTransferList", kjtTransferList);
		this.setJsonAttribute(session, "transferCount", transferCount);
		
		if (auth(request, FunctionType.EW_MY_APPROVE.getCode())) {
			// 有转账权限
			mv.setViewName(CommonConstant.URL_PREFIX + "/payoff/payoff-confirm");
		} else {
			// 无转账权限
			mv.setViewName(CommonConstant.URL_PREFIX + "/payoff/payoff-apply-confirm");
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
	 * 从会话中分页查询记录
	 * @param sessionPage
	 * @param session
	 * @return
	 */
	@ResponseBody
   	@RequestMapping("/getSessionPage")
   	public RestResponse getSessionPage(PageInfo page, HttpSession session, String type) {
		RestResponse response = new RestResponse();
		
		String key = "payoffList";
		if ("error".equalsIgnoreCase(type)) {
		    key = "errorList";
		}
		
		try {
		    List<?> list = this.getJsonAttribute(session, key, List.class);
 			List<PayoffBatchForm> formList = getPayOffList(list);
			SessionPage<PayoffBatchForm> sessionPage = new SessionPage<PayoffBatchForm>(page, new ArrayList<PayoffBatchForm>());
			super.setSessionPage(formList, sessionPage);
			response.setSuccess(true);
			response.setMessageObj(sessionPage);
		} catch (Exception e) {
			logger.error("查询分页信息出错", e);
			response.setMessage("查询分页信息出错");
		}
		
		return response;
   	}
	
	/**
	 * 确认批量转账申请
	 * @param request
	 * @param session
	 * @param env
	 * @return
	 */
	@RequestMapping("/confirmBatchPayoffApply.htm")
	public ModelAndView confirmBatchPayoffApply(HttpServletRequest request, HttpSession session, TradeEnvironment env) {
		ModelAndView mv = new ModelAndView();
		
		// 获得用户信息
		EnterpriseMember user = this.getUser(request);
		
		logger.info(LogUtil.generateMsg(OperateTypeEnum.GONGZI_APPLY, user, env, StringUtils.EMPTY));
		
		// 检查支付密码
		if(!validatePayPassword(request, user, null, mv)) {
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		
		// 从会话中获取转账信息
 		PayoffBatchForm form = this.getJsonAttribute(session, "payoffTotalForm", PayoffBatchForm.class);
		List<PayoffBatchForm> payoffList = getPayOffList(this.getJsonAttribute(session, "payoffList", List.class));
		Integer transferCount = this.getJsonAttribute(session, "transferCount", Integer.class);
		
		// 调用转账服务
		try {
			this.submitBatchPayoffApply(request, payoffList, form, user, StringUtils.EMPTY, env);
		} catch (Exception e) {
			logger.error("转账失败", e);
			mv.addObject("success", false);
			mv.addObject("message", e.getMessage());
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		
 		form.setTransferCount(transferCount);
 		mv.addObject("form", form);
 		//mv.addObject("payoffList", payoffList);
		mv.setViewName(CommonConstant.URL_PREFIX + "/payoff/payoff-apply-success");
		
		return mv;
	}
	
	/**
	 * 确认代发工资
	 * @param request
	 * @param session
	 * @param env
	 * @return
	 */
	@RequestMapping("/confirmBatchPayoff.htm")
	public ModelAndView confirmBatchPayoff(HttpServletRequest request, HttpSession session, TradeEnvironment env) {
		ModelAndView mv = new ModelAndView();
		
		// 获得用户信息
		EnterpriseMember user = this.getUser(request);
		
		logger.info(LogUtil.generateMsg(OperateTypeEnum.GONGZI_APPLY, user, env, StringUtils.EMPTY));
		
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
		
		// 从会话中获取转账信息
 		PayoffBatchForm form = this.getJsonAttribute(session, "payoffTotalForm", PayoffBatchForm.class);
 		List<PayoffBatchForm> payoffList = getPayOffList(this.getJsonAttribute(session, "payoffList", List.class));
		Integer transferCount = this.getJsonAttribute(session, "transferCount", Integer.class);
		
		// 调用永达互联网金融转账服务
		try {
		    this.submitBatchPayoff(request, payoffList, form, user, StringUtils.EMPTY, env);
		} catch (Exception e) {
			logger.error("转账失败", e);
			mv.addObject("success", false);
			mv.addObject("message", e.getMessage());
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		
 		form.setTransferCount(transferCount);
 		mv.addObject("form", form);
// 		mv.addObject("transferList", bankTransferList);
		mv.setViewName(CommonConstant.URL_PREFIX + "/payoff/payoff-success");
		
		return mv;
	}
	
	/*@SuppressWarnings("unchecked")
	private void submitBatchBankTransferApply(HttpServletRequest request, List<BankTransfer> transferList, PayoffBatchForm form, 
			EnterpriseMember user, String freezeVourceNo, TradeEnvironment env) throws Exception {
		if (ObjectUtils.isListEmpty(transferList)) {
			return;
		}
		
		HttpSession session = request.getSession();
		
		//获取原始凭证
		Map<String, JSONObject> tradeMap = super.getJsonAttribute(request.getSession(), 
        		CommonConstant.SESSION_ATTR_NAME_CURRENT_BATCH_PAYOFF, Map.class);
        if (tradeMap == null) {
        	logger.error("提现订单已提交过，请勿重复提交！");
        	throw new Exception("提现订单已提交过，请勿重复提交！");
        }
        session.removeAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_BATCH_PAYOFF);
		
		for (BankTransfer transfer : transferList) {
			BankAcctDetailInfo bankAcctInfo = convertToAcctDetailInfo(transfer);
			JSONObject tradeReqJson = tradeMap.get(String.valueOf(transfer.getOrderNo()));
			TradeRequestInfo tradeReqest = JSONArray.parseObject(String.valueOf(tradeReqJson), TradeRequestInfo.class);
			
			// 生成付款收款人信息
			this.generatePayerInfo(tradeReqest, user, bankAcctInfo, env);
			
			// 保存审核信息
			this.saveBankAuditInfo(request, tradeReqest, new Money(transfer.getMoney()), transfer, 
					bankAcctInfo, user, AuditStatus.AUDIT_WAITING.getCode(), freezeVourceNo);
		}
	}*/
	
//	/**
//	 * 生成收款付款信息
//	 * @param tradeReqest
//	 * @param user
//	 * @param memberId
//	 * @param env
//	 * @throws RuntimeException
//	 */
//	private void generatePayerInfo(TradeRequestInfo tradeReqest, EnterpriseMember user, 
//			BankAcctDetailInfo bankAcctInfo, OperationEnvironment env) throws RuntimeException {
//        //生成收款方
//        logger.info("转账,生成收款方");
//        PartyInfo payee = new PartyInfo();
//        payee.setAccountName(bankAcctInfo.getRealName());
//        payee.setIdentityNo(bankAcctInfo.getBankAccountNum());
//
//        //生成付款方
//        PartyInfo payer = TradeReqestUtil.createPay(user.getDefaultAccountId(),
//            user.getMemberId(), user.getCurrentOperatorId(), user.getMobile(),
//            user.getMemberName(),user.getMemberType());
//        payer.setAccountName(user.getLoginName());
//        payer.setEnterpriseName(user.getEnterpriseName());
//        logger.info("转账,生成付款方");
//
//        //生成交易请求
//        logger.info("转账,生成交易请求");
//        tradeReqest.setPayee(payee);
//        tradeReqest.setPayer(payer);
//	}
	
//	/**
//	 * 获取账户详细信息
//	 * @param form
//	 * @return
//	 */
//	private BankAcctDetailInfo convertToAcctDetailInfo(BankTransfer transfer) {
//		// 目标账户详细信息
//		BankAcctDetailInfo bankAcctInfo = new BankAcctDetailInfo();
//		bankAcctInfo.setBankName(transfer.getBankName());
//		bankAcctInfo.setRealName(transfer.getName());
//		bankAcctInfo.setBankAccountNum(transfer.getAccountNoMask());
//		bankAcctInfo.setBankBranch(transfer.getBranchName());
//		bankAcctInfo.setProvince(transfer.getProvName());
//		bankAcctInfo.setCity(transfer.getCityName());
//		bankAcctInfo.setMobileNum(transfer.getMobile());
//		bankAcctInfo.setCardAttribute(transfer.getCardAttribute());
//		return bankAcctInfo;	
//	}
	
	/**
	 * 提交批量代发工资申请
	 * @param transferList
	 * @param form
	 * @param user
	 */
	private boolean submitBatchPayoffApply(HttpServletRequest request, List<PayoffBatchForm> payoffList, PayoffBatchForm form, 
			EnterpriseMember user, String freezeVourceNo, TradeEnvironment env) throws Exception {
		if (ObjectUtils.isListEmpty(payoffList)) {
			logger.debug("代发工资的目标账户为空");
			return false;
		}
		
		List<BatchDetail> batchDetailList = new ArrayList<BatchDetail>();
		int size = payoffList.size();
		Money totalFee = new Money("0");
		for (int i=0; i<size; i++) {
		    PayoffBatchForm payoffForm = payoffList.get(i);
		    if (payoffForm != null && payoffForm.getPayoffType() == PayoffType.PAYOFF_TO_KJT.getCode()) {
		        // 代发工资到永达互联网金融账户
		        TransferKjtBatchForm kjtBatchForm = payoffForm.getKjtTransfer();
		        
		        // 生成付款到账户的明细
	            BatchDetail batchDetail = this.generateKjtPayDetail(kjtBatchForm, request);
	            batchDetailList.add(batchDetail);
		        
	            if (StringUtils.isNotEmpty(kjtBatchForm.getTransferAmount())) {
                    String feeStr = super.getServiceFee(request, kjtBatchForm.getTransferAmount(),
                            TradeType.PAYOFF_TO_KJT.getBizProductCode(), null);
                    if (StringUtils.isNotEmpty(feeStr)) {
                        totalFee.addTo(new Money(feeStr));
                    }
                }
		    } else {
		        // 代发工资到银行卡
		        TransferBankBatchForm bankBatchForm = payoffForm.getBankTransfer();
		        
		        // 生成付款到银行卡的明细
                BatchDetail batchDetail = this.generateBankPayDetail(bankBatchForm, request);
                batchDetailList.add(batchDetail);
                
                if (StringUtils.isNotEmpty(bankBatchForm.getTransferAmount())) {
                    String feeStr = super.getServiceFee(request, bankBatchForm.getTransferAmount(),
                            TradeType.PAYOFF_TO_BANK.getBizProductCode(), null);
                    if (StringUtils.isNotEmpty(feeStr)) {
                        totalFee.addTo(new Money(feeStr));
                    }
                }
		    }
		}
		
		String batchVourchNo = voucherService.regist(VoucherInfoType.TRADE.getCode());
		defaultTransferService.batchTransferApply(batchVourchNo,batchVourchNo, ProductType.PAY, user, batchDetailList, form.getTransMoney());
        try {
            // 保存批量审核信息
            this.saveBatchAuditInfo(request, batchVourchNo, form.getTransMoney(), user, 
                    AuditStatus.AUDIT_WAITING.getCode(), StringUtils.EMPTY, totalFee.toString());
        } catch (Exception e) {
            logger.error("保存批量转账到卡的审核信息失败", e);
            throw new Exception("保存批量转账到卡的审核信息失败！");
        }
		
		return true;
	}
	
	/**
     * 提交批量代发工资
     * @param transferList
     * @param form
     * @param user
     */
    private boolean submitBatchPayoff(HttpServletRequest request, List<PayoffBatchForm> payoffList, PayoffBatchForm form, 
            EnterpriseMember user, String freezeVourceNo, TradeEnvironment env) throws Exception {
        if (ObjectUtils.isListEmpty(payoffList)) {
            logger.debug("代发工资的目标账户为空");
            return false;
        }
        
        List<BatchDetail> batchDetailList = new ArrayList<BatchDetail>();
        int size = payoffList.size();
        Money totalFee = new Money("0");
        for (int i=0; i<size; i++) {
            PayoffBatchForm payoffForm = payoffList.get(i);
            if (payoffForm != null && payoffForm.getPayoffType() == PayoffType.PAYOFF_TO_KJT.getCode()) {
                // 代发工资到永达互联网金融账户
                TransferKjtBatchForm kjtBatchForm = payoffForm.getKjtTransfer();
                
                // 生成付款到账户的明细
                BatchDetail batchDetail = this.generateKjtPayDetail(kjtBatchForm, request);
                batchDetailList.add(batchDetail);
                
                if (StringUtils.isNotEmpty(kjtBatchForm.getTransferAmount())) {
                    String feeStr = super.getServiceFee(request, kjtBatchForm.getTransferAmount(),
                            TradeType.PAYOFF_TO_KJT.getBizProductCode(), null);
                    if (StringUtils.isNotEmpty(feeStr)) {
                        totalFee.addTo(new Money(feeStr));
                    }
                }
            } else {
                // 代发工资到银行卡
                TransferBankBatchForm bankBatchForm = payoffForm.getBankTransfer();
                
                // 生成付款到银行卡的明细
                BatchDetail batchDetail = this.generateBankPayDetail(bankBatchForm, request);
                batchDetailList.add(batchDetail);
                
                if (StringUtils.isNotEmpty(bankBatchForm.getTransferAmount())) {
                    String feeStr = super.getServiceFee(request, bankBatchForm.getTransferAmount(),
                            TradeType.PAYOFF_TO_BANK.getBizProductCode(), null);
                    if (StringUtils.isNotEmpty(feeStr)) {
                        totalFee.addTo(new Money(feeStr));
                    }
                }
            }
        }
        
        String batchVourchNo = voucherService.regist(VoucherInfoType.TRADE.getCode());
        defaultTransferService.batchTransferSubmit(batchVourchNo,batchVourchNo, ProductType.PAY, user, batchDetailList, form.getTransMoney());
        
        try {
            logger.info(LogUtil.generateMsg(OperateTypeEnum.GONGZI_EXAMINE, user, env, "通过"));
            // 保存批量审核信息
            this.saveBatchAuditInfo(request, batchVourchNo, form.getTransMoney(), user, 
                    AuditStatus.AUDIT_PASSED.getCode(), StringUtils.EMPTY, totalFee.toString());
        } catch (Exception e) {
            logger.error("保存批量转账到卡的审核信息失败", e);
            throw new Exception("保存批量转账到卡的审核信息失败！");
        }
        return true;
    }
	
	/**
     * 保存批量审核信息
     * @param tradeReqest
     * @param money
     * @param bankAcctInfo
     * @param user
     * @param payee
     * @param payer
     * @param status
     * @param freezeVourceNo
     */
    private void saveBatchAuditInfo(HttpServletRequest request, String tradeVoucherNo, String amount, 
            EnterpriseMember user, String status, String freezeVourceNo, String totalFeeStr) {
        // 提交审核申请
        Audit audit = new Audit();
        audit.setTranVoucherNo(tradeVoucherNo);
        audit.setAuditType(AuditType.AUDIT_PAYOFF_KJT.getCode());
        audit.setAuditSubType(AuditSubType.BATCH.getCode());
        audit.setFee(new Money(totalFeeStr));
        audit.setAmount(new Money(amount));
        audit.setMemberId(user.getMemberId());
        audit.setOperatorName(user.getOperator_login_name());
        audit.setOperatorId(user.getCurrentOperatorId());
        audit.setStatus(status);
        if(AuditStatus.AUDIT_PASSED.getCode().equals(status)){
            audit.setAuditorName(user.getOperator_login_name());
            audit.setGmtModified(new Date());
        }
        audit.setGmtCreated(new Date());
        audit.setExt(StringUtils.isEmpty(freezeVourceNo) ? StringUtils.EMPTY : freezeVourceNo);
        
        auditService.addAudit(audit);
    }
	
//	/**
//	 * 保存银行转账（代发工资）审核信息
//	 * @param tradeReqest
//	 * @param money
//	 * @param transfer
//	 * @param bankAcctInfo
//	 * @param user
//	 * @param status
//	 * @param freezeVourceNo
//	 */
//	private void saveBankAuditInfo(HttpServletRequest request, TradeRequestInfo tradeReqest, Money money, BankTransfer transfer, 
//			BankAcctDetailInfo bankAcctInfo, EnterpriseMember user, String status, String freezeVourceNo) {
//		// 提交审核申请
//		Audit audit = new Audit();
//		audit.setTranVoucherNo(tradeReqest.getTradeVoucherNo());
//		audit.setAuditType(AuditType.AUDIT_PAYOFF_BANK.getCode());
//		audit.setAmount(new Money(transfer.getMoney()));
//		if (StringUtils.isNotEmpty(transfer.getMoney())) {
//			String feeStr = super.getServiceFee(request, transfer.getMoney(), tradeReqest.getTradeType().getBizProductCode(), null);
//			audit.setFee(new Money(StringUtils.isEmpty(feeStr) ? "0" : feeStr));
//		}
//		audit.setMemberId(user.getMemberId());
//		audit.setOperatorName(user.getOperator_login_name());
//		audit.setOperatorId(user.getCurrentOperatorId());
//		audit.setStatus(status);
//		if(AuditStatus.AUDIT_PASSED.getCode().equals(status)){
//			audit.setAuditorName(user.getOperator_login_name());
//			audit.setGmtModified(new Date());
//		}
//		audit.setGmtCreated(new Date());
//		audit.setExt(freezeVourceNo);
//		audit.setPayeeNo(CommonUtils.getEmptyStr(bankAcctInfo.getBankAccountNum()) + "|" 
//				+ CommonUtils.getEmptyStr(bankAcctInfo.getRealName()));
//		audit.setPayeeBankInfo(bankAcctInfo.getBankBranch());
//		
//		// 验证银行卡有无联行号
//		if (StringUtils.isEmpty(bankAcctInfo.getBankName())) {
//			throw new RuntimeException("交易编号为" + audit.getTranVoucherNo() + "的银行卡没有联行号");
//		}
//		
//		// 付款信息作为tradeReqest的属性
//		Map<String, Object> dataMap = new HashMap<String, Object>();
//		dataMap.put("tradeReqest", tradeReqest);
//		dataMap.put("payeeBankAcctInfo", bankAcctInfo);
//		audit.setAuditData(JSONObject.toJSONString(dataMap));
//		auditService.addAudit(audit);
//	}
	
//	/**
//	 * 保存永达互联网金融转账（代发工资）审核信息
//	 * @param tradeReqest
//	 * @param money
//	 * @param transfer
//	 * @param account
//	 * @param user
//	 * @param payee
//	 * @param payer
//	 * @param status
//	 * @param freezeVourceNo
//	 */
//	private void saveKjtAuditInfo(HttpServletRequest request, TradeRequestInfo tradeReqest, Money money, Transfer transfer, MemberAccount account,
//			EnterpriseMember user, PartyInfo payee, PartyInfo payer, String status, String freezeVourceNo) {
//		Map<String,Object> dataMap = new HashMap<String,Object>();
//        dataMap.put("tradeReqest", tradeReqest);
//        dataMap.put("transferInfo", transfer.getRemark());
//        dataMap.put("account", user.getMemberName());
//        dataMap.put("loginName", user.getOperator_login_name());
//        dataMap.put("accountBalance", account.getAvailableBalance());
//        dataMap.put("identityNo", transfer.getContact());
//        dataMap.put("transferNum", transfer.getMoney());
//        dataMap.put("transferInfo", transfer.getRemark());
//        
//		 /*提交转账审核 */
//        Audit audit = new Audit();
//        audit.setTranVoucherNo(tradeReqest.getTradeVoucherNo());
//        audit.setAuditType(AuditType.AUDIT_PAYOFF_KJT.getCode());
//        audit.setAmount(money);
//        if (money != null) {
//			String feeStr = super.getServiceFee(request, money.toString(), tradeReqest.getTradeType().getBizProductCode(), null);
//			audit.setFee(new Money(StringUtils.isEmpty(feeStr) ? "0" : feeStr));
//		}
//        audit.setMemberId(user.getMemberId());
//        audit.setOperatorName(user.getOperator_login_name());
//        audit.setOperatorId(user.getCurrentOperatorId());
//        audit.setStatus(status);
//        if(AuditStatus.AUDIT_PASSED.getCode().equals(status)){
//			audit.setAuditorName(user.getOperator_login_name());
//			audit.setGmtModified(new Date());
//		}
//        audit.setGmtCreated(new Date());
//        audit.setAuditData(JSONObject.toJSONString(dataMap));
//        audit.setExt(freezeVourceNo);
//        audit.setPayeeNo(transfer.getContact() + "|" + payee.getMemberName());
//        
//        auditService.addAudit(audit);
//	}
	
	/**
     * 生成付款详细信息
     * @param transfer 转账付款信息
     * @param request Http请求对象
     * @return 付款详细信息，作为服务层接口参数
     * @throws Exception 抛出生成过程中异常
     */
    private BatchDetail generateKjtPayDetail(TransferKjtBatchForm transfer, HttpServletRequest request) throws Exception {
        BatchPayDetail batchDetail = new BatchPayDetail();
        batchDetail.setTradeType(TradeType.PAYOFF_TO_KJT.getPayCode());
        batchDetail.setSourceDetailNo(RadomUtil.getUUId());
        batchDetail.setAmount(new BigDecimal(transfer.getTransferAmount()));
        batchDetail.setMemo(transfer.getRemark());
        batchDetail.setPartyInfos(generateKjtPartyInfoList(transfer, super.getUser(request)));
     
        return batchDetail;
    }
    
    /**
     * 生成付款详细信息
     * @param transfer 转账到卡付款信息
     * @param request Http请求对象
     * @return 付款详细信息，作为服务层接口参数
     * @throws Exception 抛出生成过程中异常
     */
    private BatchDetail generateBankPayDetail(TransferBankBatchForm transfer, HttpServletRequest request) throws Exception {
        BatchPayDetail batchDetail = new BatchPayDetail();
        batchDetail.setTradeType(TradeType.PAYOFF_TO_BANK.getPayCode());
        batchDetail.setSourceDetailNo(RadomUtil.getUUId());
        batchDetail.setAmount(new BigDecimal(transfer.getTransferAmount()));
        batchDetail.setMemo(transfer.getRemark());
        batchDetail.setPartyInfos(generateBankPartyInfoList(transfer, super.getUser(request)));
     
        return batchDetail;
    }
    
    /**
     * 生成交易方信息列表
     * @param transfer 转账信息
     * @param user 登陆用户
     * @return 交易方信息列表
     */
    private List<com.netfinworks.batchservice.facade.model.PartyInfo> generateKjtPartyInfoList(TransferKjtBatchForm transfer, EnterpriseMember user) {
        List<com.netfinworks.batchservice.facade.model.PartyInfo> partyList 
            = new ArrayList<com.netfinworks.batchservice.facade.model.PartyInfo>();
        
        BigDecimal amount = new BigDecimal(transfer.getTransferAmount());
        
        // 付款方信息
        com.netfinworks.batchservice.facade.model.PartyInfo payerInfo 
            = new com.netfinworks.batchservice.facade.model.PartyInfo();
        payerInfo.setPartyRole(PartyRole.PAYER.getCode());
        payerInfo.setAmount(amount);
        payerInfo.setPaymentInfo(getPayerInfo(user));
        partyList.add(payerInfo);
        
        // 收款方信息
        com.netfinworks.batchservice.facade.model.PartyInfo payeeInfo 
            = new com.netfinworks.batchservice.facade.model.PartyInfo();
        payeeInfo.setPartyRole(PartyRole.PAYEE.getCode());
        payeeInfo.setAmount(amount);
        payeeInfo.setPaymentInfo(getPayeeInfo(transfer.getKjtAccount()));
        partyList.add(payeeInfo);
        
        return partyList;
    }
    
    /**
     * 生成交易方信息列表
     * @param transfer 转账到卡信息
     * @param user 登陆用户
     * @return 交易方信息列表
     * @throws ServiceException 
     */
    private List<com.netfinworks.batchservice.facade.model.PartyInfo> generateBankPartyInfoList(
            TransferBankBatchForm transfer, EnterpriseMember user) throws ServiceException {
        List<com.netfinworks.batchservice.facade.model.PartyInfo> partyList
            = new ArrayList<com.netfinworks.batchservice.facade.model.PartyInfo>();
        BigDecimal amount = new BigDecimal(transfer.getTransferAmount());
        
        // 付款方信息
        com.netfinworks.batchservice.facade.model.PartyInfo payerInfo 
            = new com.netfinworks.batchservice.facade.model.PartyInfo();
        payerInfo.setPartyRole(PartyRole.PAYER.getCode());
        payerInfo.setAmount(amount);
        payerInfo.setPaymentInfo(getPayerInfo(user));
        partyList.add(payerInfo);
        
        // 收款方信息
        com.netfinworks.batchservice.facade.model.PartyInfo payeeInfo 
            = new com.netfinworks.batchservice.facade.model.PartyInfo();
        payeeInfo.setPartyRole(PartyRole.PAYEE.getCode());
        payeeInfo.setAmount(amount);
        payeeInfo.setPaymentInfo(getBankPayeeInfo(transfer));
        partyList.add(payeeInfo);
        
        return partyList;
    }
    
    /**
     * 获取付款人信息
     * @param user 登陆用户
     * @return 付款人信息
     */
    private MemberInfo getPayerInfo(EnterpriseMember user) {
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setPartyId(user.getMemberId());
        memberInfo.setPartyAccountNo(user.getDefaultAccountId());
        return memberInfo;
    }
    
    /**
     * 获取收款人信息
     * @param loginId 登陆账户名
     * @return 收款人信息
     */
    private MemberInfo getPayeeInfo(String loginId) {
        MemberInfo memberInfo = new MemberInfo(); 
        memberInfo.setLoginId(loginId);
        memberInfo.setPlatformType(String.valueOf(PlatformTypeEnum.UID.getCode()));
        return memberInfo;
    }
    
    /**
     * 获取收款人信息
     * @param loginId 登陆账户名
     * @return 收款人信息
     * @throws ServiceException 
     */
    private BankInfo getBankPayeeInfo(TransferBankBatchForm transfer) throws ServiceException {
        BankInfo bankInfo = new BankInfo();
        bankInfo.setName(transfer.getAccountName());
        bankInfo.setAccountNo(defaultUesService.encryptData(transfer.getAccountNo()));
        BankType bankType = BankType.getByMsg(transfer.getBankName());
        bankInfo.setBankCode((bankType == null) ? StringUtils.EMPTY : bankType.getCode());
        bankInfo.setBankName(transfer.getBankName());
        bankInfo.setBranchName(transfer.getBranchBankName());
        bankInfo.setBankLineNo(StringUtils.EMPTY);//目前无法获取到
        bankInfo.setProvince(transfer.getBankProvince());
        bankInfo.setCity(transfer.getBankCity());
        String companyOrPersonal = (CardAttr.PERSONAL.getCode() == transfer.getAccountType()) ? 
                CompanyOrPersonal.PERSONAL.getCode() : CompanyOrPersonal.COMPANY.getCode();
        bankInfo.setCompanyOrPersonal(companyOrPersonal);
        return bankInfo;
    }
	
//	/**
//	 * 提交循环提交批量转账
//	 * @param request
//	 * @param transferList
//	 * @param form
//	 * @param env
//	 * @return 转账成功笔数
//	 */
//	@SuppressWarnings("unchecked")
//	private int submitBatchBankTransfer(HttpServletRequest request, List<BankTransfer> transferList, PayoffBatchForm form, 
//			TradeEnvironment env) throws Exception {
//		int successCount = 0;
//		if (ObjectUtils.isListEmpty(transferList)) {
//			return successCount;
//		}
//		
//		Money successAmount = new Money();
//		Money failAmount = new Money();
//		
//		//获取原始凭证
//		HttpSession session = request.getSession();
//		Map<String, JSONObject> tradeMap = super.getJsonAttribute(request.getSession(), 
//        		CommonConstant.SESSION_ATTR_NAME_CURRENT_PAYOFF, Map.class);
//		session.removeAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_PAYOFF);
//        if (tradeMap == null) {
//        	logger.error("提现订单已提交过，请勿重复提交！");
//        	throw new Exception("提现订单已提交过，请勿重复提交！");
//        }
//		
//		for (BankTransfer transfer : transferList) {
//			if (StringUtils.isEmpty(transfer.getAccountNoMask())) {
//				continue;
//			}
//			
//			BankAcctDetailInfo bankAcctInfo = new BankAcctDetailInfo();
//			bankAcctInfo.setBankName(transfer.getBankName());
//			bankAcctInfo.setBankAccountNum(transfer.getAccountNoMask());
//			bankAcctInfo.setRealName(transfer.getName());
//			bankAcctInfo.setBankBranch(transfer.getBranchName());
//			bankAcctInfo.setProvince(transfer.getProvName());
//			bankAcctInfo.setCity(transfer.getCityName());
//			bankAcctInfo.setCardAttribute(transfer.getCardAttribute());
//			
//			try {
//				// 解析出交易凭证
//				JSONObject tradeReqJson = tradeMap.get(String.valueOf(transfer.getOrderNo()));
//				TradeRequestInfo tradeReq = JSONArray.parseObject(String.valueOf(tradeReqJson), TradeRequestInfo.class);
//				
//				// 当前登陆会员用户
//				EnterpriseMember user = this.getUser(request);
//				
//				// 生成付款收款人信息
//				this.generatePayerInfo(tradeReq, user, bankAcctInfo, env);
//				
//				// 保存审核信息
//				this.saveBankAuditInfo(request, tradeReq, new Money(transfer.getMoney()), transfer, bankAcctInfo, 
//						 user, AuditStatus.AUDIT_PASSED.getCode(), StringUtils.EMPTY);
//				
//				// 执行转账
//				transferToBankAccount(request, session, new Money(transfer.getMoney()), bankAcctInfo, tradeReq);
//				successCount++;
//				successAmount.addTo(new Money(transfer.getMoney()));
//			} catch (Exception recordMsg) {
//				failAmount.addTo(new Money(transfer.getMoney()));
//				transfer.setSuccess(false);
//				String msg = recordMsg.getMessage();
//				transfer.setErrorMsg(StringUtils.isEmpty(msg) ? "账户信息不正确" : msg);
//			}
//		}
//		
//		form.setTransferSuccessCount(successCount);
//		form.setTransferSuccessAmount(successAmount.toString());
//		form.setTransferFailCount(transferList.size() - successCount);
//		form.setTransferFailAmount(failAmount.toString());
//		
//		return successCount;
//	}
	
//	/**
//	 * 转账银行账户
//	 * @param request
//	 * @param session
//	 * @param form
//	 * @param bankAcctInfo
//	 * @return true-成功，false-失败
//	 * @throws Exception 
//	 */
//	private void transferToBankAccount(HttpServletRequest request, HttpSession session, Money transMoney,
//			BankAcctDetailInfo bankAcctInfo, TradeRequestInfo tradeReq) throws Exception {
//		
//		if (tradeReq == null) {
//			tradeReq = super.getJsonAttribute(session, CommonConstant.SESSION_ATTR_NAME_CURRENT_PAYOFF, TradeRequestInfo.class);
//			session.removeAttribute(CommonConstant.SESSION_ATTR_NAME_CURRENT_PAYOFF);
//		}
//        tradeReq.setAmount(transMoney);
//        tradeReq.setTradeType(TradeType.PAYOFF_TO_BANK);
//        
//		// 调用提现接口实现转账到银行
//        defaultWithdrawService.submitBankTransfer(tradeReq, bankAcctInfo);
//	}
	
	/**
	 * 解析PayoffBatchForm的JSON字符串列表
	 * @param list
	 * @return
	 */
	private List<PayoffBatchForm> getPayOffList(List<?> list) {
		if (ObjectUtils.isListEmpty(list)) {
			return null;
		}
		
		List<PayoffBatchForm> formList = new ArrayList<PayoffBatchForm>();
		for(Object object : list) {
			String jsonStr = String.valueOf(object);
			PayoffBatchForm form = JSONArray.parseObject(jsonStr, PayoffBatchForm.class);
			formList.add(form);
		}
		
		return formList;
	}
	
	/**
	 * 生成代发工资转账明细和汇总
	 * @param totalForm 信息汇总
	 * @param user 登陆用户对象
	 * @param payoffList 代发工资明细
	 * @param mv 页面视图
	 * @param bankTransferList 银行卡转账明细
	 * @param kjtTransferList 永达互联网金融转账明细
	 * @param request HTTP请求对象
	 * @param env 交易环境信息
	 * @return 转账笔数
	 * @throws Exception 
	 */
	private int generateBatchPayoffInfo(PayoffBatchForm totalForm, EnterpriseMember user, 
			List<PayoffBatchForm> payoffList, ModelAndView mv, 
			List<BankTransfer> bankTransferList, List<Transfer> kjtTransferList, 
			HttpServletRequest request, TradeEnvironment env) throws Exception {
		int transferCount = 0;
		if (ObjectUtils.isListNotEmpty(payoffList)) {
			// 统计总金额
			Money totalAmount = new Money();
			Money totalServiceFee = new Money();
			int feeCount = 0;
			int bankTransferCount = 0;
			int kjtTransferCount = 0;
			int size = payoffList.size();
			
			// 检查是否有审核权限
			HttpSession session = request.getSession();
			
			//保存原始凭证号列表到session中
			for (int i=0; i<size; i++) {
				PayoffBatchForm payoffForm = payoffList.get(i);
				payoffForm.setOrderNo(i+1);
				if (PayoffType.PAYOFF_TO_BANK.getCode() == payoffForm.getPayoffType()) {
//					// 整理转账信息列表
//					BankTransfer transfer = new BankTransfer();
//				    TransferBankBatchForm bankTransferForm = payoffForm.getBankTransfer();
//					transfer.setOrderNo(i+1);
//					copyBankFormProperties(bankTransferForm, transfer);
//					bankTransferList.add(transfer);
				    
				    // 计数
					bankTransferCount++;
					
					// 合计金额
					TransferBankBatchForm bankTransferForm = payoffForm.getBankTransfer();
					String amount = bankTransferForm.getTransferAmount();
					totalAmount.addTo(new Money(StringUtils.isEmpty(amount)?"0":amount));
					
					// 计算代发工资的服务费
					String serviceFeeStr = getServiceFee(request, new Money(amount), TradeType.PAYOFF_TO_BANK.getBizProductCode());
					payoffForm.setServiceCharge(serviceFeeStr);
					Money serviceFee = new Money(serviceFeeStr);
					if (serviceFee.compareTo(new Money()) == 1) {
						totalServiceFee.addTo(serviceFee);
						feeCount++;
					}
					
//					// 获取交易凭证号
//					TradeRequestInfo tradeReqest = null;
//					try {
//						tradeReqest = defaultWithdrawService.applyTransfer(user, TradeType.PAYOFF_TO_BANK, env);
//						tradeReqest.setMemo(transfer.getRemark());
//						//保存原始凭证信息到session中
//						tradeMap.put(String.valueOf(transfer.getOrderNo()), tradeReqest);
//					} catch (BizException e) {
//						logger.error("获取转账交易凭证失败");
//					}
				} else {
//					// 整理转账信息列表
//					Transfer transfer = new Transfer();
//					TransferKjtBatchForm kjtForm = payoffForm.getKjtTransfer();
//					transfer.setOrderNo(i+1);
//					copyKjtFormProperties(kjtForm, transfer);
//					kjtTransferList.add(transfer);
				    
				    // 计数
					kjtTransferCount++;
					
					// 合计金额
					TransferKjtBatchForm kjtForm = payoffForm.getKjtTransfer();
					String amount = kjtForm.getTransferAmount();
					totalAmount.addTo(new Money(StringUtils.isEmpty(amount)?"0":amount));
					
					// 计算代发工资的服务费
					String serviceFeeStr = getServiceFee(request, new Money(amount), TradeType.PAYOFF_TO_KJT.getBizProductCode());
					payoffForm.setServiceCharge(serviceFeeStr);
					Money serviceFee = new Money(serviceFeeStr);
					if (serviceFee.compareTo(new Money()) == 1) {
						totalServiceFee.addTo(serviceFee);
						feeCount++;
					}
				}
				
				// 所有
				setJsonAttribute(session, "payoffList", payoffList);
			}
			
			// 银行转账、永达互联网金融转账笔数
			setJsonAttribute(session, "bankTransferCount", bankTransferCount);
			setJsonAttribute(session, "kjtTransferCount", kjtTransferCount);
			
			totalForm.setServiceCharge(totalServiceFee.toString());
			totalForm.setTransMoney(totalAmount.toString());
			totalForm.setTotalMoney(totalAmount.addTo(totalServiceFee).toString());
			
			totalForm.setFeeCount(feeCount);
		}
		
		transferCount = payoffList.size();
		mv.addObject("transferPerCount", transferCount);
		mv.addObject("form", totalForm);
		SessionPage<PayoffBatchForm> sessionPage = new SessionPage<PayoffBatchForm>();
		super.setSessionPage(payoffList, sessionPage);
		mv.addObject("sessionPage", sessionPage);
		
		return transferCount;
	}
	
//	/**
//	 * 银行转账信息属性拷贝
//	 * @param bankForm
//	 * @param bankTransfer
//	 */
//	private void copyBankFormProperties(TransferBankBatchForm bankForm, BankTransfer bankTransfer) {
//		bankTransfer.setName(bankForm.getAccountName());
//		bankTransfer.setMoney(bankForm.getTransferAmount());
//		bankTransfer.setBankName(bankForm.getBankName());
//		bankTransfer.setBranchName(bankForm.getBranchBankName());
//		bankTransfer.setAccountNoMask(bankForm.getAccountNo());
//		bankTransfer.setProvName(bankForm.getBankProvince());
//		bankTransfer.setCityName(bankForm.getBankCity());
//		bankTransfer.setRemark(bankForm.getRemark());
//		bankTransfer.setMobile(bankForm.getMobile());
//		bankTransfer.setCardAttribute(bankForm.getAccountType());
//	}
//	
//	/**
//	 * 永达互联网金融转账信息属性拷贝
//	 * @param kjtForm
//	 * @param kjtTransfer
//	 */
//	private void copyKjtFormProperties(TransferKjtBatchForm kjtForm, Transfer kjtTransfer) {
//		kjtTransfer.setContact(kjtForm.getKjtAccount());
//		kjtTransfer.setMoney(kjtForm.getTransferAmount());
//		kjtTransfer.setRemark(kjtForm.getRemark());
//		kjtTransfer.setName(kjtForm.getKjtAccountName());
//	}
	
	/**
	 * 解析sheet中的银行账户转账信息
	 * @param batchFormList 转账信息列表
	 * @param sheet excel中的sheet对象
	 * @throws Exception 
	 */
	private void parseOneBankSheet(HttpServletRequest request, List<PayoffBatchForm> payoffList, List<PayoffBatchForm> errorList, Sheet sheet)
			throws Exception {
		// 循环行Row
		int lastRowNum = sheet.getLastRowNum();
		for (int rowNum = 1; rowNum <= lastRowNum; rowNum++) {
			Row row = sheet.getRow(rowNum);
			if (row == null) {
				logger.debug(rowNum + "行为空");
				continue;
			}
			
			PayoffBatchForm payoffForm = new PayoffBatchForm();

			String orderNo = getValue(row.getCell(0));
			String accountName = getValue(row.getCell(1));
			String accountNo = getValue(row.getCell(2));
			String amount = getValue(row.getCell(7));
			
			if (StringUtils.isNotEmpty(orderNo)) {
				payoffForm.setOrderNo(Integer.parseInt(orderNo));
			}

			// 判断转账类型（转账到银行卡/永达互联网金融账户）
			String bankName = getValue(row.getCell(3));
			String bankProvince = getValue(row.getCell(4));
			String bankCity = getValue(row.getCell(5));
			String branchBankName = getValue(row.getCell(6));
			String companyOrPersonal = getValue(row.getCell(8));
			String remark = getValue(row.getCell(9));
			
			int accountType = -1;
			if (CompanyOrPersonal.COMPANY.getMessage().equals(companyOrPersonal)) {
			    accountType = CardAttr.COMPANY.getCode();
			} else if (CompanyOrPersonal.PERSONAL.getMessage().equals(companyOrPersonal)) {
			    accountType = CardAttr.PERSONAL.getCode();
			}
			
			if (StringUtils.isEmpty(bankName) || StringUtils.isEmpty(bankProvince)
					|| StringUtils.isEmpty(bankCity) || StringUtils.isEmpty(branchBankName)) {
				// 发到永达互联网金融
				TransferKjtBatchForm kjtForm = new TransferKjtBatchForm();
				kjtForm.setKjtAccount(accountNo);
				kjtForm.setTransferAmount(amount);
				kjtForm.setRemark(remark);
				kjtForm.setKjtAccountName(accountName);
				payoffForm.setPayoffType(PayoffType.PAYOFF_TO_KJT.getCode());
				payoffForm.setKjtTransfer(kjtForm);

				// 验证结果为false忽略
	            if (validateKjtForm(super.getUser(request), payoffForm, errorList)) {
	                payoffList.add(payoffForm);
	            }
				
				logger.debug("转账到永达互联网金融账号：{}", accountNo);
			} else {
				// 发到银行账户
				TransferBankBatchForm bankform = new TransferBankBatchForm();
				bankform.setAccountName(accountName);
				bankform.setTransferAmount(amount);
				bankform.setAccountNo(accountNo);
				bankform.setBankName(bankName);
				bankform.setBankProvince(bankProvince);
				bankform.setBankCity(bankCity);
				bankform.setBranchBankName(branchBankName);
				bankform.setAccountType(accountType);
				bankform.setRemark(remark);
				payoffForm.setPayoffType(PayoffType.PAYOFF_TO_BANK.getCode());
				payoffForm.setBankTransfer(bankform);
				
				// 验证结果为false忽略
                if (validateBankForm(super.getUser(request), payoffForm, errorList)) {
                    payoffList.add(payoffForm);
                }
				
				logger.debug("转账到银行账号：{}", accountNo);
			}
			
			// 上传条数限制
			if (payoffList.size() > MAX_IMPORT_COUNT) {
                throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, ErrorMsg.ERROR_PAYOFF_COUNT.getDesc());
            }
		}
	}
	
	/**
     * 验证解析上传文件的form对象
     * @param form form对象
     * @param rowNum 行号
     * @return true-校验成功，false-无效可忽略
     * @throws Exception 抛出异常信息提示用户
     */
    private boolean validateBankForm(EnterpriseMember user, PayoffBatchForm payoffForm, List<PayoffBatchForm> errorList) {
        if (payoffForm == null || payoffForm.getBankTransfer() == null) {
            return false;
        }
        
        TransferBankBatchForm form = payoffForm.getBankTransfer();
        
        // 如果所有行都是空，为无效行，可忽略
        if (StringUtils.isEmpty(form.getAccountName()) && StringUtils.isEmpty(form.getBankName())
                && StringUtils.isEmpty(form.getBankProvince()) && StringUtils.isEmpty(form.getBankCity())
                && StringUtils.isEmpty(form.getBranchBankName()) && StringUtils.isEmpty(form.getAccountNo()) 
                && StringUtils.isEmpty(form.getTransferAmount()) && (form.getAccountType() < 0)
                && StringUtils.isEmpty(form.getMobile()) && StringUtils.isEmpty(form.getRemark())) {
            return false;
        }
        
        if (StringUtils.isEmpty(form.getAccountName())) {
            payoffForm.setErrorInfo("[收款开户名称]不能为空");
            errorList.add(payoffForm);
            return false;
        }
        
        String amount = form.getTransferAmount();
        if (StringUtils.isEmpty(amount)) {
            payoffForm.setErrorInfo("[转账金额]不能为空");
            errorList.add(payoffForm);
            return false;
        }
        
        Money validMoney = null;
        try {
            validMoney = new Money(amount);
        } catch (Exception e) {
            payoffForm.setErrorInfo("[转账金额]格式不正确");
            errorList.add(payoffForm);
            return false;
        }
        if ((validMoney == null) || (validMoney.compareTo(new Money("0")) != 1)) {
            payoffForm.setErrorInfo("[转账金额]不能为负数");
            errorList.add(payoffForm);
            return false;
        }
        
        if (StringUtils.isEmpty(form.getBankName())) {
            payoffForm.setErrorInfo("[收款银行名称]不能为空");
            errorList.add(payoffForm);
            return false;
        }
        
        if (BankType.getByMsg(form.getBankName()) == null) {
            payoffForm.setErrorInfo("[收款银行名称]填写有误，系统中不存在该银行[" + form.getBankName() + "]");
            errorList.add(payoffForm);
            return false;
        }
        
        if (StringUtils.isEmpty(form.getBankProvince())) {
            payoffForm.setErrorInfo("[收款银行所在省份]不能为空");
            errorList.add(payoffForm);
            return false;
        }
        
        if (StringUtils.isEmpty(form.getBankCity())) {
            payoffForm.setErrorInfo("[收款银行所在市]不能为空");
            errorList.add(payoffForm);
            return false;
        }
        
        if (StringUtils.isEmpty(form.getBranchBankName())) {
            payoffForm.setErrorInfo("[收款支行名称]不能为空");
            errorList.add(payoffForm);
            return false;
        }
        
        if (StringUtils.isEmpty(form.getAccountNo())) {
            payoffForm.setErrorInfo("[收款银行帐号]不能为空");
            errorList.add(payoffForm);
            return false;
        }
        
        if (form.getAccountType() < 0) {
            payoffForm.setErrorInfo("[对公对私]填写有误，请填写“对公”或“对私”");
            errorList.add(payoffForm);
            return false;
        }
        
        if (StringUtils.isEmpty(form.getRemark())) {
            payoffForm.setErrorInfo("备注不能为空");
            errorList.add(payoffForm);
            return false;
        }
        
        if (!CommonUtils.validateRegex(RegexRule.AMOUNT_2_DECINALS, amount)) {
            logger.error("[转账金额]格式不正确，小数点前最多19位，小数点后最多2位！");
            payoffForm.setErrorInfo("[转账金额]格式不正确，小数点前最多19位，小数点后最多2位");
            errorList.add(payoffForm);
            return false;
        }
        
        if (!CommonUtils.validateRegex(RegexRule.BANKCARD_NO, form.getAccountNo())) {
            logger.error("银行卡号长度必须是8~32位的数字！");
            payoffForm.setErrorInfo("[银行卡号]长度必须是8~32位的数字");
            errorList.add(payoffForm);
            return false;
        }
        
        if (!CommonUtils.validateRegex(RegexRule.MOBLIE, form.getMobile())) {
            logger.error("手机号码格式不正确！");
            payoffForm.setErrorInfo("[手机号码]格式不正确");
            errorList.add(payoffForm);
            return false;
        }
        
        if (!CommonUtils.validateRegex(RegexRule.ACCOUNT_NAME, form.getAccountName())) {
            logger.error("银行开户名不能超过20个字！");
            payoffForm.setErrorInfo("[银行开户名]不能超过20个字");
            errorList.add(payoffForm);
            return false;
        }
        
        return true;
    }
	
	/**
     * 验证解析上传文件的form对象
     * @param form form对象
     * @param rowNum 行号
     * @return true-校验成功，false-无效可忽略
     * @throws Exception 抛出异常信息提示用户
     */
    private boolean validateKjtForm(EnterpriseMember user, PayoffBatchForm payoffForm, List<PayoffBatchForm> errorList) {
        if (payoffForm == null || payoffForm.getKjtTransfer() == null) {
            return false;
        }
        
        TransferKjtBatchForm form = payoffForm.getKjtTransfer();
        
        String kjtAccount = form.getKjtAccount();
        String kjtAccountName = form.getKjtAccountName();
        String amount = form.getTransferAmount();
        String mobile = form.getMobile();
        String remark = form.getRemark();
        // 如果所有行都是空，为无效行，可忽略
        if (StringUtils.isEmpty(kjtAccount) && StringUtils.isEmpty(amount)
                && StringUtils.isEmpty(mobile) && StringUtils.isEmpty(remark)) {
            return false;
        }
        
        if (StringUtils.isEmpty(kjtAccount)) {
            payoffForm.setErrorInfo("[转账账户]不能为空");
            errorList.add(payoffForm);
            return false;
        }
        
        if ((user != null) && kjtAccount.equalsIgnoreCase(user.getLoginName())) {
            payoffForm.setErrorInfo("[转账账户]不能和当前登陆账户一致");
            errorList.add(payoffForm);
            return false;
        }
        
        if (StringUtils.isEmpty(kjtAccountName)) {
            payoffForm.setErrorInfo("[转账账户名称]不能为空");
            errorList.add(payoffForm);
            return false;
        }
        
        if (StringUtils.isEmpty(amount)) {
            payoffForm.setErrorInfo("[转账金额]不能为空");
            errorList.add(payoffForm);
            return false;
        }
        
        if (StringUtils.isEmpty(remark)) {
            payoffForm.setErrorInfo("备注不能为空");
            errorList.add(payoffForm);
            return false;
        }
        
        Money validMoney = null;
        try {
            validMoney = new Money(amount);
        } catch (Exception e) {
            logger.error("导入金额格式错误，{}", e);
            payoffForm.setErrorInfo("[转账金额]格式不正确");
            errorList.add(payoffForm);
            return false;
        }
        
        if ((validMoney == null) || (validMoney.compareTo(new Money("0")) != 1)) {
            payoffForm.setErrorInfo("[转账金额]必须大于0");
            errorList.add(payoffForm);
            return false;
        }
        
        if (!CommonUtils.validateRegex(RegexRule.AMOUNT_2_DECINALS, amount)) {
            payoffForm.setErrorInfo("[转账金额]需保留2位小数");
            errorList.add(payoffForm);
            return false;
        }
        
        if (!CommonUtils.validateRegex(RegexRule.EMAIL, form.getKjtAccount()) 
                && !CommonUtils.validateRegex(RegexRule.MOBLIE, form.getKjtAccount())) {
            logger.error("永达互联网金融账户格式不正确！");
            payoffForm.setErrorInfo("[永达互联网金融账户]格式不正确");
            errorList.add(payoffForm);
            return false;
        }
        
        if (!CommonUtils.validateRegex(RegexRule.MOBLIE, form.getMobile())) {
            logger.error("手机号码格式不正确！");
            payoffForm.setErrorInfo("[手机号码]格式不正确");
            errorList.add(payoffForm);
            return false;
        }
        
        //查询收款人
        BaseMember payeeUser = null;
        try {
            payeeUser = defaultMemberService.isMemberExists(kjtAccount, SYSTEM_CODE, env);
            if (payeeUser == null) {
                payoffForm.setErrorInfo("系统中不存在收款账号[" + kjtAccount + "]");
                errorList.add(payoffForm);
                return false;
            }
            
            if (!kjtAccountName.equals(payeeUser.getMemberName())) {
                payoffForm.setErrorInfo("账户名称[" + kjtAccountName + "]与账户[" + kjtAccount + "]不匹配");
                errorList.add(payoffForm);
                return false;
            }
        } catch (Exception e) {
            logger.error("查询收款人异常", e);
            payoffForm.setErrorInfo("系统中不存在收款账户[" + kjtAccount + "]");
            errorList.add(payoffForm);
            return false;
        }
        
        return true;
    }
	
	/**
	 * 得到Excel表中的值
	 * @param cell Excel中的单元格
	 * @return Excel中单元格的值
	 */
	@SuppressWarnings("static-access")
	private String getValue(Cell cell) {
		if (cell == null) {
			return StringUtils.EMPTY;
		}
		if (cell.getCellType() == cell.CELL_TYPE_BOOLEAN) {
			// 返回布尔类型的值
			return String.valueOf(cell.getBooleanCellValue());
		} else if (cell.getCellType() == cell.CELL_TYPE_NUMERIC) {
		    // 返回数值类型的值
            BigDecimal decimal = new BigDecimal(String.valueOf(cell.getNumericCellValue()));
            return decimal == null ? StringUtils.EMPTY : decimal.toPlainString();
		} else {
			// 返回字符串类型的值
			return String.valueOf(cell.getStringCellValue());
		}
	}
	
	/**
	 * 计算服务费
	 * @param request HTTP请求对象
	 * @param money 转账金额
	 * @return 服务费用
	 */
	private String getServiceFee(HttpServletRequest request, Money money, String productCode) {
		// 先获取用户信息
		String memberId = this.getMemberId(request);
		
		PayPricingReq req = new PayPricingReq();
		req.setAccessChannel("WEB");
		req.setSourceCode(SYSTEM_CODE);
		req.setRequestNo(String.valueOf(System.currentTimeMillis()));
		req.setProductCode(productCode);
		req.setPaymentCode(PaymentType.PAY_FUND.getCode());
		req.setPayChannel(PayChannel.FUNDOUT.getCode());
		req.setPaymentInitiate(new Date());
		req.setPayableAmount(money);
		req.setOrderAmount(money);
		
		// 付款方
		List<com.netfinworks.pbs.service.context.vo.PartyInfo> partyList 
			= new ArrayList<com.netfinworks.pbs.service.context.vo.PartyInfo>();
		com.netfinworks.pbs.service.context.vo.PartyInfo party 
			= new com.netfinworks.pbs.service.context.vo.PartyInfo();
		party.setMemberId(memberId);
		party.setPartyRole(PartyRole.PAYER);
		partyList.add(party);
		req.setPartyInfoList(partyList);
		
		// 调用服务费计算接口
		PaymentPricingResponse resp = payPartyFeeFacade.getFee(req);
		if ((resp != null) && resp.isSuccess()) {
			List<PartyFeeInfo> pfiList = resp.getPartyFeeList();
			if (ObjectUtils.isListNotEmpty(pfiList)) {
				for (PartyFeeInfo pfi : pfiList) {
					if (memberId.equals(pfi.getMemberId())) {
						return pfi.getFee().toString();
					}
				}
			}
			
		}
		
		return StringUtils.EMPTY;
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
