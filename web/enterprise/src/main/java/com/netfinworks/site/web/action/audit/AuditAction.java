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

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.tools.config.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.meidusa.fastjson.JSONArray;
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
import com.netfinworks.site.domain.domain.info.BaseInfo;
import com.netfinworks.site.domain.domain.info.EncryptData;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.info.PartyInfo;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.MemberAccount;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.domain.response.CoTradeQueryResponse;
import com.netfinworks.site.domain.domain.trade.AuditListQuery;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.domain.trade.TradeRefundRequset;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.AccessChannel;
import com.netfinworks.site.domain.enums.BizType;
import com.netfinworks.site.domain.enums.CardAttr;
import com.netfinworks.site.domain.enums.DeciphedQueryFlag;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.enums.MemberType;
import com.netfinworks.site.domain.enums.OperateTypeEnum;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domain.exception.MemberNotExistException;
import com.netfinworks.site.domainservice.account.DefaultAccountService;
import com.netfinworks.site.domainservice.audit.AuditLogServiceImpl;
import com.netfinworks.site.domainservice.audit.AuditServiceImpl;
import com.netfinworks.site.domainservice.convert.RefundConvertor;
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
import com.netfinworks.site.ext.integration.trade.TradeService;
import com.netfinworks.site.ext.integration.ues.UesServiceClient;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.common.constant.AuditSubType;
import com.netfinworks.site.web.common.constant.AuditType;
import com.netfinworks.site.web.util.CommonUtils;
import com.netfinworks.site.web.util.LogUtil;
import com.netfinworks.site.web.util.TradeReqestUtil;
import com.netfinworks.tradeservice.facade.response.PaymentResponse;
import com.netfinworks.tradeservice.facade.response.RefundResponse;

/**
 * <p>
 * 记录审核
 * </p>
 * 
 * @author yangshihong
 * @version $Id: AuditAction.java, v 0.1 2014年5月21日 下午2:21:49 hdfs Exp $
 */
@Controller
public class AuditAction extends BaseAction {
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
	
