/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年7月8日
 */
package com.netfinworks.site.web.action.money;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.meidusa.fastjson.JSONArray;
import com.meidusa.fastjson.JSONObject;
import com.netfinworks.batchservice.facade.enums.ProductType;
import com.netfinworks.batchservice.facade.model.BatchDetail;
import com.netfinworks.batchservice.facade.model.BatchPayDetail;
import com.netfinworks.batchservice.facade.model.MemberInfo;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.mns.client.domain.PageInfo;
import com.netfinworks.payment.common.v2.enums.PartyRole;
import com.netfinworks.pbs.service.facade.PayPartyFeeFacade;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.constants.RegexRule;
import com.netfinworks.site.core.common.util.MoneyUtil;
import com.netfinworks.site.core.common.util.RadomUtil;
import com.netfinworks.site.domain.domain.audit.Audit;
import com.netfinworks.site.domain.domain.info.PartyInfo;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.enums.OperateTypeEnum;
import com.netfinworks.site.domain.enums.PlatformTypeEnum;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.account.DefaultAccountService;
import com.netfinworks.site.domainservice.audit.AuditServiceImpl;
import com.netfinworks.site.domainservice.payment.FundsControlService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.domainservice.trade.impl.DefaultTransferServiceImpl;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.voucher.VoucherService;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.action.form.KjtTransferForm;
import com.netfinworks.site.web.action.form.TransferKjtBatchForm;
import com.netfinworks.site.web.common.constant.AuditStatus;
import com.netfinworks.site.web.common.constant.AuditSubType;
import com.netfinworks.site.web.common.constant.AuditType;
import com.netfinworks.site.web.common.constant.ErrorMsg;
import com.netfinworks.site.web.common.constant.FunctionType;
import com.netfinworks.site.web.common.constant.MemoType;
import com.netfinworks.site.web.common.util.ObjectUtils;
import com.netfinworks.site.web.common.vo.SessionPage;
import com.netfinworks.site.web.common.vo.Transfer;
import com.netfinworks.site.web.util.CommonUtils;
import com.netfinworks.site.web.util.LogUtil;
import com.netfinworks.site.web.util.TradeReqestUtil;
import com.netfinworks.tradeservice.facade.response.PaymentResponse;
import com.netfinworks.ufs.client.UFSClient;
import com.netfinworks.voucher.service.facade.domain.enums.VoucherInfoType;

/**
 * 永达互联网金融账户转账
 * @author xuwei
 * @date 2014年7月8日
 */
@Controller
@RequestMapping("transfer")
public class KjtTransferAction extends BaseAction {
    @Resource(name = "defaultAccountService")
    private DefaultAccountService      defaultAccountService;

    @Resource(name = "auditService")
    private AuditServiceImpl           auditService;

    @Resource(name = "defaultMemberService")
    private DefaultMemberService       defaultMemberService;

    @Resource(name = "webResource")
    private WebDynamicResource         webResource;

    @Resource(name = "payPartyFeeFacade")
    private PayPartyFeeFacade          payPartyFeeFacade;

    @Resource(name = "defaultTransferService")
    private DefaultTransferServiceImpl defaultTransferService;

    @Resource(name = "ufsClient")
    private UFSClient                  ufsClient;

    @Resource(name = "fundsControlService")
    private FundsControlService        fundsControlService;

    @Resource(name = "voucherService")
    private VoucherService             voucherService;
    
