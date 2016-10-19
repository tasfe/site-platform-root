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

import com.meidusa.fastjson.JSONArray;
import com.meidusa.fastjson.JSONObject;
import com.netfinworks.batchservice.facade.model.query.RefundResultDetail;
import com.netfinworks.batchservice.facade.model.query.ResultDetail;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.deposit.api.domain.PageInfo;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.audit.Audit;
import com.netfinworks.site.domain.domain.audit.AuditLog;
import com.netfinworks.site.domain.domain.info.BaseInfo;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.domain.trade.TradeRefundRequset;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.AccessChannel;
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
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.common.constant.AuditSubType;
import com.netfinworks.tradeservice.facade.enums.TradeType;
import com.netfinworks.tradeservice.facade.response.RefundResponse;

/**
 * <p>
 * 退款审核
 * </p>
 * 
 * @author yangshihong
 * @version $Id: AuditAction.java, v 0.1 2014年5月21日 下午2:21:49 hdfs Exp $
 */
@Controller
public class RefundAuditAction extends BaseAction {
	protected Logger                log = LoggerFactory.getLogger(getClass());
	@Resource(name="accountService")
    private AccountService accountService;
	
	@Resource(name = "defaultWithdrawService")
	private DefaultWithdrawService defaultWithdrawService;
	
	@Resource(name = "defaultMemberService")
    private DefaultMemberService       defaultMemberService;
	
	@Resource(name = "tradeService")
	private TradeService tradeService;
	
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
	
	
	
