package com.netfinworks.site.web.action.audit;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.meidusa.fastjson.JSONObject;
import com.netfinworks.batchservice.facade.model.query.PayResultDetail;
import com.netfinworks.batchservice.facade.model.query.RefundResultDetail;
import com.netfinworks.batchservice.facade.model.query.ResultDetail;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.deposit.api.domain.PageInfo;
import com.netfinworks.ma.service.base.model.BankAcctDetailInfo;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.audit.Audit;
import com.netfinworks.site.domain.domain.audit.AuditLog;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.info.PartyInfo;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.CardAttr;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domainservice.account.DefaultAccountService;
import com.netfinworks.site.domainservice.audit.AuditLogServiceImpl;
import com.netfinworks.site.domainservice.audit.AuditServiceImpl;
import com.netfinworks.site.domainservice.payment.FundsControlService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.trade.DefaultWithdrawService;
import com.netfinworks.site.domainservice.trade.impl.DefaultTransferServiceImpl;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.auth.outer.OperatorAuthOuterService;
import com.netfinworks.site.ext.integration.batchQuery.BatchQuery;
import com.netfinworks.site.ext.integration.batchQuery.BatchQueryResponse;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.trade.QueryDetailService;
import com.netfinworks.site.ext.integration.ues.UesServiceClient;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.common.constant.AuditSubType;
import com.netfinworks.site.web.common.constant.AuditType;
import com.netfinworks.site.web.util.CommonUtils;

/**
 * <p>
 * 代发工资审核
 * </p>
 * 
 * @author yangshihong
 * @version $Id: AuditAction.java, v 0.1 2014年5月21日 下午2:21:49 hdfs Exp $
 */
@Controller
public class PayoffBankTransferAuditAction extends BaseAction {
	protected Logger                log = LoggerFactory.getLogger(getClass());
	@Resource(name="accountService")
    private AccountService accountService;
	
	@Resource(name = "defaultWithdrawService")
	private DefaultWithdrawService defaultWithdrawService;
	
	@Resource(name = "defaultMemberService")
    private DefaultMemberService       defaultMemberService;
	
	@Resource(name = "defaultUesService")
    private DefaultUesService       defaultUesService;
	
	@Resource(name = "auditService")
	private AuditServiceImpl auditService;

	@Resource(name = "auditLogService")
	private AuditLogServiceImpl auditLogService;

	@Resource(name = "defaultTransferService")
	private DefaultTransferServiceImpl defaultTransferService;

	@Resource(name = "memberService")
    private MemberService     memberService;
	
	@Resource(name = "defaultAccountService")
	private DefaultAccountService defaultAccountService;

	@Resource(name = "queryDetailService")
	private QueryDetailService queryDetailService;
	
	@Resource(name = "defaultSmsService")
    private DefaultSmsService       defaultSmsService;
	
	@Resource(name = "fundsControlService")
    private FundsControlService fundsControlService;
	
	@Resource
    private OperatorAuthOuterService operatorAuthOuterService;
	
