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
import com.netfinworks.site.domain.domain.info.EncryptData;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.info.PartyInfo;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.CardAttr;
import com.netfinworks.site.domain.enums.DeciphedQueryFlag;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.enums.OperateTypeEnum;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domain.exception.BizException;
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
import com.netfinworks.site.web.common.constant.AuditType;
import com.netfinworks.site.web.common.constant.FunctionType;
import com.netfinworks.site.web.util.CommonUtils;
import com.netfinworks.site.web.util.LogUtil;

/**
 * <p>
 * 提现审核
 * </p>
 * 
 * @author yangshihong
 * @version $Id: AuditAction.java, v 0.1 2014年5月21日 下午2:21:49 hdfs Exp $
 */
@Controller
public class WithdrawAuditAction extends BaseAction {
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
	 * 提现审核(点击"查看详情")
	 * 
	 * @param model
	 * @param request
	 * @return
	 * @throws ServiceException
	 */
	@RequestMapping(value = "/my/withdrawAudit.htm", method = { RequestMethod.GET })
	public ModelAndView withdrawAudit(HttpServletRequest request,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = initOcx();
		String id = request.getParameter("id");
		// 因为还不支持按订单号/批次号查询 以下为测试数据
		Audit audit = auditService.queryAudit(id);// 待审核数据
		Map<String, Object> auditData = JSONObject.parseObject(audit
				.getAuditData());
		Map<String, Object> payeeBankAcctMap = (Map<String, Object>) auditData
				.get("payeeBankAcctInfo");// 收款账户信息(目前只有提现审核才有)
		Map<String, Object> requestMap = (Map<String, Object>) auditData
				.get("tradeReqest");
		Map<String, Object> payerMap = (Map<String, Object>) requestMap
				.get("payer");// 付款账户信息
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String createTime = sdf.format(audit.getGmtCreated());

		data.put("id", audit.getId());
		
		data.put("validateDay", true);//该字段判断是否可以使用手机校验码用于审核  
		
		data.put("enterpriseName", payerMap.get("enterpriseName"));// 付款企业名称

		if (payeeBankAcctMap != null && payeeBankAcctMap.size() > 0) {// 收款账户信息(目前只有提现审核才有)
			data.put("bankBranch", payeeBankAcctMap.get("bankBranch"));// 银行名称
			// 收款方信息
			data.put("bankAccount", payeeBankAcctMap.get("bankAccountNumMask"));// 收款账号
			data.put("payeeName", payeeBankAcctMap.get("realName"));// 收款方名称
		}
		// 提现信息
		data.put("transId", audit.getTranVoucherNo());// 交易订单号
		data.put("createTime", createTime);// 创建时间
		data.put("auditType", audit.getAuditType());// 交易类型:提现审核、转账审核、退款审核等等....
		data.put("status", audit.getStatus());//审核状态
		
		BigDecimal amount = audit.getAmount().getAmount();
		data.put("amount",amount );// 交易金额
		
		if(audit.getFee()== null){
			audit.setFee(new Money("0.00"));
		}
		BigDecimal fee = audit.getFee().getAmount();
		data.put("fee", fee);// 服务费
		
		data.put("totalAmount",fee.add(amount));// 总金额
		
		String[] arr = null;
		String payeeNo = null;
		String payeeAccountName = null;
		if (audit.getPayeeNo() != null) {
			arr = audit.getPayeeNo().split("\\|");
			if (arr != null && arr.length > 1) {
				payeeNo = arr[0];
				payeeAccountName = arr[1];
				if(AuditType.AUDIT_WITHDRAW.getCode().equals(audit.getAuditType())){
					payeeNo = CommonUtils.getMaskData(uesServiceClient.getDataByTicket(payeeNo));
				}
			} else {
				payeeNo = arr[0];
			}
		}

		data.put("payeeNo", payeeNo);// 设置收款账号
		data.put("payeeAccountName", payeeAccountName);// 设置收款账户名称
		data.put("payeeBankInfo", audit.getPayeeBankInfo());// 设置收款支行信息
		
		data.put("memo",requestMap.get("memo"));//设置支付理由
		data.put("remark", audit.getRemark());//设置备注
		// 申请信息
		data.put("applyTime",
				new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(audit.getGmtCreated()));// 申请时间
		data.put("operatorName", audit.getOperatorName());// 申请操作员

		// 审核信息
		if(audit.getGmtModified() != null){
			data.put("auditTime",
					new SimpleDateFormat("yyyy-MM-dd").format(audit.getGmtModified()));// 审核时间
		}
		
		data.put("auditorName", audit.getAuditorName());// 审核操作员
		
		// 硬证书是否激活
		if (this.isCertActive(request, "hard", env) || this.isCertActive(request, "soft", env)) {
			data.put("isCertActive", "yes");
		} else {
			data.put("isCertActive", "no");
		}
		
		restP.setData(data);
		restP.setSuccess(true);
		String backUrl = CommonConstant.URL_PREFIX;
		
		// 当前操作员审核权限
		boolean isDraw = auth(request, FunctionType.EW_MY_APPROVE.getCode());
		
		if(!isDraw){//无审核权限,跳转详情页面(底部无审核按钮)
			backUrl += "/withdrawal/draw_AuditDetail";
		}else{
			// 判断是否通过审核，跳到不同页面
			if (audit.getStatus().equals("2") || audit.getStatus().equals("3")) {// 跳转到查看详情页面（底部不带审核）
				backUrl += "/withdrawal/draw_AuditDetail";
			} else {// 跳转到查看详情页面（底部带审核）
				backUrl += "/withdrawal/draw_AuditDetailExecute";
			}
		}
		return new ModelAndView(backUrl, "response", restP);
	}

