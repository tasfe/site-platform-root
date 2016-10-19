package com.netfinworks.site.web.action.audit;

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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.meidusa.fastjson.JSONArray;
import com.meidusa.fastjson.JSONObject;
import com.netfinworks.batchservice.facade.model.query.PayResultDetail;
import com.netfinworks.batchservice.facade.model.query.RefundResultDetail;
import com.netfinworks.batchservice.facade.model.query.ResultDetail;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.deposit.api.domain.PageInfo;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.audit.Audit;
import com.netfinworks.site.domain.domain.info.BaseInfo;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domainservice.account.DefaultAccountService;
import com.netfinworks.site.domainservice.audit.AuditLogServiceImpl;
import com.netfinworks.site.domainservice.audit.AuditServiceImpl;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.trade.DefaultWithdrawService;
import com.netfinworks.site.domainservice.trade.impl.DefaultTransferServiceImpl;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
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
import com.netfinworks.tradeservice.facade.enums.TradeType;

/**
 * <p>
 * 申请记录
 * </p>
 * 
 * @author yangshihong
 * @version $Id: AuditAction.java, v 0.1 2014年5月21日 下午2:21:49 hdfs Exp $
 */
@Controller
public class ApplicationRecordAction extends BaseAction {
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
	
	@Resource(name = "uesService")
	private UesServiceClient uesServiceClient;
	
	SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	/**
	 * 应用申请(点击"查看详情")代发工资到永达互联网金融账户
	 * 
	 * @param model
	 * @param request
	 * @return
	 * @throws ServiceException
	 */
	@RequestMapping(value = "/my/appAudit_daifa1.htm", method = {
        RequestMethod.POST, RequestMethod.GET })
	public ModelAndView appAudit_daifa(HttpServletRequest request,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		
		//用于显示页面附加字段
		Map<String, Object> data = new HashMap<String, Object>();
		
		//查询审核记录
		String id = request.getParameter("id");
		Audit audit = auditService.queryAudit(id);
		data.put("id", audit.getId());
		data.put("transId", audit.getTranVoucherNo());// 设置订单号
		data.put("createTime",sf.format(audit.getGmtCreated()));
		data.put("operatorName", audit.getOperatorName());// 申请操作员名称

		if(audit.getGmtModified() != null){
			data.put("auditTime", sf.format(audit.getGmtModified()));// 审核时间
			data.put("auditorName", audit.getAuditorName());// 审核员名称
		}else{
			data.put("auditTime",sf.format(audit.getGmtCreated()));//更新时间
			data.put("auditorName", audit.getOperatorName());// 申请操作员名称
		}
		
		data.put("remark", audit.getRemark());//备注
		data.put("status", audit.getStatus());//审核状态
		String subType = audit.getAuditSubType();//单笔/批量区分
		if(subType==null){
			subType = AuditSubType.SINGLE.getCode();//如果为空 默认为单笔
		}
		if (AuditSubType.BATCH.getCode().equals(subType)) {// 批量查看详情
			BatchQuery req = new BatchQuery();
			String batchNo = audit.getTranVoucherNo();// 批次号
			
			String currentPage = request.getParameter("currentPage");
			if (StringUtils.isBlank(currentPage)|| currentPage.equals("0")) {
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
			String transferInfo = (String) auditData
					.get("transferInfo");
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
			data.put("memo", transferInfo);//设置支付理由
			data.put("remark", audit.getRemark());// 设置备注信息
			data.put("status", audit.getStatus());// 审核结果
			
			String[] arr = null;
			String payeeNo = null;
			String payeeAccountName = null;
			if(audit.getPayeeNo()!= null) {
				arr = audit.getPayeeNo().split("\\|");
				if((arr != null)  && (arr.length > 1)){
					payeeNo = arr[0];
					payeeAccountName = arr[1];
				}else{
					payeeNo = arr[0];
				}
			}
			if(payeeNo.indexOf("@")!=-1){
				data.put("payeeNo", payeeNo);// 设置收款账号
			}else{
				data.put("payeeNo", CommonUtils.getMaskData(payeeNo, 3, 7));// 设置收款账号
			}
			data.put("payeeAccountName",payeeAccountName );// 设置收款账户名称
			data.put("payeeBankInfo", audit.getPayeeBankInfo());//设置收款支行信息
		}
		restP.setData(data);
		restP.setSuccess(true);
		if (AuditSubType.BATCH.getCode().equals(subType)) {// 底部不带审核(批量)
			return new ModelAndView(CommonConstant.URL_PREFIX
					+ "/apply/payOffKjt_BatchDetail", "response", restP);
		}else{
			return new ModelAndView(CommonConstant.URL_PREFIX
					+ "/application/payOffKjt_AuditDetail", "response", restP);
		}
	}

	/**
	 * 代发工资 转账到银行卡 (申请详情)
	 * 
	 * @param model
	 * @param request
	 * @return
	 * @throws ServiceException
	 */
	@RequestMapping(value = "/my/payoffBankAudit1.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView payoffBankAudit(HttpServletRequest request,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		
		//用于显示页面附加字段
		Map<String, Object> data = new HashMap<String, Object>();
		
		//查询审核记录
		String id = request.getParameter("id");
		Audit audit = auditService.queryAudit(id);
		
		data.put("id", audit.getId());
		data.put("transId", audit.getTranVoucherNo());// 订单号
		data.put("createTime",sf.format(audit.getGmtCreated()));//申请时间
		data.put("operatorName", audit.getOperatorName());// 申请操作员名称

		if(audit.getGmtModified() != null){
			data.put("auditTime", sf.format(audit.getGmtModified()));// 审核时间
			data.put("auditorName", audit.getAuditorName());// 审核员名称
		}else{
			data.put("auditTime",sf.format(audit.getGmtCreated()));//更新时间
			data.put("auditorName", audit.getOperatorName());// 申请操作员名称
		}
		
		data.put("remark", audit.getRemark());//备注
		data.put("status", audit.getStatus());//审核状态
		
		String subType = audit.getAuditSubType();//单笔/批量区分
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
			
			data.put("remark", audit.getRemark());// 设置备注信息
			data.put("status", audit.getStatus());// 审核结果
			
			String[] arr = null;
			String payeeNo = null;
			String payeeAccountName = null;
			if(audit.getPayeeNo()!= null) {
				arr = audit.getPayeeNo().split("\\|");
				if((arr != null)  && (arr.length > 1)){
					payeeNo = arr[0];
					payeeAccountName = arr[1];
					if(AuditType.AUDIT_PAYOFF_BANK.getCode().equals(audit.getAuditType())){
						payeeNo = CommonUtils.getMaskData(uesServiceClient.getDataByTicket(payeeNo));
					}
				}else{
					payeeNo = arr[0];
				}
			}
			data.put("payeeNo", payeeNo);// 设置收款账号
			data.put("memo", tradeReqestMap.get("memo"));//设置支付理由
			data.put("payeeAccountName",payeeAccountName );// 设置收款账户名称
			data.put("payeeBankInfo", audit.getPayeeBankInfo());//设置收款支行信息
		}
		restP.setData(data);
		restP.setSuccess(true);
		
		// 当前操作员审核权限
//		boolean isDraw = auth(request, FunctionType.EW_MY_APPROVE.getCode());
		if (AuditSubType.BATCH.getCode().equals(subType)) {// 底部不带审核(批量)
			return new ModelAndView(CommonConstant.URL_PREFIX
					+ "/apply/payOffBank_BatchDetail", "response", restP);
		}else{
			return new ModelAndView(CommonConstant.URL_PREFIX
					+ "/application/payOffBank_AuditDetail", "response", restP);
		}
	}
	