	@Resource(name = "uesService")
	private UesServiceClient uesServiceClient; 
	/**
	 * 代发工资 转账到银行卡 (审核详情)
	 * 
	 * @param model
	 * @param request
	 * @return
	 * @throws ServiceException
	 */
	@RequestMapping(value = "/my/payoffBankAudit.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView payoffBankAudit(HttpServletRequest request,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		//用于显示页面附加字段
		Map<String, Object> data = new HashMap<String, Object>();
		
		//查询审核记录
		String id = request.getParameter("id");
		Audit audit = auditService.queryAudit(id);
		
		data.put("id", audit.getId());
		data.put("transId", audit.getTranVoucherNo());// 订单号
		data.put("createTime",sf.format(audit.getGmtCreated()));//申请时间
		data.put("operatorName", audit.getOperatorName());// 申请操作员名称
		data.put("auditorName", audit.getAuditorName());// 审核员名称
		
		if(audit.getGmtModified() != null){
			data.put("auditTime", sf.format(audit.getGmtModified()));// 审核时间
		}
		
		data.put("remark", audit.getRemark());//备注
		data.put("status", audit.getStatus());//审核状态
		
		String subType = audit.getAuditSubType();//单笔/批量区分
		if(subType ==null){
			subType=AuditSubType.SINGLE.getCode();
		}
		
		if (AuditSubType.BATCH.getCode().equals(subType)) {// 批量查看详情
			BatchQuery req = new BatchQuery();
			String batchNo = audit.getTranVoucherNo();// 批次号
			
			String currentPage = request.getParameter("currentPage");
			if (StringUtils.isBlank(currentPage) || currentPage.equals("0")) {
	            currentPage = "1";
	        }
			
			PageInfo pageInfo = new PageInfo();
			pageInfo.setPageSize(3);
			pageInfo.setCurrentPage(Integer.parseInt(currentPage));
			
			req.setBatchNo(batchNo);
			req.setPageNum(Integer.parseInt(currentPage));
			req.setPageSize(3);
			
			List<Map<String,String>> payResultList = null;
			
			//获得批次明细
			BatchQueryResponse queryResponse = queryDetailService.batchQueryDetail(req);
			
			if (queryResponse.getResultCode().equals("S")) {// 成功
				data.put("resultMessage", queryResponse.getResultMessage());
				data.put("totalSize", queryResponse.getTotalSize());
				
				PageResultList resultList = new PageResultList();
				pageInfo.setTotalItem(queryResponse.getTotalSize());
				resultList.setPageInfo(pageInfo);// 分页信息
				
				List<ResultDetail> list = queryResponse.getResultDetails();
				BigDecimal allFee = new BigDecimal("0");
				if (list != null && list.size() > 0) {
					payResultList = new ArrayList<Map<String,String>>();
					for (int i = 0; i < list.size(); i++) {// 目前只能强转了,稍后和维金沟通如何获取两个批量明细的子类
						Map<String,String> detail = new HashMap<String,String>();
						PayResultDetail pay = (PayResultDetail) list.get(i);
						detail.put("tradeVoucherNo", pay.getTradeVoucherNo());
						if(pay.getPayeeBankCode() == null || pay.getPayeeBankCode().isEmpty()){
							detail.put("payeeAccountNo", pay.getPayeeLoginId());
							detail.put("payeeAccountInfo", getTargetAccountName(null, pay.getPayeeLoginId()));
						}else{
							detail.put("payeeAccountNo", pay.getPayeeBankAccountName());
							detail.put("payeeAccountInfo", pay.getPayeeBranchName()+"\n"+CommonUtils.getMaskData(uesServiceClient.getDataByTicket(pay.getPayeeBankAccountNo())));
						}
						detail.put("amount", pay.getAmount().toString());
						Map<String, Object> fee = JSONObject.parseObject(pay.getExtension());
						if(fee.get("fee") != null){
							detail.put("fee", (String)fee.get("fee"));
							allFee = allFee.add(new BigDecimal((String)fee.get("fee")));
						}
						detail.put("memo", pay.getMemo());
						detail.put("status", pay.getStatus());
						
						payResultList.add(detail);
					}
					resultList.setInfos(payResultList);// list
				}
				data.put("page", resultList);
				data.put("allAmount", audit.getAmount().getAmount());//设置转账总金额
				data.put("allFee", allFee);//设置转账总金额
				data.put("totalAmount", (audit.getAmount().add(audit.getFee())).getAmount());//设置总金额
			} else {
				// 失败
			}
		} else {// 单笔查看详情
			Map<String, Object> auditData = JSONObject.parseObject(audit
					.getAuditData());
			Map<String, Object> tradeReqestMap = (Map<String, Object>) auditData
					.get("tradeReqest");
			
			boolean isSendMessage = (Boolean) tradeReqestMap.get("sendMessage");
			if (isSendMessage) {// 判断是否短信通知
				data.put("isSend", "是");
			} else {
				data.put("isSend", "否");
			}
			
			BigDecimal amount = audit.getAmount().getAmount();
			data.put("amount", amount);// 交易金额
			
			//手续费
			if(audit.getFee() ==null){
				audit.setFee(new Money("0.00"));
			}
			BigDecimal fee = audit.getFee().getAmount();
			data.put("fee", fee);//手续费
			
			data.put("totalAmount", amount.add(fee));// 总金额
			
			data.put("memo", tradeReqestMap.get("memo"));//设置支付理由
			data.put("remark", audit.getRemark());// 设置备注信息
			data.put("status", audit.getStatus());// 审核结果
			
			String[] arr = null;
			String payeeNo = null;
			String payeeAccountName = null;
			if (audit.getPayeeNo() != null) {
				arr = audit.getPayeeNo().split("\\|");
				if (arr != null && arr.length > 1) {
					payeeNo = arr[0];
					payeeAccountName = arr[1];
					if(AuditType.AUDIT_PAYOFF_BANK.getCode().equals(audit.getAuditType())){
						payeeNo = CommonUtils.getMaskData(uesServiceClient.getDataByTicket(payeeNo));
					}
				} else {
					payeeNo = arr[0];
				}
			}

			data.put("payeeNo", payeeNo);// 设置收款账号
			data.put("payeeAccountName", payeeAccountName);// 设置收款账户名称
			data.put("payeeBankInfo", audit.getPayeeBankInfo());// 设置收款支行信息
		}
		// 硬证书是否激活
		if (this.isCertActive(request, "hard", env) || this.isCertActive(request, "soft", env)) {
			data.put("isCertActive", "yes");
		} else {
			data.put("isCertActive", "no");
		}
		
		restP.setData(data);
		restP.setSuccess(true);
		
		// 当前操作员审核权限
//		boolean isDraw = auth(request, FunctionType.EW_MY_APPROVE.getCode());
		if (audit.getStatus().equals("1") && audit.getAuditSubType().equals(AuditSubType.SINGLE.getCode())) {
			return new ModelAndView(CommonConstant.URL_PREFIX
					+ "/application/payOffBank_AuditDetailExecute", "response", restP);
		}else if(audit.getStatus().equals("1") && audit.getAuditSubType().equals(AuditSubType.BATCH.getCode())){
			return new ModelAndView(CommonConstant.URL_PREFIX
					+ "/application/payOffBank_BatchDetailexecute", "response", restP);
		}else if(audit.getAuditSubType().equals(AuditSubType.BATCH.getCode()) && !audit.getStatus().equals("1") ){
			return new ModelAndView(CommonConstant.URL_PREFIX
					+ "/application/payOffBank_BatchDetail", "response", restP);
		}else{
			return new ModelAndView(CommonConstant.URL_PREFIX
					+ "/application/payOffBank_AuditDetail", "response", restP);
		}
	}