	@Resource(name = "tradeService")
	private TradeService tradeService;
	
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
	 * 批量审核 (通过点击"审核"按钮)
	 * 
	 * @param model
	 * @param request
	 * @return
	 * @throws ServiceException
	 */
	@RequestMapping(value = "/my/batchAudit.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView batchAudit(HttpServletRequest request,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, String> errorMap = new HashMap<String, String>();
		EnterpriseMember user = getUser(request);
		checkUser(user, errorMap, restP);
		String currentPage = request.getParameter("currentPage");
		if (StringUtils.isBlank(currentPage)) {
			currentPage = "1";
		}
		PageInfo pageInfo = new PageInfo();
		pageInfo.setCurrentPage(Integer.parseInt(currentPage));
		Map<String, Object> data = initOcx();
		String[] ids = request.getParameterValues("id");
		List<Map<String, Object>> auditsList = new ArrayList<Map<String, Object>>();// 需要审核的列表
		String allId = "";
		if(ids != null){
			for (int i = 0; i < ids.length; i++){
				allId += ids[i] + "|";
			}
		}else{
			allId = request.getParameter("ids");
			ids = allId.split("\\|");
		}
		
		for (int i = 0; i < ids.length; i++) {
			Audit audit = auditService.queryAudit(ids[i]);

			Map<String, Object> map = new HashMap<String, Object>();
			map.put("transId", audit.getTranVoucherNo());// 订单号
			map.put("applyTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(audit.getGmtCreated()));// 创建时间
			map.put("amount", audit.getAmount().getAmount().toString());// 金额
			if(audit.getFee() == null){
				audit.setFee(new Money("0.00"));
			}
			
			Money fee = audit.getFee();
			if(fee.equals(new Money("0"))){
				map.put("fee", fee);
			}else{
				map.put("fee","-" + fee);
			}
			
			map.put("applyName", audit.getOperatorName());// 申请人
			map.put("payeeAccount", audit.getPayeeNo());// 收款账户账号
			map.put("auditType", audit.getAuditType());
			map.put("payeeBankInfo",audit.getPayeeBankInfo());//收款银行的银行名称
			String payeeNo = audit.getPayeeNo();
			String payeeAccountName = null;
			if(payeeNo != null){
				String[] payeeNos = payeeNo.split("\\|");
				payeeNo = payeeNos[0];
				payeeAccountName = payeeNos[1];
				if(AuditType.AUDIT_PAYOFF_BANK.getCode().equals(audit.getAuditType())
						|| AuditType.AUDIT_TRANSFER_BANK.getCode().equals(audit.getAuditType())
						|| AuditType.AUDIT_WITHDRAW.getCode().equals(audit.getAuditType())){
					payeeNo = CommonUtils.getMaskData(uesServiceClient.getDataByTicket(payeeNo));
				}
				map.put("payeeNo", payeeNo);//银行卡账号
				map.put("payeeAccountName", audit.getPayeeMemberId());
			}
			auditsList.add(map);
		}
		PageResultList resultList = new PageResultList();
		resultList.setInfos(auditsList);
		data.put("AllId", allId);// 传到页面的隐藏域里，再传回到action，调用单笔的去一笔笔审核
		data.put("page", resultList);
		data.put("Url", "/my/batchAuditConfirm.htm");
		
		// 硬证书是否激活
		if (this.isCertActive(request, "hard", env) || this.isCertActive(request, "soft", env)) {
			data.put("isCertActive", "yes");
		} else {
			data.put("isCertActive", "no");
		}

		
		restP.setData(data);
		restP.setSuccess(true);
		return new ModelAndView(
				CommonConstant.URL_PREFIX + "/audit/batchAudit", "response",
				restP);
	}
	
	/**
	 *  批量审核处理(通过点击"审核"按钮)
	 */
	@RequestMapping(value = "/my/batchAuditConfirm.htm", method = { RequestMethod.POST })
	public @ResponseBody RestResponse batchAudit(HttpServletRequest request,HttpSession session,OperationEnvironment env,
			TradeEnvironment tradeEnv,HttpServletResponse resp){
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
	    Map<String, String> errorMap = new HashMap<String, String>();
		// 获得用户信息
		EnterpriseMember user = this.getUser(request);
		
		String Ids = request.getParameter("ids");// 页面隐藏域传值
		String[] batchIds = Ids.split("\\|");

		String operate = request.getParameter("operate");// 审核状态：通过 OR 拒绝
		String remark = request.getParameter("remark");// 审核通过备注

		checkUser(user, errorMap, restP);
		AuditLog log = new AuditLog();
		// 审核拒绝
		Money fee = null;
		Money amount = null;
		Money allAmount = null;
		
		List<Audit> success = new ArrayList<Audit>();
		List<Audit> fail = new ArrayList<Audit>();
		if ("reject".equals(operate)) {// 拒绝审核
			for (int i = 0; i < batchIds.length; i++) {
				log.setAuditId(batchIds[i]);
				log.setAuditorId(user.getOperatorId());
				log.setAuditorName(user.getOperator_login_name());
				log.setOperateType(operate);
				log.setAuditRemark(remark);
				log.setGmtCreated(new Date());
				Audit audit = auditService.queryAudit(batchIds[i]);
				
				fee = audit.getFee();
				if(fee == null){
					fee = new Money("0.00");
				}
				allAmount = audit.getAmount().add(fee);
				
				try{
	                if (!AuditSubType.BATCH.getCode().equals(audit.getAuditSubType())) {
	                    if("refund".equals(audit.getAuditType())){
	                        String freezeNo = "";
	                        if(audit.getExt() != null && !audit.getExt().isEmpty()){
	                            Map<String, Object> ext = JSONObject.parseObject(audit.getExt());
	                            freezeNo = (String)ext.get("bizPaymentSeqNo");
	                        }
	                        if (!fundsControlService.unfreeze(freezeNo, allAmount, env)) {
	                            logger.error("资金解冻失败");
	                        }
	                    }else{
	                     // 资金解冻
	                        if (!fundsControlService.unfreeze(audit.getExt(), allAmount, env)) {
	                            logger.error("资金解冻失败");
	                        }
	                    }
	                } else {
	                    if (defaultTransferService.unfreezeBatch(audit.getTranVoucherNo()).getResultCode().equals("F002")) {
	                        logger.error("资金解冻失败");
	                    }
	                }
	            }catch(Exception ignore) {
	                logger.error("资金解冻出现异常", ignore);
	            }
				
				audit.setId(batchIds[i]);
				audit.setAuditorId(user.getOperatorId());
				audit.setAuditorName(user.getOperator_login_name());
				audit.setGmtModified(new Date());
				audit.setRemark(remark);
				audit.setStatus("3");// 3.拒绝审核
				auditService.updAudit(audit);
				auditLogService.addAuditLog(log);
				success.add(audit);
			}
			
//			if(fail.size()>0){
//				restP.setMessageObj(fail);
//				restP.setSuccess(false);
//				return restP;
//			}
			restP.setSuccess(true);
			return restP;
		}
		
		String mobileCaptcha = request.getParameter("mobileCaptcha");
		if (StringUtils.isNotEmpty(mobileCaptcha)) {
			// 检查手机校验码
			if(!mobileCaptcha.equals(session.getAttribute("mobileCaptcha"))) {
				restP.setSuccess(false);
				restP.setMessage("手机校验码错误");
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
		if(!super.validatePayPassword(request, user, restP, null)) {
			restP.setSuccess(false);
			restP.setMessage("您输入的支付密码不正确！");
			return restP;
		}
		
		
		for (int i = 0; i < batchIds.length; i++) {
			Audit audit = auditService.queryAudit(batchIds[i]);
			
			String subType = audit.getAuditSubType();
			if(subType==null){
				subType = AuditSubType.SINGLE.getCode();//如果为空 默认为单笔
			}
			
			if ("refund".equals(audit.getAuditType())) {
                logger.info(LogUtil.generateMsg(OperateTypeEnum.REFUND_EXAMINE, user, env,
                        "reject".equals(operate) ? "拒绝" : "通过"));
			    
				// 记录审核日志
				log.setAuditId(batchIds[i]);
				log.setAuditorId(user.getOperatorId());
				log.setAuditorName(user.getOperator_login_name());
				log.setOperateType(operate);
				log.setAuditRemark(remark);
				log.setGmtCreated(new Date());
				
				if(audit.getFee() == null){
					audit.setFee(new Money("0.00"));
				}
				
				if(AuditSubType.SINGLE.getCode().equals(subType)){
				    fee = audit.getFee();
                    allAmount = audit.getAmount().add(fee);
                    try{
                        // 资金解冻
                        String freezeNo = "";
                        if(audit.getExt() != null && !audit.getExt().isEmpty()){
                            Map<String, Object> ext = JSONObject.parseObject(audit.getExt());
                            freezeNo = (String)ext.get("bizPaymentSeqNo");
                        }
                        if (!fundsControlService.unfreeze(freezeNo, allAmount, env)) {
                            logger.error("资金解冻失败");
                        }
                    }catch(Exception ignore) {
                        logger.error("资金解冻出现异常", ignore);
                    }
				    
					TradeRequestInfo req = RefundConvertor
							.convertRefundApply(user, tradeEnv);
					Map<String, Object> auditData = JSONObject.parseObject(audit
							.getAuditData());
					BaseInfo baseInfo = JSONArray.parseObject(String.valueOf(auditData.get("rep")), BaseInfo.class);
					String memo = (String)auditData.get("memo");
			        
					// 页面获取到的退款金额
					TradeRefundRequset tradeRefundRequset = new TradeRefundRequset();
					tradeRefundRequset.setRefundAmount(audit.getAmount());
					tradeRefundRequset.setTradeSourceVoucherNo(audit.getOrigTranVoucherNo());
					tradeRefundRequset.setTradeVoucherNo(audit.getTranVoucherNo());
					tradeRefundRequset.setRemarks(audit.getRemark());
					tradeRefundRequset.setAccessChannel(AccessChannel.WEB.getCode());
					// 调用退款接口
					try{
						RefundResponse refundResponse = tradeService.tradeRefund(
								baseInfo, tradeRefundRequset, env);
						if(!refundResponse.isSuccess()){
							fail.add(audit);
						}
					}catch(Exception e){
						logger.error("调用退款接口失败",e);
						fail.add(audit);
					}
				}else{
					try {
						defaultTransferService.batchTransferPay(audit.getTranVoucherNo());
					}catch(Exception e){
						logger.error("批量退款{}的操作失败", e);
					}
				}
				
				
				audit.setId(batchIds[i]);
				audit.setStatus("2");// 2.已审核
				audit.setRemark(remark);
				audit.setAuditorId(user.getOperatorId());
				audit.setAuditorName(user.getOperator_login_name());
				audit.setGmtModified(new Date());
				boolean isUpdate = auditService.updAudit(audit);
				auditLogService.addAuditLog(log);

				if (!isUpdate) {
					fail.add(audit);
				}
			}
			if (AuditType.AUDIT_WITHDRAW.getCode().equals(audit.getAuditType())) {
                logger.info(LogUtil.generateMsg(OperateTypeEnum.WITHDRAW_EXAMINE, user, env,
                        "reject".equals(operate) ? "拒绝" : "通过"));
			    
				log.setAuditId(batchIds[i]);
				log.setAuditorId(user.getOperatorId());
				log.setAuditorName(user.getOperator_login_name());
				log.setOperateType(operate);
				log.setAuditRemark(remark);
				log.setGmtCreated(new Date());

				Map<String, Object> auditData = JSONObject.parseObject(audit
						.getAuditData());
				Map<String, Object> requestMap = (Map<String, Object>) auditData
						.get("tradeReqest");
				Map<String, Object> payerMap = (Map<String, Object>) requestMap
						.get("payer");
				amount = audit.getAmount();
				
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
				
				if(audit.getFee() == null){
					audit.setFee(new Money("0.00"));
				}
				
				fee = audit.getFee();
				allAmount = audit.getAmount().add(fee);
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
		    				((String) auditData.get("bankCardId")), tradeEnv);
		        	if (!flag) {
		        		logger.error("无效的银行卡账户名");
		        		fail.add(audit);
		            }
				} catch (Exception e) {
					logger.error("调用提现接口异常", e);
					fail.add(audit);
				}
				
				// 更新审核状态
				audit.setId(batchIds[i]);
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
					fail.add(audit);
				}

				fail.add(audit);
			}
			if (AuditType.AUDIT_TRANSFER_KJT.getCode().equals(audit.getAuditType()) || AuditType.AUDIT_TRANSFER.getCode().equals(audit.getAuditType())) {
                logger.info(LogUtil.generateMsg(OperateTypeEnum.TRANSFER_EXAMINE, user, env,
                        "reject".equals(operate) ? "拒绝" : "通过"));
			    
				log.setAuditId(batchIds[i]);
				log.setAuditorId(user.getOperatorId());
				log.setAuditorName(user.getOperator_login_name());
				log.setOperateType(operate);
				log.setAuditRemark(remark);
				log.setGmtCreated(new Date());
				amount = audit.getAmount();
				
				if(AuditSubType.SINGLE.getCode().equals(subType)){
				    fee = audit.getFee();
	                if(fee==null){
	                    fee = new Money("0.00");
	                }
	                allAmount = audit.getAmount().add(fee);
	                try{
	                    // 资金解冻
	                    if (!fundsControlService.unfreeze(audit.getExt(), allAmount, env)) {
	                        logger.error("资金解冻失败");
	                    }
	                }catch(Exception ignore) {
	                    logger.error("资金解冻出现异常", ignore);
	                }
				    
					// 验证余额
					Map<String, Object> auditData = JSONObject
							.parseObject(audit.getAuditData());
					Map<String, Object> requestMap = (Map<String, Object>) auditData
							.get("tradeReqest");
					String transferInfo = auditData.get("transferInfo") == null ? ""
							: auditData.get("transferInfo").toString();
					Map<String, Object> payerMap = (Map<String, Object>) requestMap
							.get("payer");
					Map<String, Object> payeeMap = (Map<String, Object>) requestMap
							.get("payee");

					// 付款人信息
					String payerAccountId = (String) payerMap.get("accountId");
					String payerMemberId = (String) payerMap.get("memberId");
					String payerOperatorId = (String) payerMap
							.get("operatorId");
					String payerMobile = (String) payerMap.get("mobile");
					String payerMemberName = (String) payerMap
							.get("memberName");
					String payerMemberType = (String) payerMap
							.get("memberType");

					// 收款人的邮箱或手机
					String identityNo = (String) payeeMap.get("identityNo");
					// 查询收款人
					BaseMember payeeUser = null;
					try {
						payeeUser = defaultMemberService.isMemberExists(
								identityNo, "1", env);
					} catch (ServiceException e) {
						logger.error("调用服务异常");
						fail.add(audit);
					} catch (MemberNotExistException e) {
						logger.error("网关异常");
						fail.add(audit);
					}

					// 生成付款方
					logger.info("转账,生成付款方");
					PartyInfo payer = TradeReqestUtil.createPay(payerAccountId,
							payerMemberId, payerOperatorId, payerMobile,
							payerMemberName,
							MemberType.valueOf(payerMemberType));

					// 生成收款方
					logger.info("转账,生成收款方");
					PartyInfo payee = TradeReqestUtil.createPay(payeeUser.getDefaultAccountId(),payeeUser.getMemberId(), payeeUser.getOperatorId(),"", payeeUser.getMemberName(),payeeUser.getMemberType());
					payee.setIdentityNo(identityNo);
					boolean sendMessage = (Boolean) requestMap.get("sendMessage");
					String msn = "N";
					if (sendMessage) {
						msn = "Y";
					}
					// 生成交易请求
					TradeRequestInfo info = TradeReqestUtil.createTransferTradeRequest(payer, payee, amount,transferInfo, new Money(), msn);
					
					/* 录入员环境信息 */
					info.setClientIp((String) requestMap.get("clientIp"));
					info.setClientId((String) requestMap.get("clientId"));
					info.setBrowser((String) requestMap.get("browser"));
					info.setCookie((String) requestMap.get("cookie"));
					info.setClientMac((String) requestMap.get("clientMac"));
					info.setDomainName((String) requestMap.get("domainName"));
					info.setReferUrl((String) requestMap.get("referUrl"));
					info.setTradeType(TradeType.valueOf((String) requestMap.get("tradeType")));
					info.setTradeVoucherNo(audit.getTranVoucherNo());
					info.setTradeSourceVoucherNo((String)requestMap.get("tradeSourceVoucherNo"));
					
					PaymentResponse response = null;
					try {
						defaultTransferService.etransfer(info, TradeType.TRANSFER);
						response = defaultTransferService.pay(info);
						
						if (response.isSuccess()) {
							log.setProcessResult("success");
						} else {
							log.setProcessResult("failure");
						}
						audit.setPayVoucherNo(info.getPaymentVoucherNo());
						logger.info("response:{}", response);
						auditLogService.addAuditLog(log);
					} catch (BizException biz) {
						logger.error("调用外部接口异常", biz.getMessage());
						fail.add(audit);
					}
				}else{
					try {
						defaultTransferService.batchTransferPay(audit.getTranVoucherNo());
					}catch(Exception e){
						logger.error("批量转账到账户{}的操作失败", e);
					}
				}
				// 更新审核状态
				// auditTrans.setId(auditTrans.getId());
				audit.setStatus("2");// 2.已审核
				audit.setRemark(remark);
				audit.setAuditorId(user.getOperatorId());
				audit.setAuditorName(user.getOperator_login_name());
				// auditTrans.setTranVoucherNo(info.getTradeVoucherNo());
				
				audit.setGmtModified(new Date());
				boolean isUpdate = auditService.updAudit(audit);
				if (!isUpdate) {
					logger.error("更新审核表状态失败");
					fail.add(audit);
				}
			}
			if (AuditType.AUDIT_TRANSFER_BANK.getCode().equals(audit.getAuditType()) || AuditType.AUDIT_PAY_TO_CARD.getCode().equals(audit.getAuditType())) {
                logger.info(LogUtil.generateMsg(OperateTypeEnum.TRANSFER_EXAMINE, user, env,
                        "reject".equals(operate) ? "拒绝" : "通过"));
				// 记录审核日志
				log.setAuditId(audit.getId());
				log.setAuditorId(user.getOperatorId());
				log.setAuditorName(user.getOperator_login_name());
				log.setOperateType(operate);
				log.setAuditRemark(remark);
				log.setGmtCreated(new Date());

				fee = audit.getFee();
				if(fee==null){
					fee = new Money("0.00");
				}
				
				if(AuditSubType.SINGLE.getCode().equals(subType)){
				    allAmount = audit.getAmount().add(fee);
	                try{
	                    // 资金解冻
	                    if (!fundsControlService.unfreeze(audit.getExt(), allAmount, env)) {
	                        logger.error("资金解冻失败");
	                    }
	                }catch(Exception ignore) {
	                    logger.error("资金解冻出现异常", ignore);
	                }
				    
					Map<String, Object> auditData = JSONObject.parseObject(audit
							.getAuditData());
					Map<String, Object> requestMap = (Map<String, Object>) auditData
							.get("tradeReqest");
					Map<String, Object> payerMap = (Map<String, Object>) requestMap
							.get("payer");
					Map<String, Object> payeeBankAcctInfo = (Map<String, Object>)auditData
		                    .get("payeeBankAcctInfo");
					
					TradeRequestInfo info = new TradeRequestInfo();
					// 金额
					info.setAmount(audit.getAmount());
					
					/* 付款方 */
					PartyInfo payer = new PartyInfo();
					payer.setAccountId((String) payerMap.get("accountId"));
					payer.setMemberId((String) payerMap.get("memberId"));
					payer.setMemberType(MemberType.valueOf((String) payerMap.get("memberType")));
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
					info.setTradeSourceVoucherNo((String) requestMap.get("tradeSourceVoucherNo"));
					info.setTradeType(TradeType.valueOf((String) requestMap
							.get("tradeType")));
					info.setTradeVoucherNo((String) requestMap.get("tradeVoucherNo"));
					
					// 从审核表中获取到转账信息
					BankAcctDetailInfo bankAcctInfo = convertToAcctDetailInfo(audit);
					bankAcctInfo.setCardAttribute((Integer) payeeBankAcctInfo.get("cardAttribute"));
					try {
						transferToBankAccount(request, session, audit.getAmount(), bankAcctInfo, info);
					} catch (Exception e) {
						logger.error("转账失败！", e);
						fail.add(audit);
					}
					audit.setPayVoucherNo(info.getPaymentVoucherNo());
				}else{
					try {
						defaultTransferService.batchTransferPay(audit.getTranVoucherNo());
					}catch(Exception e){
						logger.error("批量转账到账户{}的操作失败", e);
					}
				}
//				 更新审核状态
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
					fail.add(audit);
				}
			}
			if (AuditType.AUDIT_PAYOFF_BANK.getCode().equals(audit.getAuditType())) {
                logger.info(LogUtil.generateMsg(OperateTypeEnum.GONGZI_EXAMINE, user, env,
                        "reject".equals(operate) ? "拒绝" : "通过"));
			    
				log.setAuditId(audit.getId());
				log.setAuditorId(user.getOperatorId());
				log.setAuditorName(user.getOperator_login_name());
				log.setOperateType(operate);
				log.setAuditRemark(remark);
				log.setGmtCreated(new Date());

				if(AuditSubType.SINGLE.getCode().equals(subType)){
				    fee = audit.getFee();
	                if(fee==null){
	                    fee = new Money("0.00");
	                }
	                allAmount = audit.getAmount().add(fee);
	                try{
	                    // 资金解冻
	                    if (!fundsControlService.unfreeze(audit.getExt(), allAmount, env)) {
	                        logger.error("资金解冻失败");
	                    }
	                }catch(Exception ignore) {
	                    logger.error("资金解冻出现异常", ignore);
	                }
				    
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
				
//				 更新审核状态
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
					fail.add(audit);
				}
			}
			if (AuditType.AUDIT_PAYOFF_KJT.getCode().equals(audit.getAuditType())){
                logger.info(LogUtil.generateMsg(OperateTypeEnum.GONGZI_EXAMINE, user, env,
                        "reject".equals(operate) ? "拒绝" : "通过"));
				log.setAuditId(batchIds[i]);
				log.setAuditorId(user.getOperatorId());
				log.setAuditorName(user.getOperator_login_name());
				log.setOperateType(operate);
				log.setAuditRemark(remark);
				log.setGmtCreated(new Date());
				
				if (AuditSubType.SINGLE.getCode().equals(subType)) {// 批量查看详情
				    amount = audit.getAmount();
	                fee = audit.getFee();
	                if(fee==null){
	                    fee = new Money("0.00");
	                }
	                allAmount = audit.getAmount().add(fee);
	                try{
	                    // 资金解冻
	                    if (!fundsControlService.unfreeze(audit.getExt(), allAmount, env)) {
	                        logger.error("资金解冻失败");
	                    }
	                }catch(Exception ignore) {
	                    logger.error("资金解冻出现异常", ignore);
	                }
				    
					Map<String, Object> auditData = JSONObject
							.parseObject(audit.getAuditData());
					Map<String, Object> requestMap = (Map<String, Object>) auditData
							.get("tradeReqest");
					String transferInfo = auditData.get("transferInfo") == null ? ""
							: auditData.get("transferInfo").toString();
					Map<String, Object> payerMap = (Map<String, Object>) requestMap
							.get("payer");
					Map<String, Object> payeeMap = (Map<String, Object>) requestMap
							.get("payee");

					// 付款人信息
					String payerAccountId = (String) payerMap.get("accountId");
					String payerMemberId = (String) payerMap.get("memberId");
					String payerOperatorId = (String) payerMap
							.get("operatorId");
					String payerMobile = (String) payerMap.get("mobile");
					String payerMemberName = (String) payerMap
							.get("memberName");
					String payerMemberType = (String) payerMap
							.get("memberType");

					// 收款人的邮箱或手机
					String identityNo = (String) payeeMap.get("identityNo");
					// 查询收款人
					BaseMember payeeUser = null;
					try {
						payeeUser = defaultMemberService.isMemberExists(
								identityNo, "1", env);
					} catch (ServiceException e) {
						restP.setMessage("调用服务异常");
						fail.add(audit);
					} catch (MemberNotExistException e) {
						restP.setMessage("网关异常");
						fail.add(audit);
					}

					// 生成付款方
					logger.info("转账,生成付款方");
					PartyInfo payer = TradeReqestUtil.createPay(payerAccountId,
							payerMemberId, payerOperatorId, payerMobile,
							payerMemberName,
							MemberType.valueOf(payerMemberType));

					// 生成收款方
					logger.info("转账,生成收款方");
					PartyInfo payee = TradeReqestUtil.createPay(payeeUser.getDefaultAccountId(),payeeUser.getMemberId(), payeeUser.getOperatorId(),"", payeeUser.getMemberName(),payeeUser.getMemberType());
					payee.setIdentityNo(identityNo);
					boolean sendMessage = (Boolean) requestMap.get("sendMessage");
					String msn = "N";
					if (sendMessage) {
						msn = "Y";
					}
					// 生成交易请求
					TradeRequestInfo info = TradeReqestUtil.createTransferTradeRequest(payer, payee, amount,transferInfo, new Money(), msn);

					/* 录入员环境信息 */
					info.setClientIp((String) requestMap.get("clientIp"));
					info.setClientId((String) requestMap.get("clientId"));
					info.setBrowser((String) requestMap.get("browser"));
					info.setCookie((String) requestMap.get("cookie"));
					info.setClientMac((String) requestMap.get("clientMac"));
					info.setDomainName((String) requestMap.get("domainName"));
					info.setReferUrl((String) requestMap.get("referUrl"));
					info.setTradeType(TradeType.valueOf((String) requestMap.get("tradeType")));
					info.setTradeSourceVoucherNo((String)requestMap.get("tradeSourceVoucherNo"));
					PaymentResponse response = null;
					try {
						info.setTradeVoucherNo(audit.getTranVoucherNo());
						defaultTransferService.etransfer(info, TradeType.TRANSFER);
						response = defaultTransferService.pay(info);
						// 更新审核状态
						// auditTrans.setId(auditTrans.getId());
						logger.info("response:{}", response);
						audit.setPayVoucherNo(info.getPaymentVoucherNo());
					} catch (BizException biz) {
						logger.error("调用外部接口异常", biz.getMessage());
						fail.add(audit);
					}
					
					if (response.isSuccess()) {
						log.setProcessResult("success");
					} else {
						log.setProcessResult("failure");
					}
				}else{
					try {
						defaultTransferService.batchTransferPay(audit.getTranVoucherNo());
					}catch(Exception e){
						logger.error("批量转账到账户{}的操作失败", e);
					}
				}
				
				auditLogService.addAuditLog(log);
				audit.setStatus("2");// 2.已审核
				audit.setRemark(remark);
				audit.setAuditorId(user.getOperatorId());
				audit.setAuditorName(user.getOperator_login_name());
				// auditTrans.setTranVoucherNo(info.getTradeVoucherNo());
				
				audit.setGmtModified(new Date());
				boolean isUpdate = auditService.updAudit(audit);
				if (!isUpdate) {
					logger.error("更新审核表状态失败");
					fail.add(audit);
				}
				
			}
		}
		
//		if(fail.size()>0){
//			restP.setMessageObj(fail);
//			restP.setSuccess(false);
//			return restP;
//		}
		restP.setSuccess(true);
		return restP;
	}
	
	/**
	 * 转账银行账户
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