	/**
	 * 应用审核(点击"查看详情")代付订单
	 * 
	 * @param model
	 * @param request
	 * @return
	 * @throws ServiceException
	 */
	@RequestMapping(value = "/my/appAudit_daifu1.htm", method = { RequestMethod.GET })
	public ModelAndView appAudit_daifu(HttpServletRequest request,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		String id = request.getParameter("id");
		Audit audit = auditService.queryAudit(id);
		data.put("id", audit.getId());
		data.put("transId", audit.getTranVoucherNo());// 设置订单号
		data.put("createTime",sf.format(audit.getGmtCreated()));
		data.put("operatorName", audit.getOperatorName());// 申请操作员名称
		data.put("auditorName", audit.getAuditorName());// 审核员名称
		if(audit.getGmtModified() != null){
			data.put("auditTime", sf.format(audit.getGmtModified()));// 审核时间
			data.put("auditorName", audit.getAuditorName());// 审核员名称
		}else{
			data.put("auditTime",sf.format(audit.getGmtCreated()));//更新时间
			data.put("auditorName", audit.getOperatorName());// 申请操作员名称
		}
		data.put("remark", audit.getRemark());
		data.put("status", audit.getStatus());
		
		data.put("validateDay", true);//该字段判断是否可以使用手机校验码用于审核  
		
		String subType = audit.getAuditSubType();
		if ("batch".equals(subType)) {// 批量查看详情
			String batchNo = audit.getTranVoucherNo();// 批次号
			BatchQuery req = new BatchQuery();
			
			String currentPage = request.getParameter("currentPage");
			if (StringUtils.isBlank(currentPage)|| currentPage.equals("0")) {
	            currentPage = "1";
	        }
			
			PageInfo pageInfo = new PageInfo();
			pageInfo.setCurrentPage(Integer.parseInt(currentPage));
			
			req.setBatchNo(batchNo);
			req.setPageNum(Integer.parseInt(currentPage));
			req.setPageSize(3);
			
			List<PayResultDetail> payResultList = null;
			List<RefundResultDetail> refundResultList = null;
			BatchQueryResponse queryResponse = queryDetailService.batchQueryDetail(req);
			
			//构造
			queryResponse = new BatchQueryResponse();
			queryResponse.setResultCode("S");
			queryResponse.setTotalSize(1);
			queryResponse.setResultMessage("success");
			//
			

			if (queryResponse.getResultCode().equals("S")) {// 成功
				data.put("resultMessage", queryResponse.getResultMessage());
				data.put("totalSize", queryResponse.getTotalSize());
				
				PageResultList resultList = new PageResultList();
				pageInfo.setTotalItem(queryResponse.getTotalSize());
				resultList.setPageInfo(pageInfo);// 分页信息
				
				
				List<ResultDetail> list = queryResponse.getResultDetails();
				
				//构造数据
				list = new ArrayList<ResultDetail>();
				ResultDetail de = new PayResultDetail();
				de.setBatchNo("20010101");
				de.setTradeVoucherNo("31321313136556");
				de.setPaymentVoucherNo("5554464546");
				de.setSourceDetailNo("2131221321");
				de.setTradeType("2");
				de.setAmount(new BigDecimal("100.00"));
				de.setPartnerId("1000");
				de.setStatus("1");
				de.setErrorMessage("");
				de.setExtension("没啥扩展信息");
				de.setGmtCreate(new Date());
				de.setMemo("设置备注");
				list.add(de);
				//
				
				
				BigDecimal allAmount = new BigDecimal("0.00");
				
				if (list.size() > 0) {
					String status = list.get(0).getTradeType();
					if(status.equals("3")){//返回的是:"退款审核"数据
						refundResultList = new ArrayList<RefundResultDetail>();
						for(int i=0;i<list.size();i++){
							RefundResultDetail refund = (RefundResultDetail)list.get(i);
							allAmount.add(refund.getAmount());
							refundResultList.add(refund);
						}
						resultList.setInfos(refundResultList);// list
						data.put("page", resultList);
						
					}
					if(!status.equals("3")){//返回的是:"转账审核"数据
						payResultList = new ArrayList<PayResultDetail>();
						for (int i = 0; i < list.size(); i++) {// 目前只能强转了,稍后和维金沟通如何获取两个批量明细的子类//TODO
//							PayResultDetail pay = (PayResultDetail) list.get(i);
//							allAmount.add(pay.getAmount());
//							payResultList.add((PayResultDetail) list.get(i));
							
							
							
							//构造数据
							allAmount = new BigDecimal("100.00");
							PayResultDetail result = (PayResultDetail) list.get(i);
							result.setPayerID("11");
							result.setPayerAccountType("海南药业");
							result.setPayerAccountNo("122@qq.com");
							result.setPayeeLoginId("123");
							result.setPayeePlatformType("中信支付平台");
							result.setPayeeId("2233");
							result.setPayeeAccountNo("155@163.com");
							result.setPayeeBankAccountName("狗不理包子");
							result.setPayeeBankAccountNo("1555489");
							result.setPayeeBankCode("cncb");
							result.setPayeeBankName("中信银行");
							result.setPayeeBranchName("中信银行凤山路分行");
							result.setPayeeBankLineNo("2206");
							result.setPayeeProvince("浙江省");
							result.setPayeeCity("台州大帝国");
							result.setPayeeCompanyOrPersonal("对公");
							payResultList.add(result);
							//
							
						}
						resultList.setInfos(payResultList);// list
						data.put("page", resultList);
					}
				}
				data.put("amount", allAmount);//设置转账总金额
				
				BigDecimal fee = new BigDecimal("0");//由ResultDetail中的fee字段累加得到 fee字段尚未提供
				data.put("fee", fee);
				
				data.put("allAmount", allAmount.add(fee));//设置总金额
			} else {
				// 失败
			}
			// else {
			// // 失败
			// }
		} else {// 单笔查看详情
			Map<String, Object> auditData = JSONObject.parseObject(audit
					.getAuditData());
			Map<String, Object> tradeReqestMap = (Map<String, Object>) auditData
					.get("tradeReqest");
			Map<String, Object> payeeMap = (Map<String, Object>) tradeReqestMap
					.get("payee");
			Map<String, Object> payerMap = (Map<String, Object>) tradeReqestMap
					.get("payer");

			data.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(audit.getGmtCreated()));
			data.put("auditTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(audit.getGmtModified()));
			data.put("auditType", "转账-永达互联网金融账户");// 交易类型
			data.put("amount", audit.getAmount().toString());// 交易金额
			data.put("fee", "200");// 手续费待加入
			data.put("totalAmount", "2222");// 总金额
			data.put("payType", "转账支付");// 支付类型待加入
			data.put("payeraccountName", payerMap.get("accountName"));// 支付账户
			data.put("payData", "2010-10-22 10:22:10");// 支付时间待加入
			boolean isSendMessage = (Boolean) tradeReqestMap.get("sendMessage");
			if (isSendMessage) {// 判断是否短信通知
				data.put("isSend", "是");
			} else {
				data.put("isSend", "否");
			}
			data.put("remark", audit.getRemark());// 设置备注信息
			data.put("status", audit.getStatus());// 审核结果

			data.put("memberName", payeeMap.get("memberName"));// 设置收款账户名称
			data.put("identityNo", payeeMap.get("identityNo"));// 设置收款账号

			data.put("auditorName", audit.getAuditorName());// 审核员名称
		}

		restP.setData(data);
		restP.setSuccess(true);
		if (subType.equals("batch")) {// 底部不带审核(批量)
			return new ModelAndView(CommonConstant.URL_PREFIX
					+ "/application/paidByOtherBatchDetail", "response", restP);
		}
		return new ModelAndView(CommonConstant.URL_PREFIX
				+ "/application/paidByOther", "response", restP);// 底部不带审核(单笔)
	}