	/**
	 * 转账到银行(审核确认)
	 * 
	 * @param model
	 * @param request
	 * @return
	 * @throws ServiceException
	 */
	@RequestMapping(value = "/my/payoffBankAuditConfirm.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody RestResponse payoffBankAuditConfirm(HttpServletRequest request,HttpSession session,OperationEnvironment env,
			HttpServletResponse resp){
		RestResponse restP = new RestResponse();
		
		// 获得用户信息
		EnterpriseMember user = this.getUser(request);
		String mobileCaptcha = request.getParameter("mobileCaptcha");
		
		String id = request.getParameter("id");
		String operate = request.getParameter("operate"); // 获得密码
		String remark = request.getParameter("remark");// 审核通过备注
		
		Audit audit = auditService.queryAudit(id);
		// 记录审核日志
		AuditLog log = new AuditLog();
		log.setAuditId(audit.getId());
		log.setAuditorId(user.getOperatorId());
		log.setAuditorName(user.getOperator_login_name());
		log.setOperateType(operate);
		log.setAuditRemark(remark);
		log.setGmtCreated(new Date());

		Money fee = audit.getFee();
		if(fee==null){
			fee = new Money("0.00");
		}
		Money allAmount = audit.getAmount().add(fee);
		
		if ("reject".equals(operate)) {
		    try{
                if (AuditSubType.SINGLE.getCode().equals(audit.getAuditSubType())) {
                    // 资金解冻
                    if (!fundsControlService.unfreeze(audit.getExt(), allAmount, env)) {
                        logger.error("资金解冻失败");
                    }
                } else {
                    if (defaultTransferService.unfreezeBatch(audit.getTranVoucherNo()).getResultCode().equals("F002")) {
                        logger.error("资金解冻失败");
                    }
                }
            }catch(Exception ignore) {
                logger.error("资金解冻出现异常", ignore);
            }
			
			audit.setId(audit.getId());
			audit.setAuditorId(user.getOperatorId());
			audit.setAuditorName(user.getOperator_login_name());
			audit.setGmtModified(new Date());
			audit.setRemark(remark);
			audit.setStatus("3");// 3.拒绝审核
			auditService.updAudit(audit);
			auditLogService.addAuditLog(log);
			restP.setSuccess(true);
			return restP;
		}
		
		
		if (StringUtils.isNotEmpty(mobileCaptcha)) {
			// 检查手机校验码
			if(!mobileCaptcha.equals(session.getAttribute("mobileCaptcha"))) {
				restP.setSuccess(false);
				restP.setMessage("手机校验码输入错误!");
				return restP;
			}
		} else {
			// 验证硬证书签名
			String payPassword = request.getParameter("payPassword");
			String signedData = request.getParameter("signedData");
			try {
				if(!validateSignature(request, payPassword, signedData, restP, null, env)) {
					restP.setSuccess(false);
					restP.setMessage("证书不存在!");
					return restP;
				}
			} catch (UnsupportedEncodingException e) {
				logger.error("验证证书时编码错误", e);
				restP.setMessage("您未插入快捷盾或证书已经过期！");
				restP.setSuccess(false);
				return restP;
			}
		}
		
		// 检查支付密码
		if(!validatePayPassword(request, user, restP, null)) {
			restP.setSuccess(false);
			restP.setMessage("支付密码错误!");
			return restP;
		}
		
		if (AuditSubType.SINGLE.getCode().equals(audit.getAuditSubType())) {
    		try{
    			// 资金解冻
    			if (!fundsControlService.unfreeze(audit.getExt(), allAmount, env)) {
    				logger.error("资金解冻失败");
    			}
    		}catch(Exception ignore) {
    			logger.error("资金解冻出现异常", ignore);
    		}
		}
		
		String subType = audit.getAuditSubType();
		if(subType==null){
			subType = AuditSubType.SINGLE.getCode();//如果为空 默认为单笔
		}
		
		if(AuditSubType.SINGLE.getCode().equals(subType)){
			Map<String, Object> auditData = JSONObject.parseObject(audit
					.getAuditData());
			Map<String, Object> requestMap = (Map<String, Object>) auditData
					.get("tradeReqest");
			Map<String, Object> payerMap = (Map<String, Object>) requestMap
					.get("payer");
			
			TradeRequestInfo info = new TradeRequestInfo();
			// 金额
			info.setAmount(audit.getAmount());
			
			/* 付款方 */
			PartyInfo payer = new PartyInfo();
			payer.setAccountId((String) payerMap.get("accountId"));
			payer.setMemberId((String) payerMap.get("memberId"));
			payer.setMemberType(MemberType.valueOf((String) payerMap
					.get("memberType")));
			payer.setOperatorId((String) payerMap.get("operatorId"));
			info.setPayer(payer);

			/* 录入员环境信息 */
			info.setClientIp((String) requestMap.get("clientIp"));
			info.setClientId((String) requestMap.get("clientId"));
			info.setBrowser((String) requestMap.get("browser"));
			info.setCookie((String) requestMap.get("cookie"));
			info.setClientMac((String) requestMap.get("clientMac"));
			info.setDomainName((String) requestMap.get("domainName"));
			info.setReferUrl((String) requestMap.get("referUrl"));
			info.setTradeSourceVoucherNo((String) requestMap
					.get("tradeSourceVoucherNo"));
			info.setTradeType(TradeType.valueOf((String) requestMap
					.get("tradeType")));
			info.setTradeVoucherNo((String) requestMap.get("tradeVoucherNo"));
			
			// 从审核表中获取到转账信息
			BankAcctDetailInfo bankAcctInfo = convertToAcctDetailInfo(audit);
			
			try {
				transferToBankAccount(request, session, audit.getAmount(), bankAcctInfo, info);
			} catch (Exception e) {
				logger.error("转账失败！", e);
			}
			audit.setPayVoucherNo(info.getPaymentVoucherNo());
		}else{
			try {
				defaultTransferService.batchTransferPay(audit.getTranVoucherNo());
			}catch(Exception e){
				logger.error("批量转账到账户{}的操作失败", e);
			}
		}
		
		
//		 更新审核状态
		audit.setId(audit.getId());
		audit.setStatus("2");// 2.已审核
		audit.setRemark(remark);
		audit.setAuditorId(user.getOperatorId());
		audit.setAuditorName(user.getOperator_login_name());
		audit.setGmtModified(new Date());
		boolean isUpdate = auditService.updAudit(audit);
		
		auditLogService.addAuditLog(log);
		if (!isUpdate) {
			logger.error("更新审核表状态失败");
		}
		
		restP.setSuccess(true);
		return restP;
	}
	