	/**
	 * 退款审核(点击"查看详情")
	 * 
	 * @param model
	 * @param request
	 * @return
	 * @throws ServiceException
	 */
	@RequestMapping(value = "/my/refundAudit.htm", method = { RequestMethod.POST, RequestMethod.GET })
	public ModelAndView refundAudit(HttpServletRequest request,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Map<String, Object> data = initOcx();
		String id = request.getParameter("id");
		// 因为还不支持按订单号/批次号查询 以下为测试数据
		Audit audit = auditService.queryAudit(id);// 待审核数据
		
		data.put("id", audit.getId());
		data.put("transId", audit.getTranVoucherNo());// 设置订单号
		data.put("createTime",sf.format(audit.getGmtCreated()));//申请时间
		data.put("operatorName", audit.getOperatorName());// 申请操作员名称
		data.put("auditorName", audit.getAuditorName());// 审核员名称
		data.put("tranSourceVoucherNo", audit.getTranSourceVoucherNo());//商户订单号/商户批次号
		if(audit.getGmtModified() != null){
			data.put("auditTime", sf.format(audit.getGmtModified()));// 审核时间
		}
		
		data.put("remark", audit.getRemark());//备注
		data.put("status", audit.getStatus());//审核状态
		
		String bizProductCode = "";
		String subType = audit.getAuditSubType();
		if(subType==null){
			subType = AuditSubType.SINGLE.getCode();//如果为空 默认为单笔
		}
		if (AuditSubType.BATCH.getCode().equals(subType)) {// 批量查看详情
			BatchQuery req = new BatchQuery();
			String batchNo = audit.getTranVoucherNo();// 批次号
			
			String currentPage = request.getParameter("currentPage");
			if (StringUtils.isBlank(currentPage) || currentPage.equals("0")) {
	            currentPage = "1";
	        }
			
			PageInfo pageInfo = new PageInfo();
			pageInfo.setPageSize(20);
			pageInfo.setCurrentPage(Integer.parseInt(currentPage));
			
			req.setBatchNo(batchNo);
			req.setPageNum(Integer.parseInt(currentPage));
			req.setPageSize(20);
			
			List<Map<String,String>> refundResultList = null;
			
			//获得批次明细
			BatchQueryResponse queryResponse = queryDetailService.batchQueryDetail(req);
			
			if (queryResponse.getResultCode().equals("S")) {// 成功
				data.put("resultMessage", queryResponse.getResultMessage());
				data.put("totalSize", queryResponse.getTotalSize());
				
				PageResultList resultList = new PageResultList();
				pageInfo.setTotalItem(queryResponse.getTotalSize());
				resultList.setPageInfo(pageInfo);// 分页信息
				
				List<ResultDetail> list = queryResponse.getResultDetails();
				if ((list != null) && (list.size() > 0)) {
					refundResultList = new ArrayList<Map<String,String>>();
					for(int i=0;i<list.size();i++){
						RefundResultDetail refund = (RefundResultDetail)list.get(i);						
						Map<String,String> detail = new HashMap<String,String>();
						detail.put("tradeVoucherNo", refund.getTradeVoucherNo());
						detail.put("sourceDetailNo", refund.getSourceDetailNo());
						detail.put("origOutDetailNo", refund.getOrigOutDetailNo());
						detail.put("origTradeVoucherNo", refund.getOrigTradeVoucherNo());
						detail.put("amount", refund.getAmount().toString());
						Map<String, Object> fee = JSONObject.parseObject(refund.getExtension());
						if ((fee != null) && (fee.get("fee") != null)) {
							detail.put("fee", (String)fee.get("fee"));
						}
						detail.put("memo", refund.getMemo());
						detail.put("status", refund.getStatus());
						
						refundResultList.add(detail);
					}
					resultList.setInfos(refundResultList);// list
				}
				data.put("page", resultList);
				
				data.put("allAmount", audit.getAmount() != null ? audit.getAmount().getAmount() : "0");//设置转账总金额
				data.put("allFee", audit.getFee() != null ? audit.getFee().getAmount() : "0");//设置转账总金额
				data.put("totalAmount", (audit.getAmount().add(audit.getFee())).getAmount());//设置总金额
			} else {
				// 失败
			}
		}else{
			Map<String, Object> auditData = JSONObject.parseObject(audit
					.getAuditData());
			BaseInfo baseInfo = JSONArray.parseObject(String.valueOf(auditData.get("rep")), BaseInfo.class);
			String refundMemo = (String)auditData.get("memo");
			bizProductCode = baseInfo.getOrderNumber();
			
			data.put("tradeMemo", baseInfo.getTradeMemo());
			data.put("refundMemo", refundMemo);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String gmtCreated = sdf.format(audit.getGmtCreated());//退款发起时间
			data.put("id", audit.getId());
			data.put("transId", audit.getTranVoucherNo());//退款订单号
			data.put("gmtCreated", gmtCreated);//申请时间
			data.put("operatorName", audit.getOperatorName());//申请操作员
			data.put("amount", audit.getAmount().getAmount().toString());//退款金额
			data.put("gmtpaid", baseInfo.getGmtpaid());//支付时间
			
			if(audit.getGmtModified() != null){
				data.put("auditData", sdf.format(audit.getGmtModified()));//审核时间
			}
			data.put("auditorName", audit.getAuditorName());//审核操作员
			Money fee = audit.getFee();
			if(fee == null){
				fee= new Money("0.00");
			}
			data.put("fee", fee.getAmount().toString());//退款手续费
			data.put("remark", audit.getRemark());//退款原因
			data.put("shStatus", audit.getStatus());//审核状态
			
			//下列是原交易订单信息
			data.put("tranSourceVoucherNo", audit.getTranSourceVoucherNo());//退款商户订单号
			data.put("origTranVoucherNo", audit.getOrigTranVoucherNo());//原交易订单号
			if(audit.getExt() != null && !audit.getExt().isEmpty()){
			    Map<String, Object> ext = new HashMap<String,Object>();
			    if(audit.getExt().indexOf(":")!=-1){
				    ext = JSONObject.parseObject(audit.getExt());
                    String serialNumber = (String)ext.get("serialNumber");
                    data.put("ext", serialNumber);//退款原商户订单号先放在ext字段里
			    }else{
			    	data.put("ext", audit.getExt());
			    }
            }
			
			Date tra_orderData = baseInfo.getGmtSubmit();
			data.put("tra_orderData", sdf.format(tra_orderData));//交易创建时间
			
			String type = baseInfo.getTradeType();
			String tra_type = TradeType.valueOf(type).getMessage();
			data.put("tra_type", tra_type);//交易类型
			
			if(baseInfo.getOrderMoney() != null){
				data.put("tra_amount", baseInfo.getOrderMoney().getAmount().toString());//订单交易金额
			}
			
			if(baseInfo.getPayeeFee() != null){
				data.put("tra_fee", baseInfo.getPayeeFee().getAmount().toString());//订单手续费
			}
			
			if(baseInfo.getPayAmount() != null){
				data.put("payAmount", baseInfo.getPayAmount().getAmount().toString());//实际费用
			}
			
			String state = baseInfo.getOrderState();
			String orderState = null;
			if("401".equals(state)){
				orderState = "交易成功";
			}else if(state.equals("201")||state.equals("301")){
				orderState = "支付成功";
			}
			
			data.put("orderState", orderState);//交易订单状态
		}
		
		// 硬证书是否激活
		if (this.isCertActive(request, "hard", env) || this.isCertActive(request, "soft", env)) {
			data.put("isCertActive", "yes");
		} else {
			data.put("isCertActive", "no");
		}
		
		restP.setData(data);
		//pos退款--判断是否通过审核，跳到不同页面
		if (audit.getStatus().equals("1") && audit.getAuditSubType().equals(AuditSubType.SINGLE.getCode())
				&&bizProductCode.equals("20205")) {// 跳转到查看详情页面（底部带审核）
			return new ModelAndView(CommonConstant.URL_PREFIX + "/refund/pos_refund_AuditDetailExecute","response", restP);
		}else if(!audit.getStatus().equals("1") && audit.getAuditSubType().equals(AuditSubType.SINGLE.getCode())
				&&bizProductCode.equals("20205")){
			return new ModelAndView(CommonConstant.URL_PREFIX + "/refund/pos_refund_AuditDetail","response", restP);
		}
		
		// 判断是否通过审核，跳到不同页面
		if (audit.getStatus().equals("1") && audit.getAuditSubType().equals(AuditSubType.SINGLE.getCode())) {// 跳转到查看详情页面（底部带审核）
			return new ModelAndView(CommonConstant.URL_PREFIX + "/refund/refund_AuditDetailExecute","response", restP);
		}else if(audit.getStatus().equals("1") && audit.getAuditSubType().equals(AuditSubType.BATCH.getCode())){
			return new ModelAndView(CommonConstant.URL_PREFIX + "/refund/refund_BatchDetailExecute","response", restP);
		}else if(audit.getAuditSubType().equals(AuditSubType.BATCH.getCode()) && !audit.getStatus().equals("1")){
			return new ModelAndView(CommonConstant.URL_PREFIX + "/refund/refund_BatchDetail","response", restP);
		} else {// 跳转到查看详情页面（底部带审核）
			return new ModelAndView(CommonConstant.URL_PREFIX + "/refund/refund_AuditDetail","response", restP);
		}
	}