	/**
	 * 退款申请 (点击"查看详情")
	 * 
	 * @param model
	 * @param request
	 * @return
	 * @throws ServiceException
	 */
	@RequestMapping(value = "/my/refundAudit1.htm", method = {
        RequestMethod.POST, RequestMethod.GET })
	public ModelAndView refundAudit(HttpServletRequest request,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Map<String, Object> data = new HashMap<String, Object>();
		String id = request.getParameter("id");
		// 因为还不支持按订单号/批次号查询 以下为测试数据
		Audit audit = auditService.queryAudit(id);// 待审核数据
		
		data.put("id", audit.getId());
		data.put("transId", audit.getTranVoucherNo());// 设置订单号
		data.put("createTime",sf.format(audit.getGmtCreated()));//申请时间
		data.put("operatorName", audit.getOperatorName());// 申请操作员名称

		if(audit.getGmtModified() != null){
			data.put("auditTime", sf.format(audit.getGmtModified()));// 审核时间
			data.put("auditorName", audit.getAuditorName());// 审核员名称
		}else{
			data.put("auditTime",sf.format(audit.getGmtCreated()));//更新时间
			data.put("auditorName", audit.getOperatorName());// 申请操作员名称
		}
		
		data.put("remark", audit.getRemark());//备注
		data.put("status", audit.getStatus());//审核状态
		
		String productCode = "";
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
			pageInfo.setPageSize(3);
			pageInfo.setCurrentPage(Integer.parseInt(currentPage));
			
			req.setBatchNo(batchNo);
			req.setPageNum(Integer.parseInt(currentPage));
			req.setPageSize(3);
			
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
				if (list != null && list.size() > 0) {
					refundResultList = new ArrayList<Map<String,String>>();
					for(int i=0;i<list.size();i++){
						RefundResultDetail refund = (RefundResultDetail)list.get(i);
						
						Map<String,String> detail = new HashMap<String,String>();
						detail.put("tradeVoucherNo", refund.getTradeVoucherNo());
						detail.put("sourceDetailNo", refund.getSourceDetailNo());
						detail.put("origOutDetailNo", refund.getOrigOutDetailNo());
						detail.put("amount", refund.getAmount().toString());
						Map<String, Object> fee = JSONObject.parseObject(refund.getExtension());
						if((fee != null) && (fee.get("fee") != null)){
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
			productCode = baseInfo.getOrderNumber();
			String refundMemo = (String)auditData.get("memo");
			data.put("ext", baseInfo.getSerialNumber());//原商户订单号
			data.put("tradeMemo", baseInfo.getTradeMemo());
			data.put("refundMemo", refundMemo);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String gmtCreated = sdf.format(audit.getGmtCreated());//退款发起时间
			data.put("id", audit.getId());
			data.put("transId", audit.getTranVoucherNo());//退款订单号
			data.put("gmtCreated", gmtCreated);//申请时间
			data.put("operatorName", audit.getOperatorName());//申请操作员
			data.put("amount", audit.getAmount().getAmount().toString());//退款金额
			
			
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
			data.put("gmtpaid", baseInfo.getGmtpaid());//支付时间
			//下列是原交易订单信息
			data.put("origTranVoucherNo", baseInfo.getTradeVoucherNo());//交易订单号
			
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
		restP.setData(data);
		
		if (AuditSubType.BATCH.getCode().equals(subType)) {// 底部不带审核(批量)
			return new ModelAndView(CommonConstant.URL_PREFIX + "/apply/refund_BatchDetail","response", restP);
		}else if(productCode.equals("20205")){
			return new ModelAndView(CommonConstant.URL_PREFIX + "/refund/pos_refund_AuditDetail","response", restP);
		}else{
			return new ModelAndView(CommonConstant.URL_PREFIX + "/refund/refund_AuditDetail","response", restP);
		}
	}

	/**
	 * 提现申请(点击"查看详情")
	 * 
	 * @param model
	 * @param request
	 * @return
	 * @throws ServiceException
	 */
	@RequestMapping(value = "/my/cashAudit1.htm", method = {
        RequestMethod.POST, RequestMethod.GET })
	public ModelAndView cashAudit(HttpServletRequest request,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
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
		String createTime = sf.format(audit.getGmtCreated());

		data.put("id", audit.getId());
		
		data.put("validateDay", true);//该字段判断是否可以使用手机校验码用于审核  
		
		data.put("enterpriseName", payerMap.get("enterpriseName"));// 付款企业名称

		if ((payeeBankAcctMap != null) && (payeeBankAcctMap.size() > 0)) {// 收款账户信息(目前只有提现审核才有)
			data.put("bankBranch", payeeBankAcctMap.get("bankBranch"));// 银行名称
			// 收款方信息
			data.put("bankAccount", payeeBankAcctMap.get("bankAccountNumMask"));// 收款账号
			data.put("payeeName", payeeBankAcctMap.get("realName"));// 收款方名称
		}
		// 提现信息
		data.put("transId", audit.getTranVoucherNo());// 交易订单号
		data.put("createTime", createTime);// 创建时间
		data.put("auditType", audit.getAuditType());// 交易类型:提现审核、转账审核、退款审核等等....
		data.put("shStatus", audit.getStatus());//审核状态
		
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
		if(audit.getPayeeNo()!= null) {
			arr = audit.getPayeeNo().split("\\|");
			if((arr != null)  && (arr.length > 1)){
				payeeNo = arr[0];
				payeeAccountName = arr[1];
				if(AuditType.AUDIT_WITHDRAW.getCode().equals(audit.getAuditType())){
					payeeNo = CommonUtils.getMaskData(uesServiceClient.getDataByTicket(payeeNo));
				}
			}else{
				payeeNo = arr[0];
			}
		}
		data.put("payeeNo", payeeNo);// 设置收款账号
		data.put("payeeAccountName",payeeAccountName );// 设置收款账户名称
		data.put("payeeBankInfo", audit.getPayeeBankInfo());//设置收款支行信息
		
		data.put("remark", audit.getRemark());//审核说明
		
		// 申请信息
		data.put("applyTime",sf.format(audit.getGmtCreated()));// 申请时间
		data.put("operatorName", audit.getOperatorName());// 申请操作员
		// 审核信息
		if(audit.getGmtModified() != null){
			data.put("auditTime", sf.format(audit.getGmtModified()));// 审核时间
			data.put("auditorName", audit.getAuditorName());// 审核员名称
		}else{
			data.put("auditTime",sf.format(audit.getGmtCreated()));//更新时间
			data.put("auditorName", audit.getOperatorName());// 申请操作员名称
		}
		
		data.put("auditorName", audit.getAuditorName());// 审核操作员
		data.put("status", audit.getStatus());
		restP.setData(data);
		restP.setSuccess(true);
		String backUrl = CommonConstant.URL_PREFIX;
		backUrl += "/withdrawal/draw_AuditDetail";
		return new ModelAndView(backUrl, "response", restP);
	}

	/**
	 * 转账到永达互联网金融申请(点击"查看详情")
	 * 
	 * @param model
	 * @param request
	 * @return
	 * @throws ServiceException
	 */
	@RequestMapping(value = "/my/transferAudit1.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView transferAudit(HttpServletRequest request,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		String id = request.getParameter("id");
		Audit audit = auditService.queryAudit(id);
		data.put("id", audit.getId());
		data.put("transId", audit.getTranVoucherNo());// 设置订单号
		data.put("createTime",sf.format(audit.getGmtCreated()));
		data.put("operatorName", audit.getOperatorName());// 申请操作员名称

		if(audit.getGmtModified() != null){
			data.put("auditTime", sf.format(audit.getGmtModified()));// 审核时间
			data.put("auditorName", audit.getAuditorName());// 审核员名称
		}else{
			data.put("auditTime",sf.format(audit.getGmtCreated()));//更新时间
			data.put("auditorName", audit.getOperatorName());// 申请操作员名称
		}
		
		data.put("remark", audit.getRemark());//备注
		data.put("status", audit.getStatus());//审核状态
		
		String subType = audit.getAuditSubType();
		if(subType==null){
			subType=AuditSubType.SINGLE.getCode();
		}
		if (AuditSubType.BATCH.getCode().equals(subType)) {// 批量查看详情
			BatchQuery req = new BatchQuery();
			String batchNo = audit.getTranVoucherNo();// 批次号
			
			String currentPage = request.getParameter("currentPage");
			if (StringUtils.isBlank(currentPage)|| currentPage.equals("0")) {
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
			Map<String, Object> payerMap = (Map<String, Object>) tradeReqestMap
					.get("payer");
			Map<String, Object> payeeMap = (Map<String, Object>) tradeReqestMap
					.get("payee");
			data.put("memo", tradeReqestMap.get("memo"));
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
			
			data.put("remark", audit.getRemark());// 设置备注信息
			data.put("status", audit.getStatus());// 审核结果
			
			String[] arr = null;
			String payeeNo = null;
			String payeeAccountName = null;
			if(audit.getPayeeNo()!= null) {
				arr = audit.getPayeeNo().split("\\|");
				if((arr != null)  && (arr.length > 1)){
					payeeNo = arr[0];
					payeeAccountName = arr[1];
				}else{
					payeeNo = arr[0];
				}
			}
			if(payeeNo.indexOf("@")!=-1){
				data.put("payeeNo", payeeNo);// 设置收款账号
			}else{
				data.put("payeeNo", CommonUtils.getMaskData(payeeNo, 3, 7));// 设置收款账号
			}
			data.put("payeeAccountName",payeeAccountName );// 设置收款账户名称
			data.put("payeeBankInfo", audit.getPayeeBankInfo());//设置收款支行信息
		}
		restP.setData(data);
		restP.setSuccess(true);
		
		if(subType.equals(AuditSubType.BATCH.getCode())){
			return new ModelAndView(CommonConstant.URL_PREFIX
					+ "/apply/transferKjt_BatchDetail", "response", restP);
		}
		return new ModelAndView(CommonConstant.URL_PREFIX
				+ "/transfer/transferKjt_AuditDetail", "response", restP);// 底部不带审核(单笔)
	}
	
	/**
	 * 转账到银行 (申请详情)
	 * 
	 * @param model
	 * @param request
	 * @return
	 * @throws ServiceException
	 */
	@RequestMapping(value = "/my/transferBankAudit1.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView bankTransferAudit(HttpServletRequest request,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		
		//用于显示页面附加字段
		Map<String, Object> data = new HashMap<String, Object>();
		
		//查询审核记录
		String id = request.getParameter("id");
		Audit audit = auditService.queryAudit(id);
		
		data.put("id", audit.getId());
		data.put("transId", audit.getTranVoucherNo());// 订单号
		data.put("createTime",sf.format(audit.getGmtCreated()));//申请时间
		data.put("operatorName", audit.getOperatorName());// 申请操作员名称
		
		if(audit.getGmtModified() != null){
			data.put("auditTime", sf.format(audit.getGmtModified()));// 审核时间
			data.put("auditorName", audit.getAuditorName());// 审核员名称
		}else{
			data.put("auditTime",sf.format(audit.getGmtCreated()));//更新时间
			data.put("auditorName", audit.getOperatorName());// 申请操作员名称
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
			Map<String, Object> payeeBankAcctInfo = (Map<String, Object>) auditData
					.get("payeeBankAcctInfo");
			data.put("memo", tradeReqestMap.get("memo"));
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
			
			data.put("remark", audit.getRemark());// 设置备注信息
			data.put("status", audit.getStatus());// 审核结果
			
			String[] arr = null;
			String payeeNo = null;
			String payeeAccountName = null;
			if(audit.getPayeeNo()!= null) {
				arr = audit.getPayeeNo().split("\\|");
				if((arr != null)  && (arr.length > 1)){
					payeeNo = arr[0];
					payeeAccountName = arr[1];
					if(AuditType.AUDIT_TRANSFER_BANK.getCode().equals(audit.getAuditType())){
						payeeNo = CommonUtils.getMaskData(uesServiceClient.getDataByTicket(payeeNo));
					}
				}else{
					payeeNo = arr[0];
				}
			}
			data.put("payeeNo", payeeNo);// 设置收款账号
			data.put("payeeAccountName",payeeAccountName );// 设置收款账户名称
			data.put("payeeBankInfo", audit.getPayeeBankInfo());//设置收款支行信息
		}
		restP.setData(data);
		restP.setSuccess(true);
		
		// 当前操作员审核权限
//		boolean isDraw = auth(request, FunctionType.EW_MY_APPROVE.getCode());
		
		if (subType.equals(AuditSubType.BATCH.getCode())) {// 底部不带审核(批量)
			return new ModelAndView(CommonConstant.URL_PREFIX
					+ "/apply/transferBank_BatchDetail", "response", restP);
		}
		return new ModelAndView(CommonConstant.URL_PREFIX
				+ "/transfer/transferBank_AuditDetail", "response", restP);// 底部不带审核(单笔)
	}
}