	/**
	 * 提现审核确认页
	 * 
	 * @param model
	 * @param request
	 * @return
	 * @throws ServiceException
	 */
	@RequestMapping(value = "/my/withdrawAuditConfirm.htm", method = { RequestMethod.POST })
	public @ResponseBody RestResponse withdrawAuditConfirm(HttpServletRequest request,HttpSession session,
			HttpServletResponse resp,TradeEnvironment env){
		RestResponse restP = new RestResponse();
		ModelAndView mv = new ModelAndView();
		EnterpriseMember user = getUser(request);
		Map<String, Object> data = new HashMap<String, Object>();
        Map<String, String> errorMap = new HashMap<String, String>();
        checkUser(user, errorMap, restP);
        
		String id = request.getParameter("id");
		String operate = request.getParameter("operate");// 审核状态：通过 OR 拒绝
		String remark = request.getParameter("remark");
		
		// 记录审核日志
        logger.info(LogUtil.generateMsg(OperateTypeEnum.WITHDRAW_EXAMINE, user, env,
                "reject".equals(request.getParameter("operate")) ? "通过" : "拒绝"));
		
		// 查询待审核信息
		Audit audit = auditService.queryAudit(id);

		// 记录审核日志
		AuditLog log = new AuditLog();
		log.setAuditId(id);
		log.setAuditorId(user.getOperatorId());
		log.setAuditorName(user.getOperator_login_name());
		log.setOperateType(operate);
		log.setAuditRemark(remark);
		log.setGmtCreated(new Date());
		
		if(audit.getFee() == null){
			audit.setFee(new Money("0.00"));
		}
		Money fee = audit.getFee();
		Money allAmount = audit.getAmount().add(fee);
		
		if ("reject".equals(operate)) {// 审核拒绝
			try{
				// 资金解冻
				if (!fundsControlService.unfreeze(audit.getExt(), allAmount, env)) {
					logger.error("资金解冻失败");
				}
			}catch(Exception ignore) {
				logger.error("资金解冻出现异常", ignore);
			}
		    audit.setId(id);
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
		
        String mobileCaptcha = request.getParameter("mobileCaptcha");
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
		
		Map<String, Object> auditData = JSONObject.parseObject(audit
				.getAuditData());
		Map<String, Object> requestMap = (Map<String, Object>) auditData
				.get("tradeReqest");
		Map<String, Object> payerMap = (Map<String, Object>) requestMap
				.get("payer");
		Money amount = audit.getAmount();
		
		TradeRequestInfo info = new TradeRequestInfo();
		// 金额
		info.setAmount(amount);

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
		
		try{
			// 资金解冻
			if (!fundsControlService.unfreeze(audit.getExt(), allAmount, env)) {
				logger.error("资金解冻失败");
			}
		}catch(Exception ignore) {
			logger.error("资金解冻出现异常", ignore);
		}
		
		// 调用提现接口
        try {
        	boolean flag = defaultWithdrawService.submitWithdraw(info,
    				((String) auditData.get("bankCardId")), env);
        	if (!flag) {
        		logger.error("无效的银行卡账户名");
            }
		} catch (Exception e) {
			logger.error("调用提现接口异常", e);
		}
		
		// 更新审核状态
		audit.setId(id);
		audit.setStatus("2");// 2.已审核
		audit.setRemark(remark);
		audit.setAuditorId(user.getOperatorId());
		audit.setAuditorName(user.getOperator_login_name());
		 audit.setPayVoucherNo(info.getPaymentVoucherNo());
		audit.setGmtModified(new Date());
		boolean isUpdate = auditService.updAudit(audit);
		auditLogService.addAuditLog(log);

		if (!isUpdate) {
			logger.error("更新审核表状态失败");
		}

		restP.setSuccess(true);
		return restP;
	}
}