	/**
	 * 退款审核确认页
	 * 
	 * @param model
	 * @param request
	 * @return
	 * @throws ServiceException
	 */
	@RequestMapping(value = "/my/refundAuditConfirm.htm", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody RestResponse refundAuditConfirm(HttpServletRequest request,HttpSession session,
			HttpServletResponse resp,TradeEnvironment env){
		RestResponse restP = new RestResponse();
		EnterpriseMember user = getUser(request);
		Map<String, Object> data = new HashMap<String, Object>();
        Map<String, String> errorMap = new HashMap<String, String>();
        checkUser(user, errorMap, restP);
        
		String id = request.getParameter("id");
		String operate = request.getParameter("operate");// 审核状态：通过 OR 拒绝
		String remark = request.getParameter("remark");
		
		// 查询待审核信息
		Audit audit = auditService.queryAudit(id);
		
		
		TradeRequestInfo req = RefundConvertor
				.convertRefundApply(user, env);

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
                if (AuditSubType.SINGLE.getCode().equals(audit.getAuditSubType())) {
                    // 资金解冻
                    
                    String freezeNo = "";
                    if(audit.getExt() != null && !audit.getExt().isEmpty()){
                        Map<String, Object> ext = JSONObject.parseObject(audit.getExt());
                        freezeNo = (String)ext.get("bizPaymentSeqNo");
                    }
                    
                    if (!fundsControlService.unfreeze(freezeNo, allAmount, env)) {
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
		
		if (AuditSubType.SINGLE.getCode().equals(audit.getAuditSubType())) {
    		try{
    		    
    		    String freezeNo = "";
                if(audit.getExt() != null && !audit.getExt().isEmpty()){
                    Map<String, Object> ext = JSONObject.parseObject(audit.getExt());
                    freezeNo = (String)ext.get("bizPaymentSeqNo");
                }
    			// 资金解冻
    			if (!fundsControlService.unfreeze(freezeNo, allAmount, env)) {
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
			BaseInfo baseInfo = JSONArray.parseObject(String.valueOf(auditData.get("rep")), BaseInfo.class);
	        String memo = (String)auditData.get("memo");
			
			// 页面获取到的退款金额
			TradeRefundRequset tradeRefundRequset = new TradeRefundRequset();
			tradeRefundRequset.setRefundAmount(audit.getAmount());
			tradeRefundRequset.setTradeSourceVoucherNo(audit.getOrigTranVoucherNo());
			tradeRefundRequset.setTradeVoucherNo(audit.getTranVoucherNo());
			tradeRefundRequset.setRemarks(memo);
			tradeRefundRequset.setAccessChannel(AccessChannel.WEB.getCode());
			
			// 调用退款接口
			try{
				RefundResponse refundResponse = tradeService.tradeRefund(
						baseInfo, tradeRefundRequset, env);
				if(!refundResponse.isSuccess()){
					logger.error("退款失败");
				}
			}catch(Exception e){
				logger.error("调用退款接口失败",e);
			}
		}else{
			try {
				defaultTransferService.batchTransferPay(audit.getTranVoucherNo());
			}catch(Exception e){
				logger.error("批量退款{}的操作失败", e);
			}
		}
		
		audit.setId(id);
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
	
}