	/**
	 * 代发工资 转账银行账户
	 * @param request
	 * @param session
	 * @param form
	 * @param bankAcctInfo
	 * @return true-成功，false-失败
	 * @throws Exception 
	 */
	private void transferToBankAccount(HttpServletRequest request, HttpSession session, Money transMoney,
			BankAcctDetailInfo bankAcctInfo, TradeRequestInfo tradeReq) throws Exception {
		
		
        tradeReq.setAmount(transMoney);
        tradeReq.setTradeType(TradeType.PAY_TO_BANK);
        
        bankAcctInfo.setBranchNo(bankAcctInfo.getBranchNo());
        bankAcctInfo.setCardAttribute(CardAttr.COMPANY.getCode());
		
		// 调用提现接口实现转账到银行
        defaultWithdrawService.submitBankTransfer(tradeReq, bankAcctInfo);
	}
	
	/**
	 * 获取账户详细信息
	 * @param form
	 * @return
	 */
	private BankAcctDetailInfo convertToAcctDetailInfo(Audit audit) {
		Map<String, Object> auditData = JSONObject.parseObject(audit
				.getAuditData());
		Map<String, Object> payeeBankAcctInfo = (Map<String, Object>) auditData
				.get("payeeBankAcctInfo");
		
		// 目标账户详细信息
		BankAcctDetailInfo bankAcctInfo = new BankAcctDetailInfo();
		bankAcctInfo.setBankName((String)payeeBankAcctInfo.get("bankName"));
		bankAcctInfo.setRealName((String)payeeBankAcctInfo.get("realName"));
		bankAcctInfo.setBankAccountNum((String)payeeBankAcctInfo.get("bankAccountNum"));//加密
		bankAcctInfo.setBankBranch((String)payeeBankAcctInfo.get("bankBranch"));
		bankAcctInfo.setProvince((String)payeeBankAcctInfo.get("province"));
		bankAcctInfo.setCity((String)payeeBankAcctInfo.get("city"));
		bankAcctInfo.setMobileNum((String)payeeBankAcctInfo.get("mobileNum"));//加密
		bankAcctInfo.setBranchNo((String)payeeBankAcctInfo.get("branchNo"));
		return bankAcctInfo;	
	}
}