    @Resource(name = "memberService")
    private MemberService commMemberService;
	/**
	 * 永达互联网金融转账
	 * @return
	 */
	@RequestMapping("/toTransferKjt.htm")
	public ModelAndView toKjtTransfer(HttpServletRequest request,
			HttpServletResponse resp, TradeEnvironment env) {
		ModelAndView mv = new ModelAndView();
		 RestResponse restP = new RestResponse();
		 restP.setData(initOcx());
		 mv.addObject("response", restP);
		
		// 获取用户信息
		EnterpriseMember user = getUser(request);
		
		// 实名认证
		if (!AuthResultStatus.PASS.getCode().equals(user.getNameVerifyStatus().getCode())) {
			logger.error("登陆账户未进行实名认证");
			mv.addObject("message", "对不起，您的账户尚未实名认证，无法转账!");
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		
		// 查询可用余额
		try {
			// 根据用户ID查询会员账户
			MemberAccount account = defaultAccountService.queryAccountById(user.getDefaultAccountId(), env);
			mv.addObject("account", account);
			
			String errorMsg = request.getParameter("errorMsg");
			if (StringUtils.isNotEmpty(errorMsg)) {
				mv.addObject("errorMsg", errorMsg);
			}
			
			mv.addObject("user", user);
		} catch (Exception e) {
			logger.error("查询可用余额、冻结资金失败", e);
		}
		
		mv.setViewName(CommonConstant.URL_PREFIX + "/kjtTransfer/transfer-kjt");
		return mv;
	}
	
	/**
	 * 验证限额限次
	 * @param request
	 * @param cashingForm
	 * @return
	 * @throws BizException 
	 */
	@ResponseBody
	@RequestMapping("/validateLflt")
	public RestResponse validateLflt(HttpServletRequest request, KjtTransferForm form) throws BizException {
		RestResponse response = new RestResponse();
		
		// 获取用户信息
		EnterpriseMember user = getUser(request);
		
		// 单笔最大金额
		Money maxMoney = MoneyUtil.getMaxMoney(form.getMoneyList());
		String payee = "";
        List<String> emailList = form.getEmailList();
        for(String email:emailList){
        	try{
        		payee = commMemberService.queryMemberIntegratedInfo(email, env);
        	}catch(BizException e){
        		if(e.getCode() == ErrorCode.MEMBER_NOT_EXIST){
        			response.setMessage("账户"+email+"不存在");
        			return response;
        		}
        	}
            // 验证限额限次
            if (!super.validateLflt(user.getMemberId(), payee,maxMoney, TradeType.TRANSFER, null, response, env)) {
                logger.error("限额限次验证失败");
                return response;
            }
        }
		
		response.setSuccess(true);
		return response;
	}
	
	/**
	 * 永达互联网金融转账确认
	 * @param form
	 * @param request
	 * @param session
	 * @param resp
	 * @param env
	 * @return
	 */
	@RequestMapping("/toConfirmTransferKjt.htm")
	public ModelAndView toConfirmTransferKjt(KjtTransferForm form, HttpServletRequest request,
			HttpSession session, HttpServletResponse resp, TradeEnvironment env) {
		ModelAndView mv = new ModelAndView();
		RestResponse restP = new RestResponse();
		restP.setData(initOcx());
		mv.addObject("response", restP);
		EnterpriseMember user = this.getUser(request);
		// 软硬证书是否激活
        if (this.isCertActive(request, "hard", env) || this.isCertActive(request, "soft", env)) {
            mv.addObject("isCertActive", "yes");
        } else {
            mv.addObject("isCertActive", "no");
            if (StringUtils.isEmpty(user.getMobile())) {
                mv.addObject("message", "请先绑定数字证书或手机！");
                mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
                return mv;
            }
        }
        // 校验提交参数
        String errorMsg = validator.validate(form);
        if (StringUtils.isNotEmpty(errorMsg)) {
            mv.addObject("message", errorMsg);
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
		
		if (StringUtils.isEmpty(form.getTotalMoney())) {
			logger.error("转账金额输入有误");
			mv.addObject("message", "转账金额输入有误！");
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		
		try {
			// 检查余额是否足够
			Money avaBalance = account.getAvailableBalance();
			Money totalMoney = new Money(form.getTotalMoney());
			if (avaBalance.compareTo(totalMoney) == -1) {
				logger.error("账户[{}]余额不足", account.getAccountId());
				mv.addObject("message", "您的转账金额已超限，请重新输入！");
				mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
				return mv;
			}
			
			mv.addObject("avaBalance", avaBalance);
		} catch (Exception e) {
			logger.error("转账金额输入有误");
			mv.addObject("message", "转账金额输入有误！");
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		
		try {
			// 生成转账信息
			List<Transfer> transferList = new ArrayList<Transfer>();
			int transferCount = generateTransferInfo(user, form, mv, transferList);
			
			// 验证限额限次
	        Money maxMoney = new Money(form.getMaxAmount());
	        
	        String payee = "";
	        List<String> emailList = form.getEmailList();
	        for(String email:emailList){
	            payee = commMemberService.queryMemberIntegratedInfo(email, env);
	            // 验证限额限次
	            if (!super.validateLflt(user.getMemberId(), payee,maxMoney, TradeType.TRANSFER, mv, null, env)) {
	                mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
	                return mv;
	            }
	        }
	        
	        
	        long token = RadomUtil.createRadom();
			
			// 把相关信息放到Session中
			this.setJsonAttribute(session, "kjtTransferForm" + token, form);
			this.setJsonAttribute(session, "transferList" + token, transferList);
			this.setJsonAttribute(session, "transferCount" + token, transferCount);
			mv.addObject("token", token);
		} catch (Exception e) {
			logger.error("生成转账信息失败", e);
			mv.addObject("message", "您的输入的转账信息不正确！" + e.getMessage());
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		
		if (auth(request, FunctionType.EW_MY_APPROVE.getCode())) {
			// 有审核权限
			mv.setViewName(CommonConstant.URL_PREFIX + "/kjtTransfer/transfer-confirm");
		} else {
			// 无审核权限
			mv.setViewName(CommonConstant.URL_PREFIX + "/kjtTransfer/transfer-apply-confirm");
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
	 * 生成转账信息
	 * @param form 表单对象
	 * @param mv 返回视图对象
	 * @param transferList 转账信息
	 * @return 生成转账记录条数
	 */
	private int generateTransferInfo(EnterpriseMember user, KjtTransferForm form, ModelAndView mv, List<Transfer> transferList) throws Exception {
		List<String> emailList = form.getEmailList();
		List<String> moneyList = form.getMoneyList();
		int transferCount = 0;
		if (ObjectUtils.isListNotEmpty(emailList) && ObjectUtils.isListNotEmpty(moneyList)) {
			int size = emailList.size();
			int orderNo = 1;
			for (int i=0; i<size; i++) {
				String email = emailList.get(i);
				if (StringUtils.isEmpty(email)) {
					continue;
				}
				
				// 不能给自己转账
				if ((user != null) && email.equalsIgnoreCase(user.getLoginName())) {
		            throw new Exception("转账账户[" + email + "]不能和当前登陆账户一致");
		        }
				
				Transfer transfer = new Transfer();
				transfer.setOrderNo(orderNo);
				transfer.setContact(email);
				try {
				    //查询收款人
					transfer.setName(getTargetAccountName(null, email));
				} catch (Exception e) {
					logger.error("查询收款人失败", e);
					throw new Exception("系统中不存在收款账户[" + email + "]");
				}
				transfer.setMoney(moneyList.get(i));
				
				// 更新单笔最大金额
				setMaxAmount(transfer.getMoney(), form);
				
				if (StringUtils.isEmpty(transfer.getRemark())) {
					if (MemoType.TYPE_OTHERS.getCode() != form.getRemarkType()) {
						form.setRemark(MemoType.getDesc(form.getRemarkType()));
						transfer.setRemark(MemoType.getDesc(form.getRemarkType()));
					} else {
						transfer.setRemark(form.getRemark());
					}
				}
				transferList.add(transfer);
				orderNo++;
			}
		}
		
		transferCount = transferList.size();
		mv.addObject("transferPerCount", transferCount);
		mv.addObject("transferList", transferList);
		mv.addObject("form", form);
		if (MemoType.TYPE_OTHERS.getCode() != form.getRemarkType()) {
			// 选择其他时，获取用户输入的备注
			form.setRemark(MemoType.getDesc(form.getRemarkType()));
		}
		
		return transferCount;
	}
	
	/**
     * 更新最大金额
     * @param curAmount 当前交易金额
     * @param totalForm 批量转账统计对象
     */
    private void setMaxAmount(String curAmount, KjtTransferForm totalForm) {
        if (StringUtils.isEmpty(curAmount)) {
            return;
        }
        
        if (StringUtils.isEmpty(totalForm.getMaxAmount())) {
            totalForm.setMaxAmount(curAmount);
            return;
        }
        
        Money curMoney = new Money(curAmount);
        if (curMoney.compareTo(new Money(totalForm.getMaxAmount())) > 0) {
            totalForm.setMaxAmount(curAmount);
        }
    }
	
	/**
	 * 确认永达互联网金融转账申请
	 * @param request
	 * @param env
	 * @return
	 */
	@RequestMapping("/confirmTransferKjtApply.htm")
	public ModelAndView confirmTransferKjtApply(HttpServletRequest request, HttpSession session, TradeEnvironment env) {
		ModelAndView mv = new ModelAndView();
		RestResponse restP = new RestResponse();
		restP.setData(initOcx());
		mv.addObject("response", restP);
		// 获得用户信息
		EnterpriseMember user = this.getUser(request);
		
		// 检查支付密码
		if(!validatePayPassword(request, user, null, mv)) {
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
 		
		String token = request.getParameter("token");
 		// 从会话中获取转账信息
 		KjtTransferForm form = this.getJsonAttribute(session, "kjtTransferForm" + token, KjtTransferForm.class);
		List<Transfer> transferList = getTransferList(this.getJsonAttribute(session, "transferList" + token, List.class));
		Integer transferCount = this.getJsonAttribute(session, "transferCount" + token, Integer.class);
		String sourceBatchNo=this.getJsonAttribute(session, "sourceBatchNo" + token, String.class);
 		// 提交转账审核申请
		String batchTransfer = request.getParameter("batchTransfer");
 		try {
			if ("1".equals(batchTransfer)) {
			    this.submitBatchTransferApply(request, transferList, form, user, transferCount, "", env,sourceBatchNo);
			} else {
			    if (StringUtils.isEmpty(form.getTotalMoney())) {
	                throw new Exception("转账总金额不能为空");
	            }
	            Money freezeAmount = new Money(form.getTotalMoney());
	            
	            // 冻结金额
	            String bizPaymentSeqNo = fundsControlService.freeze(user.getMemberId(), user.getDefaultAccountId(), freezeAmount, env);
	            if (StringUtils.isEmpty(bizPaymentSeqNo)) {
	                logger.error("资金冻结失败");
	                throw new Exception("您的账户余额不足！");
	            }
			    
			    if (!this.submitTransferAudit(request, transferList, form, user, transferCount, bizPaymentSeqNo, env)) {
	                throw new Exception("转账审核申请操作失败！");
	            }
            }
		} catch (Exception e) {
		    removeSessionToken(request, token);
			logger.error("无法提交转账申请", e);
			mv.addObject("message", e.getMessage());
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
 		
 		form.setMobile(user.getMobile());
 		form.setTransferCount(transferCount);
 		mv.addObject("form", form);
 		mv.addObject("transferList", transferList);
 		
		if ("1".equals(batchTransfer)) {
			mv.setViewName(CommonConstant.URL_PREFIX + "/kjtTransfer/transfer-batch-apply-confirm-success");
		} else {
			mv.setViewName(CommonConstant.URL_PREFIX + "/kjtTransfer/transfer-apply-confirm-success");
		}
		removeSessionToken(request, token);
		
		return mv;
	}
	
	/**
	 * 提交所有的审核申请（目前只能单个提交）
	 * @param transferList
	 * @param form
	 * @param user
	 * @param transferCount
	 * @param env
	 * @return
	 */
	private boolean submitTransferAudit(HttpServletRequest request, List<Transfer> transferList, KjtTransferForm form, 
			EnterpriseMember user, int transferCount, String freezeVourceNo, TradeEnvironment env) throws Exception {
		if (ObjectUtils.isListEmpty(transferList)) {
			logger.debug("转账的目标账户为空");
			return false;
		}
		
		MemberAccount account = null;
		try {
			account = defaultAccountService.queryAccountById(user.getDefaultAccountId(), env);
		} catch (ServiceException e1) {
			logger.error("查询账户失败", e1);
			return false;
		}
		
		int size = transferList.size();
		for (int i=0; i<size; i++) {
			Transfer transfer = transferList.get(i);
			//查询收款人
	        BaseMember payeeUser = null;
			try {
				payeeUser = defaultMemberService.isMemberExists(transfer.getContact(), SYSTEM_CODE, env);
				transfer.setName(getTargetAccountName(payeeUser, transfer.getContact()));
			} catch (Exception e) {
				logger.error("查询收款人失败", e);
				return false;
			}
			
	        //生成收款方
            logger.info("转账,生成收款方");
            PartyInfo payee = new PartyInfo();
            payee.setAccountId(payeeUser.getDefaultAccountId());
            payee.setMemberId(payeeUser.getMemberId());
            payee.setOperatorId(payeeUser.getCurrentOperatorId());
            payee.setIdentityNo(transfer.getContact());
            payee.setMemberName(payeeUser.getMemberName());
            payee.setMemberType(payeeUser.getMemberType());

            //生成付款方
            PartyInfo payer = TradeReqestUtil.createPay(user.getDefaultAccountId(),
                user.getMemberId(), user.getCurrentOperatorId(), user.getMobile(),
                user.getMemberName(),user.getMemberType());
            payer.setAccountName(user.getLoginName());
            payer.setEnterpriseName(user.getEnterpriseName());
            logger.info("转账,生成付款方");

            //生成交易请求
            TradeRequestInfo tradeReqest = TradeReqestUtil.createTransferTradeRequest(payer, payee,
        		new Money(transfer.getMoney()), form.getRemark(), new Money(), 
        		form.getSendNoteMsg() == 0 ? FALSE_STRING : TRUE_STRING);
            env.setSuccessDispalyUrl(webResource.getWalletAddress() + "/my/home.htm");
            logger.info("转账,生成交易请求");
            
            //封装客户端信息,获取凭证号
            try {
				BeanUtils.copyProperties(tradeReqest, env);
				defaultTransferService.getTransferVoucherNo(tradeReqest);
			} catch (Exception e) {
				logger.error("获取交易凭证失败", e);
				throw new Exception("获取交易凭证失败！");
			}
            
            try {
            	// 保存审核信息，状态为“待审核”
                this.saveAuditInfo(request, tradeReqest, transfer, account.getAvailableBalance().toString(), user, 
                		AuditStatus.AUDIT_WAITING.getCode(), freezeVourceNo);
			} catch (Exception e) {	 
				logger.error("保存审核信息失败", e);
				throw new Exception("您提交的转账信息不正确！");
			}
		}
		
		return true;
	}
	
	/**
	 * 确认永达互联网金融转账
	 * @param request
	 * @param env
	 * @return
	 */
	@RequestMapping("/confirmTransferKjt")
	public ModelAndView confirmTransferKjt(HttpServletRequest request, HttpSession session, TradeEnvironment env) {
		ModelAndView mv = new ModelAndView();
		RestResponse restP = new RestResponse();
		restP.setData(initOcx());
		mv.addObject("response", restP);
		// 获得用户信息
		EnterpriseMember user = this.getUser(request);
        String batchTransfer = request.getParameter("batchTransfer");
		
        // 记录申请日志
        logger.info(LogUtil.generateMsg(
                StringUtils.isEmpty(batchTransfer) ? OperateTypeEnum.TRANSFER_APPLY
                        : OperateTypeEnum.TRANSFER_APPLY_FILE, user, env, StringUtils.EMPTY));
		
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
		if(!super.validatePayPassword(request, user, null, mv)) {
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		
		String token = request.getParameter("token");
 		// 从会话中获取转账信息
 		KjtTransferForm form = this.getJsonAttribute(session, "kjtTransferForm" + token, KjtTransferForm.class);
		List<Transfer> transferList = getTransferList(this.getJsonAttribute(session, "transferList" + token, List.class));
		Integer transferCount = this.getJsonAttribute(session, "transferCount" + token, Integer.class);
        String sourceBatchNo=this.getJsonAttribute(session, "sourceBatchNo" + token, String.class);
		try {
		    if ("1".equals(batchTransfer)) {
		        this.submitBatchTransfer(request, transferList, form, user, transferCount,sourceBatchNo, env);
		    } else {
		        this.submitTransfer(request, transferList, form, user, transferCount, env);
            }
		} catch (Exception e) {
			logger.error("转账失败", e);
			mv.addObject("success", false);
			mv.addObject("message", e.getMessage());
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			removeSessionToken(request, token);
			return mv;
		}
 		
 		form.setTransferCount(transferCount);
 		mv.addObject("form", form);
 		mv.addObject("transferList", transferList);
 		
		if ("1".equals(batchTransfer)) {
			mv.setViewName(CommonConstant.URL_PREFIX + "/kjtTransfer/transfer-batch-confirm-success");
		} else {
			mv.setViewName(CommonConstant.URL_PREFIX + "/kjtTransfer/transfer-confirm-success");
		}
		removeSessionToken(request, token);
		
		return mv;
	}
	
	/**
     * 批量提交所有的转账
     * @param transferList
     * @param form
     * @param user
     * @param transferCount
     * @param sourceBatchNo 外部订单号
     * @param env
     * @return
     */
    private void submitBatchTransfer(HttpServletRequest request, List<Transfer> transferList, KjtTransferForm form, 
            EnterpriseMember user, int transferCount,String sourceBatchNo, TradeEnvironment env) throws Exception {
        if (ObjectUtils.isListEmpty(transferList)) {
            logger.debug("转账的目标账户不存在");
            throw new Exception("转账的目标账户不存在");
        }
        
        List<BatchDetail> batchDetailList = new ArrayList<BatchDetail>();
        int size = transferList.size();
		Money totalFee = new Money("0");
        for (int i=0; i<size; i++) {
            Transfer transfer = transferList.get(i);
            //查询收款人
            BaseMember payeeUser = null;
            try {
                payeeUser = defaultMemberService.isMemberExists(transfer.getContact(), SYSTEM_CODE, env);
                if (payeeUser == null) {
                    throw new Exception();
                }
            } catch (Exception e) {
                logger.error("收款人不存在", e);
                throw new Exception("收款人不存在");
            }
            
            // 生成批量支付详细信息
            BatchDetail batchDetail = this.generatePayDetail(transfer, request);
            batchDetailList.add(batchDetail);
            
			if (StringUtils.isNotEmpty(transfer.getMoney())) {
				String feeStr = super.getServiceFee(request, transfer.getMoney(),
						TradeType.TRANSFER.getBizProductCode(), null);
				if (StringUtils.isNotEmpty(feeStr)) {
					totalFee.addTo(new Money(feeStr));
				}
			}

        }
        
        // 生成批次号
        String batchVourchNo = null;
        try {
            batchVourchNo = voucherService.regist(VoucherInfoType.CONTROL.getCode());
            voucherService.record(batchVourchNo, VoucherInfoType.CONTROL.getCode(),
                    TradeType.TRANSFER.getBizProductCode(), user.getMemberId(), env);
        }
        catch (Exception e) {
            logger.error("获取凭证号失败！", e);
        }
        
        try {
            defaultTransferService.batchTransferSubmit(batchVourchNo,sourceBatchNo, ProductType.PAY, user, batchDetailList, form.getTotalTransMoney());
        } catch (Exception e) {
            logger.error("批量转账失败", e);
            throw new Exception(e.getMessage());
        }
        
        try {
            logger.info(LogUtil.generateMsg(OperateTypeEnum.TRANSFER_EXAMINE, user, env, "通过"));
            // 保存审核信息，状态为“审核通过”
			this.saveBatchAuditInfo(request, batchVourchNo, form, user, AuditStatus.AUDIT_PASSED.getCode(),
					StringUtils.EMPTY, totalFee.toString(),sourceBatchNo);
        } catch (Exception e) {
            logger.error("保存审核信息失败", e);
            throw new Exception("保存审核信息失败");
        }
        
    }
    
    /**
     * 批量提交所有的转账申请
     * @param transferList
     * @param form
     * @param user
     * @param transferCount
     * @param env
     * @return
     */
    private void submitBatchTransferApply(HttpServletRequest request, List<Transfer> transferList, KjtTransferForm form, 
            EnterpriseMember user, int transferCount, String freezeVourceNo, TradeEnvironment env,String sourceBatchNo) throws Exception {
        if (ObjectUtils.isListEmpty(transferList)) {
            logger.debug("转账的目标账户不存在");
            throw new Exception("转账的目标账户不存在");
        }
        
        List<BatchDetail> batchDetailList = new ArrayList<BatchDetail>();
        int size = transferList.size();
		Money totalFee = new Money("0");
        for (int i=0; i<size; i++) {
            Transfer transfer = transferList.get(i);
            //查询收款人
            BaseMember payeeUser = null;
            try {
                payeeUser = defaultMemberService.isMemberExists(transfer.getContact(), SYSTEM_CODE, env);
                if (payeeUser == null) {
                    throw new Exception();
                }
            } catch (Exception e) {
                logger.error("收款人不存在", e);
                throw new Exception("收款人不存在");
            }
            
            // 生成批量支付详细信息
            BatchDetail batchDetail = this.generatePayDetail(transfer, request);
            batchDetailList.add(batchDetail);

			if (StringUtils.isNotEmpty(transfer.getMoney())) {
				String feeStr = super.getServiceFee(request, transfer.getMoney(),
						TradeType.TRANSFER.getBizProductCode(), null);
				if (StringUtils.isNotEmpty(feeStr)) {
					totalFee.addTo(new Money(feeStr));
				}
			}
        }
        
        // 生成批次号
        String batchVourchNo = null;
        try {
            batchVourchNo = voucherService.regist(VoucherInfoType.CONTROL.getCode());
            voucherService.record(batchVourchNo, VoucherInfoType.CONTROL.getCode(),
                    TradeType.TRANSFER.getBizProductCode(), user.getMemberId(), env);
        }
        catch (Exception e) {
            logger.error("获取凭证号失败！", e);
        }
        
        try {
            defaultTransferService.batchTransferApply(batchVourchNo,sourceBatchNo, ProductType.PAY, user, batchDetailList, form.getTotalTransMoney());
        } catch (Exception e) {
            logger.error("批量转账失败", e);
            throw new Exception(e.getMessage());
        }
        try {
            // 保存审核信息，状态为“待审核”
			this.saveBatchAuditInfo(request, batchVourchNo, form, user, AuditStatus.AUDIT_WAITING.getCode(),
					freezeVourceNo, totalFee.toString(),sourceBatchNo);
        } catch (Exception e) {
            logger.error("保存审核信息失败", e);
            throw new Exception("您没有转账审核权限");
        }
        
    }
	
	/**
	 * 提交所有的转账
	 * @param transferList
	 * @param form
	 * @param user
	 * @param transferCount
	 * @param env
	 * @return
	 */
	private void submitTransfer(HttpServletRequest request, List<Transfer> transferList, KjtTransferForm form, 
			EnterpriseMember user, int transferCount, TradeEnvironment env) throws Exception {
		if (ObjectUtils.isListEmpty(transferList)) {
			logger.debug("转账的目标账户不存在");
			throw new Exception("转账的目标账户不存在");
		}
		
		MemberAccount account = null;
		try {
			account = defaultAccountService.queryAccountById(user.getDefaultAccountId(), env);
		} catch (ServiceException e) {
			logger.error("您的账户无效", e);
			throw new Exception("您的账户无效");
		}
		
		int size = transferList.size();
		for (int i=0; i<size; i++) {
			Transfer transfer = transferList.get(i);
			//查询收款人
	        BaseMember payeeUser = null;
			try {
				payeeUser = defaultMemberService.isMemberExists(transfer.getContact(), SYSTEM_CODE, env);
				transfer.setName(getTargetAccountName(payeeUser, transfer.getContact()));
			} catch (Exception e) {
				logger.error("收款人不存在", e);
				throw new Exception("收款人不存在");
			}
			
	        //生成收款方
            logger.info("转账,生成收款方");	
            PartyInfo payee = new PartyInfo();
            payee.setAccountId(payeeUser.getDefaultAccountId());
            payee.setMemberId(payeeUser.getMemberId());
            payee.setOperatorId(payeeUser.getCurrentOperatorId());
            payee.setIdentityNo(transfer.getContact());
            payee.setMemberName(payeeUser.getMemberName());
            payee.setMemberType(payeeUser.getMemberType());

            //生成付款方
            PartyInfo payer = TradeReqestUtil.createPay(user.getDefaultAccountId(),
                user.getMemberId(), user.getCurrentOperatorId(), user.getMobile(),
                user.getMemberName(),user.getMemberType());
            payer.setAccountName(user.getLoginName());
            payer.setEnterpriseName(user.getEnterpriseName());
            logger.info("转账,生成付款方");

            //生成交易请求
            TradeRequestInfo tradeReqest = TradeReqestUtil.createTransferTradeRequest(payer, payee,
        		new Money(transfer.getMoney()), transfer.getRemark(), new Money(), 
        		form.getSendNoteMsg() == 0 ? FALSE_STRING : TRUE_STRING);
            env.setSuccessDispalyUrl(webResource.getWalletAddress() + "/my/home.htm");
            logger.info("转账,生成交易请求");
            
            //封装客户端信息
            try {
				BeanUtils.copyProperties(tradeReqest, env);
			} catch (Exception e) {
				logger.error("获取交易环境信息失败", e);
				throw new Exception("获取交易环境信息失败");
			}
            
            try {
            	// 获取凭证
            	defaultTransferService.etransfer(tradeReqest, TradeType.TRANSFER);
            	
            	// 记录审核日志
            	logger.info(LogUtil.generateMsg(OperateTypeEnum.TRANSFER_EXAMINE, user, env, "通过"));
            	
            	// 保存审核信息，状态为“审核通过”
                this.saveAuditInfo(request, tradeReqest, transfer, account.getAvailableBalance().toString(), user, 
                		AuditStatus.AUDIT_PASSED.getCode(), StringUtils.EMPTY);
			} catch (Exception e) {
				logger.error("保存审核信息失败", e);
				throw new Exception("您没有转账审核权限");
			}
            
            // 执行转账
            try {
				PaymentResponse response = defaultTransferService.pay(tradeReqest);
				logger.info("转账结果:{}", response.getResultMessage());
				transfer.setSuccess(response.isSuccess());
			} catch (BizException e) {
				logger.error("转账到账户{}的操作失败", transfer.getContact(), e);
				throw new Exception("转账失败");
			}
		}
	}
	
	/**
	 * 导入永达互联网金融账户批量转账
	 * @return
	 */
	@RequestMapping(value = "/importKjtBatchTransfer", method = {
        RequestMethod.POST, RequestMethod.GET })
	public ModelAndView importKjtBatchTransfer(HttpServletRequest request, HttpSession session, 
			@RequestParam("batchFile") MultipartFile batchFile) {
		ModelAndView mv = new ModelAndView();
		RestResponse restP = new RestResponse();
		restP.setData(initOcx());
		mv.addObject("response", restP);
		Workbook xwb = null;
		
		// 获得用户信息
        EnterpriseMember user = this.getUser(request);
        // 软硬证书是否激活
        if (this.isCertActive(request, "hard", env) || this.isCertActive(request, "soft", env)) {
            mv.addObject("isCertActive", "yes");
        } else {
            mv.addObject("isCertActive", "no");
            if (StringUtils.isEmpty(user.getMobile())) {
                mv.addObject("message", "请先绑定数字证书或手机！");
                mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
                return mv;
            }
        }
		
		// 批量转账账户信息
		List<TransferKjtBatchForm> batchFormList = new ArrayList<TransferKjtBatchForm>();
		//批量转账账户商户订单号信息
		List<String> sourceDetailNoList=new ArrayList<String>();
		//外部批次号里
		StringBuffer sourceBatchNo=new StringBuffer();
		List<Transfer> transferList = new ArrayList<Transfer>();
		KjtTransferForm totalForm = new KjtTransferForm();
		int transferCount = 0;
		try {
			xwb = WorkbookFactory.create(batchFile.getInputStream());
			
			// 循环工作表Sheet
			int numOfSheets = xwb.getNumberOfSheets();
			for (int i=0; i<numOfSheets; i++) {
				parseOneKjtSheet(request, batchFormList, xwb.getSheetAt(i),sourceDetailNoList,sourceBatchNo);
			}			
			//判断商户订单号是否有重复			 
			for(String sourceDetailNo: sourceDetailNoList){
			    if(Collections.frequency(sourceDetailNoList, sourceDetailNo) > 1){
			    	throw new Exception("您导入的文件有重复商户订单号！");
			    }
			}
			
			// 生成转账信息
			transferCount = generateBatchTransferInfo(totalForm, batchFormList, mv, transferList, request);
			if (transferCount == 0) {
				throw new Exception("您导入的文件未包含有效数据！");
			}
			
			// 验证限额限次
            Money maxMoney = new Money(totalForm.getMaxAmount());
            String payee = "";
            List<String> emailList = totalForm.getEmailList();
            for(String email:emailList){
                payee = commMemberService.queryMemberIntegratedInfo(email, env);
                // 验证限额限次
                if (!super.validateLflt(user.getMemberId(), payee,maxMoney, TradeType.TRANSFER, mv, null, env)) {
                    mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
                    return mv;
                }
            }
            
		} catch (Exception e) {
			logger.error("导入Excel文件失败", e);
			mv.addObject("message", StringUtils.isEmpty(e.getMessage())?ErrorMsg.ERROR_EXCEL_FORMAT.getDesc():e.getMessage());
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		
		MemberAccount account = null;
		try {
			// 检查账户
			account = defaultAccountService.queryAccountById(user.getDefaultAccountId(), env);
			mv.addObject("user", user);
		} catch (Exception e) {
			logger.error("查询账户失败", e);
		}
		
		// 检查余额是否足够
		Money avaBalance = account.getAvailableBalance();
		Money totalMoney = new Money(totalForm.getTotalMoney());
		if (avaBalance.compareTo(totalMoney) == -1) {
			logger.error("账户[{}]余额不足", account.getAccountId());
			mv.addObject("message", "账户余额不足[批量转账支出金额" + totalMoney + "大于账户余额" + avaBalance + "]");
			mv.setViewName(CommonConstant.URL_PREFIX + "/business-error");
			return mv;
		}
		mv.addObject("avaBalance", avaBalance);
		
		long token = RadomUtil.createRadom();
		// 把相关信息放到Session中
		this.setJsonAttribute(session, "kjtTransferForm" + token, totalForm);
		this.setJsonAttribute(session, "transferList" + token, transferList);
		this.setJsonAttribute(session, "transferCount" + token, transferCount);
		this.setJsonAttribute(session, "sourceBatchNo" + token, sourceBatchNo);
		mv.addObject("token", token);
		mv.addObject("sourceBatchNo", sourceBatchNo);
		if (auth(request, FunctionType.EW_MY_APPROVE.getCode())) {
			// 有审核权限
			mv.setViewName(CommonConstant.URL_PREFIX + "/kjtTransfer/transfer-batch-confirm");
		} else {
			// 无审核权限
			mv.setViewName(CommonConstant.URL_PREFIX + "/kjtTransfer/transfer-batch-apply-confirm");
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
   	public RestResponse getSessionPage(HttpServletRequest request, PageInfo page, HttpSession session) {
		RestResponse response = new RestResponse();
		
		try {
		    String token = request.getParameter("token");
			List<Transfer> transferList = getTransferList(this.getJsonAttribute(session, "transferList" + token, List.class));
			SessionPage<Transfer> sessionPage = new SessionPage<Transfer>(page, new ArrayList<Transfer>());
			super.setSessionPage(transferList, sessionPage);
			response.setSuccess(true);
			response.setMessageObj(sessionPage);
		} catch (Exception e) {
			logger.error("查询分页信息出错");
			response.setMessage("查询分页信息出错");
		}
		
		return response;
   	}
	
	/**
	 * 生成批量转账信息
	 * @param totalForm 合计转账信息
	 * @param batchFormList 导入批量转账原始列表
	 * @param mv 视图
	 * @param transferList 转账信息列表
	 * @return 转账笔数
	 * @throws Exception 
	 */
	private int generateBatchTransferInfo(KjtTransferForm totalForm, List<TransferKjtBatchForm> batchFormList, 
			ModelAndView mv, List<Transfer> transferList, HttpServletRequest request) throws Exception {
		if (transferList == null) {
		    transferList = new ArrayList<Transfer>();
		}
		List<String> emailList = new ArrayList<String>();
	    int transferCount = 0;
		if (ObjectUtils.isListNotEmpty(batchFormList)) {
			// 统计总金额
			Money totalAmount = new Money();
			Money totalServiceFee = new Money();
			int size = batchFormList.size();
			for (int i=0; i<size; i++) {
				// 整理转账信息列表
				Transfer transfer = new Transfer();
				TransferKjtBatchForm batchForm = batchFormList.get(i);
				String amount = batchForm.getTransferAmount();
				transfer.setOrderNo(i+1);
				transfer.setContact(batchForm.getKjtAccount());
				try {
				    //查询收款人
					transfer.setName(getTargetAccountName(null, transfer.getContact()));
				} catch (Exception e) {
					logger.error("查询收款人失败", e);
					throw new Exception("系统中不存在收款账户" + batchForm.getKjtAccount());
				}
				transfer.setMoney(amount);
				transfer.setRemark(batchForm.getRemark());
				transfer.setSourceDetailNo(batchForm.getSourceDetailNo());
				transferList.add(transfer);
				emailList.add(transfer.getContact());
				// 更新单笔最大金额
				setMaxAmount(amount, totalForm);
				
				// 合计金额
				totalAmount.addTo(new Money(StringUtils.isEmpty(amount)?"0":amount));
				
				// 计算代发工资的服务费
				String serviceFeeStr = getServiceFee(request, amount, TradeType.TRANSFER.getBizProductCode(), null);
				Money serviceFee = new Money(StringUtils.isEmpty(serviceFeeStr) ? "0" : serviceFeeStr);
				if (serviceFee.compareTo(new Money()) == 1) {
					totalServiceFee.addTo(serviceFee);
				}
				
			}
			
			// 计算服务费
			totalForm.setServiceCharge(totalServiceFee.toString());
			totalForm.setTotalTransMoney(totalAmount.toString());
			totalForm.setTotalMoney(totalAmount.addTo(totalServiceFee).toString());
			totalForm.setEmailList(emailList);
		}
		
		transferCount = transferList.size();
		mv.addObject("transferPerCount", transferCount);
		mv.addObject("form", totalForm);
		if (MemoType.TYPE_OTHERS.getCode() != totalForm.getRemarkType()) {
			// 选择其他时，获取用户输入的备注
			totalForm.setRemark(MemoType.getDesc(totalForm.getRemarkType()));
		}
		SessionPage<Transfer> sessionPage = new SessionPage<Transfer>();
		super.setSessionPage(transferList, sessionPage);
		mv.addObject("sessionPage", sessionPage);
		
		return transferCount;
	}
	
	/**
	 * 生成付款详细信息
	 * @param transfer 转账付款信息
	 * @param request Http请求对象
	 * @return 付款详细信息，作为服务层接口参数
	 * @throws Exception 抛出生成过程中异常
	 */
	private BatchDetail generatePayDetail(Transfer transfer, HttpServletRequest request) throws Exception {
	    BatchPayDetail batchDetail = new BatchPayDetail();
        batchDetail.setTradeType(TradeType.TRANSFER.getPayCode());
	    batchDetail.setSourceDetailNo(transfer.getSourceDetailNo());
	    batchDetail.setAmount(new BigDecimal(transfer.getMoney()));
	    batchDetail.setMemo(transfer.getRemark());
	    batchDetail.setPartyInfos(generatePartyInfoList(transfer, super.getUser(request)));
     
	    return batchDetail;
    }
	
	/**
	 * 生成交易方信息列表
	 * @param transfer 转账信息
	 * @param user 登陆用户
	 * @return 交易方信息列表
	 */
	private List<com.netfinworks.batchservice.facade.model.PartyInfo> generatePartyInfoList(Transfer transfer, EnterpriseMember user) {
	    List<com.netfinworks.batchservice.facade.model.PartyInfo> partyList 
	        = new ArrayList<com.netfinworks.batchservice.facade.model.PartyInfo>();
	    
	    BigDecimal amount = new BigDecimal(transfer.getMoney());
	    
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
	    payeeInfo.setPaymentInfo(getPayeeInfo(transfer.getContact()));
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
	 * 解析sheet中的永达互联网金融账户转账信息
	 * @param batchFormList 转账信息列表
	 * @param sheet excel中的sheet对象
	 * @throws Exception 
	 */
	private void parseOneKjtSheet(HttpServletRequest request, List<TransferKjtBatchForm> batchFormList, Sheet sheet,List<String> sourceDetailNoList,StringBuffer sourceBatchNo) throws Exception {
		
		//获取批次号
		Row batchRow = sheet.getRow(1);
		String sourceBatchNoTemp=getValue(batchRow.getCell(1));		
		if(!sourceBatchNoTemp.matches("^\\w{1,32}$")){
      	  throw new Exception("商户批次不能为空或格式不正确！");
        }
		sourceBatchNo.append(sourceBatchNoTemp);		
		// 循环行Row
		int lastRowNum = sheet.getLastRowNum();
		for (int rowNum = 3; rowNum <= lastRowNum; rowNum++) {
			Row row = sheet.getRow(rowNum);
			if (row == null) {
				logger.debug(rowNum + "行为空");
				continue;
			}
			
			TransferKjtBatchForm form = new TransferKjtBatchForm();
			String kjtAccount = getValue(row.getCell(0));
			form.setKjtAccount(kjtAccount);
			
			Cell transferAmount = row.getCell(1);
			String amount = getValue(transferAmount);
			form.setTransferAmount(amount);
			
			Cell mobile = row.getCell(2);
			form.setMobile(getValue(mobile));
			
			Cell remark = row.getCell(3);
			form.setRemark(getValue(remark));
			
			Cell sourceDetailNo=row.getCell(4);
			form.setSourceDetailNo(getValue(sourceDetailNo));
			
			// 验证结果为false忽略
			if (validateForm(super.getUser(request), form, rowNum+1)) {
			    batchFormList.add(form);
			    sourceDetailNoList.add(getValue(sourceDetailNo));
			}
			
			if (batchFormList.size() > MAX_IMPORT_COUNT) {
                throw new RuntimeException(ErrorMsg.ERROR_TRANSFER_COUNT.getDesc());
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
	private boolean validateForm(EnterpriseMember user, TransferKjtBatchForm form, int rowNum) throws Exception {
	    if (form == null) {
	        return false;
	    }
	    
	    String kjtAccount = form.getKjtAccount();
	    String amount = form.getTransferAmount();
	    String mobile = form.getMobile();
	    String remark = form.getRemark();
	    String sourceDetailNo=form.getSourceDetailNo();
	    // 如果所有行都是空，为无效行，可忽略
	    if (StringUtils.isEmpty(kjtAccount) && StringUtils.isEmpty(amount)
	            && StringUtils.isEmpty(mobile) && StringUtils.isEmpty(remark)&& StringUtils.isEmpty(sourceDetailNo)) {
	        return false;
	    }
	    
        if (StringUtils.isEmpty(kjtAccount)) {
            throw new Exception("第[" + rowNum + "]行输入的账户不能为空");
        }
        
        if ((user != null) && kjtAccount.equalsIgnoreCase(user.getLoginName())) {
            throw new Exception("第[" + rowNum + "]行输入的转账账户不能和当前登陆账户一致");
        }
        
        if (StringUtils.isEmpty(amount)) {
            throw new Exception("第[" + rowNum + "]行输入的转账金额不能为空");
        }
        
        Money validMoney = null;
        try {
            validMoney = new Money(amount);
        } catch (Exception e) {
            logger.error("导入金额格式错误，{}", e);
            throw new Exception("第[" + rowNum + "]行的转账金额格式不正确");
        }
        
        if ((validMoney == null) || (validMoney.compareTo(new Money("0")) != 1)) {
            throw new Exception("第[" + rowNum + "]行的转账金额必须大于0！");
        }
        
        if (!CommonUtils.validateRegex(RegexRule.AMOUNT_2_DECINALS, amount)) {
            logger.error("导入第" + rowNum + "行的[转账金额" + amount + "]需保留2位小数！");
            throw new RuntimeException("第" + rowNum + "行的[转账金额]需保留2位小数！");
        }
        
        if (!CommonUtils.validateRegex(RegexRule.EMAIL, form.getKjtAccount()) 
                && !CommonUtils.validateRegex(RegexRule.MOBLIE, form.getKjtAccount())) {
            logger.error("导入第" + rowNum + "行的永达互联网金融账户格式不正确！");
            throw new RuntimeException("第" + rowNum + "行的永达互联网金融账户格式不正确！");
        }
        
        if (!CommonUtils.validateRegex(RegexRule.MOBLIE, form.getMobile())) {
            logger.error("导入第" + rowNum + "行的手机号码格式不正确！");
            throw new RuntimeException("第" + rowNum + "行的手机号码格式不正确！");
        }
        
        if (StringUtils.isEmpty(remark)) {
            throw new Exception("第[" + rowNum + "]行输入的备注不能为空");
        }
        if (StringUtils.isEmpty(sourceDetailNo)) {
            throw new Exception("第[" + rowNum + "]行输入的商户订单号不能为空");
        }
        if(!sourceDetailNo.matches("^\\w{1,32}$")){
        	  throw new Exception("第[" + rowNum + "]行输入的商户订单号格式不正确！");
        }
        
        
        //查询收款人
        BaseMember payeeUser = null;
        try {
            payeeUser = defaultMemberService.isMemberExists(kjtAccount, SYSTEM_CODE, env);
            if (payeeUser == null) {
                throw new RuntimeException("第" + rowNum + "行系统中不存在收款账号[" + kjtAccount + "]！");
            }
        } catch (Exception e) {
            logger.error("查询收款人异常", e);
            throw new RuntimeException("第" + rowNum + "行系统中不存在收款账户[" + kjtAccount + "]！");
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
	 * 解析Transfer的JSON字符串列表
	 * @param list
	 * @return
	 */
	private List<Transfer> getTransferList(List<?> list) {
		if (ObjectUtils.isListEmpty(list)) {
			return null;
		}
		
		List<Transfer> transferList = new ArrayList<Transfer>();
		for(Object object : list) {
			String jsonStr = String.valueOf(object);
			Transfer transfer = JSONArray.parseObject(jsonStr, Transfer.class);
			transferList.add(transfer);
		}
		
		return transferList;
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
		
		getServiceFee(request, money, TradeType.TRANSFER.getBizProductCode(), response);
		return response;
	}
	
	/**
     * 提交审核信息
     * @param tradeReqest 交易请求信息
     * @param money 金额
     * @param remark 备注
     * @param user 登陆用户
     * @param payee 收款方
     * @param payer 付款方
     * @param freezeVourceNo 冻结凭证号
     * @param sourceBatchNo 外部商户批次号
     */
    private void saveBatchAuditInfo(HttpServletRequest request, String tradeVoucherNo, KjtTransferForm form, 
			EnterpriseMember user, String status, String freezeVourceNo, String totalFeeStr,String sourceBatchNo) {
    	Map<String,Object> dataMap = new HashMap<String,Object>();
    	dataMap.put("sourceBatchNo", sourceBatchNo);
    	String totalTransMoney = form.getTotalTransMoney();
        Money totalAmount = new Money(totalTransMoney); 
        
         /*提交转账审核 */
        Audit audit = new Audit();
        audit.setTranVoucherNo(tradeVoucherNo);
        audit.setAuditType(AuditType.AUDIT_TRANSFER_KJT.getCode());
        audit.setAuditSubType(AuditSubType.BATCH.getCode());
        audit.setAmount(totalAmount);
		audit.setFee(new Money(totalFeeStr));
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
        audit.setAuditData(JSONObject.toJSONString(dataMap));   
        audit.setTranSourceVoucherNo(sourceBatchNo);
        auditService.addAudit(audit);
    }
	
	/**
	 * 提交审核信息
	 * @param tradeReqest 交易请求信息
	 * @param money 金额
	 * @param remark 备注
	 * @param user 登陆用户
	 * @param payee 收款方
	 * @param payer 付款方
	 * @param freezeVourceNo 冻结凭证号
	 */
	private void saveAuditInfo(HttpServletRequest request, TradeRequestInfo tradeReqest, Transfer transfer, 
			String balance, EnterpriseMember user, String status, String freezeVourceNo) {
		Map<String,Object> dataMap = new HashMap<String,Object>();
        dataMap.put("tradeReqest", tradeReqest);
        dataMap.put("transferInfo", transfer.getRemark());
        dataMap.put("account", user.getMemberName());
        dataMap.put("loginName", user.getOperator_login_name());
        dataMap.put("accountBalance", balance);
        dataMap.put("identityNo", transfer.getContact());
        dataMap.put("transferNum", transfer.getMoney());
        dataMap.put("transferInfo", transfer.getRemark());
        
		 /*提交转账审核 */
        Audit audit = new Audit();
        String money = transfer.getMoney();
        audit.setTranVoucherNo(tradeReqest.getTradeVoucherNo());
        audit.setAuditType(AuditType.AUDIT_TRANSFER_KJT.getCode());
        audit.setAuditSubType(AuditSubType.SINGLE.getCode());
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
        audit.setAuditData(JSONObject.toJSONString(dataMap));
        audit.setPayeeNo(CommonUtils.getEmptyStr(transfer.getContact()) + "|" + transfer.getName());
        audit.setPayeeBankInfo(transfer.getName());
        audit.setExt(StringUtils.isEmpty(freezeVourceNo) ? StringUtils.EMPTY : freezeVourceNo);
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
	
	private void removeSessionToken(HttpServletRequest request, String token) {
	    request.getSession().removeAttribute("kjtTransferForm" + token);
	    request.getSession().removeAttribute("transferList" + token);
	    request.getSession().removeAttribute("transferCount" + token);
	}
	
}
