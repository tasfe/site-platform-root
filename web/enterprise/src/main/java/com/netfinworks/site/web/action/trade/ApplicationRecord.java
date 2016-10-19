package com.netfinworks.site.web.action.trade;

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
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.meidusa.fastjson.JSONObject;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.deposit.api.domain.PageInfo;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.audit.Audit;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.trade.AuditListQuery;
import com.netfinworks.site.domainservice.audit.AuditServiceImpl;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.netfinworks.site.ext.integration.ues.UesServiceClient;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.common.constant.AuditType;
import com.netfinworks.site.web.common.constant.FunctionType;
import com.netfinworks.site.web.util.CommonUtils;
import com.netfinworks.site.web.util.DateUtils;
import com.netfinworks.site.web.util.ExcelUtil;
/**
 * 申请记录查询
 * @author chenhui
 */
@Controller
public class ApplicationRecord extends BaseAction{
    @Resource(name = "defaultMemberService")
    private DefaultMemberService       defaultMemberService;
    
    @Resource(name="accountService")
    private AccountService accountService;
    
    @Resource(name = "auditService")
    private AuditServiceImpl       auditService;
    
    @Resource(name = "uesService")
	private UesServiceClient uesServiceClient; 

    private SimpleDateFormat           sdf = new SimpleDateFormat("yyyy-MM-dd");

	@RequestMapping(value = "/my/applicationRecord.htm", method = { RequestMethod.POST,
			RequestMethod.GET })
	public ModelAndView searchAllAudit(HttpServletRequest req,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		String currentPage = req.getParameter("currentPage");
		String startDate = req.getParameter("startDate");
		String endDate = req.getParameter("endDate");
		
		String auditType = req.getParameter("auditType");// 审核类型
		if(auditType==null){
			auditType="";
		}
		String txType = req.getParameter("txType");// 提现类型 (提现审核)
		String appType = req.getParameter("appType");//应用类型（应用审核）
		
		String shStatus = req.getParameter("shStatus");// 审核状态
		if(shStatus==null){
			shStatus="";
		}
		String selectType = req.getParameter("selectType");// 关键字类型
		String value=req.getParameter("value");//关键值
		
		String queryByTimeType = req.getParameter("queryByTime");//申请时间、审核时间
		
		
		
		if (StringUtils.isBlank(currentPage)) {
			currentPage = "1";
		}
		
		Map<String, Object> data = new HashMap<String, Object>();
		
		Map<String, String> errorMap = new HashMap<String, String>();
		EnterpriseMember user = getUser(req);
		checkUser(user, errorMap, restP);

		// 当前操作员审核权限
//		boolean isDraw = auth(req, FunctionType.EW_MY_APPROVE.getCode());
		
		// 分页信息及信息列表
		PageResultList resultList = new PageResultList();
		PageInfo pageInfo = new PageInfo();
		pageInfo.setCurrentPage(Integer.parseInt(currentPage));
//		if(isDraw){
			EnterpriseMember member = defaultMemberService.queryCompanyMember(user,
					env);
			member.setAccount(accountService.queryAccountById(user.getDefaultAccountId(), env));
			
		
			/* 查询审核列表 */
			AuditListQuery query = new AuditListQuery();
			query.setSelectType(selectType);//设置需要查询的类型
			query.setValue(value);//设置需要查询的值
			query.setMemberId(user.getMemberId());//查询出来的是转账审核
			query.setAuditType(auditType);//设置审核类型
//			query.setTxType(txType);//设置提现类型
			query.setStatus(shStatus);//
			
			if(queryByTimeType == null || "gmtCreated".equals(queryByTimeType)){
				query.setQueryByTime("gmtCreated");
			}else{
				query.setQueryByTime("gmtModified");
			}
			
			/**暂时注释*/
			if (StringUtils.isNotBlank(startDate)) {
				query.setStartDate(DateUtils.parseDate(startDate.substring(0,10)+ " 00:00:01"));
			}
			else {
				startDate = sf.format(DateUtil.getDateNearCurrent(-30))+" 00:00";
				query.setStartDate(DateUtils.parseDate(startDate.substring(0,10)+ " 00:00:01"));
			}
			
			if (StringUtils.isNotBlank(endDate)) {
				query.setEndDate(DateUtils.parseDate(endDate.subSequence(0, 10)+ " 23:59:59"));
			}
			else {
				query.setEndDate(new Date());
				endDate = sdf.format(new Date())+" 59:59";
			}
			data.put("startDate", startDate);
	        data.put("endDate", endDate);
			int totalItem = auditService.count(query);
			
			query.setQueryBase(pageInfo);
			
			List<Audit> auditList = auditService.queryAuditList(query);
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

			for (int i = 0; i < auditList.size(); i++) {
				Audit audit = auditList.get(i);
				Map<String, Object> map = new HashMap<String, Object>();//页面数据从此map中提取
				
				Map<String, Object> auditData = JSONObject.parseObject(audit
						.getAuditData());//获取待审核交易数据
				
//				Map<String, Object> requestMap = (Map<String, Object>) auditData.get("tradeReqest");
//				Map<String, Object> payeeBankAcctMap = (Map<String, Object>) auditData.get("payeeBankAcctInfo");
//				Map<String, Object> payerMap = new HashMap<String, Object>();
				
				Money amount = audit.getAmount();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String createTime = sdf.format(audit.getGmtCreated());
				map.put("gmtCreated", createTime);
				map.put("amount", amount.getAmount().toString());
				map.put("operatorName", audit.getOperatorName());//设置操作员(申请人)
				if(audit.getFee()==null){
					audit.setFee(new Money("0.00"));
				}
				map.put("tranSourceId", audit.getTranSourceVoucherNo());//原交易订单号
				map.put("fee", audit.getFee().getAmount());//服务费(手续费) 待加的字段
				map.put("transId", audit.getTranVoucherNo());//订单号
				map.put("auditType", audit.getAuditType());
				map.put("appType", appType);//应用类型
				map.put("auditorName", audit.getAuditorName());//审核操作员
				map.put("status", audit.getStatus());//设置审核状态
				map.put("id", audit.getId());
				String payeeNo = audit.getPayeeNo();
				String payeeAccountName = null;
				if(payeeNo != null){
					String[] payeeNos = payeeNo.split("\\|");
					payeeNo = payeeNos[0];
					if(payeeNos.length>1){
						payeeAccountName = payeeNos[1];
					}
					if(AuditType.AUDIT_PAYOFF_BANK.getCode().equals(audit.getAuditType())
							|| AuditType.AUDIT_TRANSFER_BANK.getCode().equals(audit.getAuditType())
							|| AuditType.AUDIT_WITHDRAW.getCode().equals(audit.getAuditType())){
						payeeNo = CommonUtils.getMaskData(uesServiceClient.getDataByTicket(payeeNo));
					}
					map.put("payeeNo", payeeNo);//银行卡账号
					map.put("payeeAccountName", audit.getPayeeMemberId());
				}
				map.put("payeeBankInfo", audit.getPayeeBankInfo());//设置收款支行信息
				map.put("otransId", audit.getOrigTranVoucherNo());//原交易订单号 添加日期：2014-8-6 16:32
				
				String skipURL = null;//用于待审核页面每条记录的跳转地址
				if(AuditType.AUDIT_TRANSFER_BANK.getCode().equals(audit.getAuditType())
						 || AuditType.AUDIT_PAY_TO_CARD.getCode().equals(
									audit.getAuditType())){
					skipURL = "my/transferBankAudit1.htm";
				}
				if(AuditType.AUDIT_TRANSFER_KJT.getCode().equals(audit.getAuditType())
						||AuditType.AUDIT_TRANSFER.getCode().equals(
								audit.getAuditType())){
					skipURL = "my/transferAudit1.htm";
				}
				if(AuditType.AUDIT_WITHDRAW.getCode().equals(audit.getAuditType())){
					skipURL ="my/cashAudit1.htm";
				}
				if(AuditType.AUDIT_REFUND.getCode().equals(audit.getAuditType())){
					skipURL="my/refundAudit1.htm";
				}
				if(AuditType.AUDIT_PAYOFF_BANK.getCode().equals(audit.getAuditType())){
					skipURL="my/payoffBankAudit1.htm";
				}
				if(AuditType.AUDIT_PAYOFF_KJT.getCode().equals(audit.getAuditType())){
					skipURL="my/appAudit_daifa1.htm";
				}
				if("daifu".equals(audit.getAuditType())){
					skipURL="my/appAudit_daifu1.htm";
				}
				map.put("skipURL", skipURL);
				
				// 类型为提现的待审核数据组装
				if (AuditType.AUDIT_WITHDRAW.getCode().equals(audit.getAuditType())) {
//					map.put("memberName", audit.getPayeeBankInfo());//获得银行卡账户信息
					list.add(map);
				} else if (AuditType.AUDIT_TRANSFER_BANK.getCode().equals(audit.getAuditType())||AuditType.AUDIT_TRANSFER_KJT.getCode().equals(audit.getAuditType())) {
//					payerMap = (Map<String, Object>) requestMap.get("payer");
//					Map<String, Object> payeeMap = (Map<String, Object>) requestMap.get("payee");
//					map.put("memberName", audit.getPayeeBankInfo());//获得银行卡账户信息
					list.add(map);
				}else if(AuditType.AUDIT_PAYOFF_BANK.getCode().equals(audit.getAuditType())||AuditType.AUDIT_PAYOFF_KJT.getCode().equals(audit.getAuditType())){
//					map.put("memberName", audit.getPayeeBankInfo());//获得银行卡账户信息
					list.add(map);
				}else if("daifu".equals(audit.getAuditType())){
//					map.put("memberName", audit.getPayeeBankInfo());//获得银行卡账户信息
					list.add(map);
				}else if(AuditType.AUDIT_REFUND.getCode().equals(audit.getAuditType())){
//					map.put("memberName", audit.getPayeeBankInfo());//获得银行卡账户信息
//					map.put("otransId", requestMap.get("tradeVoucherNo"));//原交易订单号
					list.add(map);
				}else{
					list.add(map);
				}
			}			
			
			pageInfo.setTotalItem(totalItem);
			resultList.setPageInfo(pageInfo);// 分页信息
			resultList.setInfos(list);// list
			data.put("auditType", auditType);
			data.put("member", member);
//		}
		data.put("mobile", user.getMobileStar());
		data.put("page", resultList);
		data.put("shStatus", shStatus);
		data.put("selectType", selectType);
		data.put("value", value);
		data.put("pageReqMapping", "/my/all-audit.htm");
		restP.setData(data);
		restP.setSuccess(true);
		
		return new ModelAndView(CommonConstant.URL_PREFIX + "/trade/ApplicationRecord", "response", restP);
	}
	
	/**
     * 导出申请记录
     * @param req
     * @param resp
     * @param error
     * @param model
     * @param env
     * @return
     * @throws Exception
     */
	@RequestMapping(value = "/my/downloadApplication.htm", method = { RequestMethod.POST, RequestMethod.GET })
    public void downloadToExcel(HttpServletRequest req, HttpServletResponse resp,
                                      boolean error, ModelMap model, OperationEnvironment env)
                                                                                              throws Exception {
    	RestResponse restP = new RestResponse();
		String currentPage = "1";
		String startDate = req.getParameter("startDate");
		String endDate = req.getParameter("endDate");
		
		String auditType = req.getParameter("auditType");// 审核类型
		String queryByTimeType = req.getParameter("queryByTime");//申请时间、审核时间
		if(auditType.equals("")){
			auditType="myPennding";
		}
		String txType = req.getParameter("txType");// 提现类型 (提现审核)
		
		String shStatus = req.getParameter("shStatus");// 审核状态
		String selectType = req.getParameter("selectType");// 关键字类型
		String value= req.getParameter("value");//关键值
		String queryByTime = req.getParameter("queryByTime");
		
		
		Map<String, Object> data = new HashMap<String, Object>();
		Map<String, String> errorMap = new HashMap<String, String>();
		EnterpriseMember user = getUser(req);
		checkUser(user, errorMap, restP);

		// 当前操作员提现审核权限
		boolean isWithdraw = auth(req, "EW_WITHDRAW_AUDIT");
		// 当前操作员转账审核权限
		boolean isTransfer = auth(req, "EW_TRANSFER_AUDIT");
		if (isWithdraw)
			data.put("isWithdraw", "1");
		else
			data.put("isWithdraw", "0");

		if (isTransfer)
			data.put("isTransfer", "1");
		else
			data.put("isTransfer", "0");

		EnterpriseMember member = defaultMemberService.queryCompanyMember(user,
				env);
		member.setAccount(accountService.queryAccountById(user.getDefaultAccountId(), env));

		/* 查询审核列表 */
		AuditListQuery query = new AuditListQuery();
		query.setQueryByTime(queryByTime);
		query.setSelectType(selectType);//设置需要查询的类型
		query.setValue(value);//设置需要查询的值
		query.setMemberId(user.getMemberId());//查询出来的是转账审核
		if("myPennding".equals(auditType)){
			query.setAuditType("");//设置审核类型
		}else{
			query.setAuditType(auditType);
		}
		query.setTxType(txType);//设置提现类型
		query.setStatus(shStatus);//
		if(queryByTimeType == null || "gmtCreated".equals(queryByTimeType)){
			query.setQueryByTime("gmtCreated");
		}else{
			query.setQueryByTime("gmtModified");
		}
		/**暂时注释*/
		if (StringUtils.isNotBlank(startDate)) {
			query.setStartDate(DateUtils.parseDate(startDate.substring(0,10)+ " 00:00:01"));
		}
		else {
			startDate = sdf.format(DateUtil.getDateNearCurrent(-30));
			query.setStartDate(DateUtils.parseDate(startDate.substring(0,10)+ " 00:00:01"));
		}
		
		if (StringUtils.isNotBlank(endDate)) {
			query.setEndDate(DateUtils.parseDate(endDate.subSequence(0, 10)+ " 23:59:59"));
		}
		else {
			query.setEndDate(new Date());
			endDate = sdf.format(new Date());
		}
		
		int totalItem = auditService.count(query);
		
		PageInfo pageInfo = new PageInfo();
		pageInfo.setCurrentPage(Integer.parseInt(currentPage));
		pageInfo.setPageSize(50000);//导出审核记录为查询结果过滤的前5万条记录
		query.setQueryBase(pageInfo);
		 
		List<Audit> auditList = auditService.queryAuditList(query);
		
        ExcelUtil excelUtil = new ExcelUtil();
        Integer excelType = null;
        if("transfer".equals(auditType)){
        	excelType = 1;//转账申请
        }else if(AuditType.AUDIT_WITHDRAW.getCode().equals(auditType)){
        	excelType = 2;//提现申请
        }else if(AuditType.AUDIT_REFUND.getCode().equals(auditType)){
        	excelType = 3;//退款申请
        }else if(AuditType.AUDIT_PAYOFF_BANK.getCode().equals(auditType)||AuditType.AUDIT_PAYOFF_KJT.getCode().equals(auditType)){
        	excelType = 4;//代发工资申请
        }else if("daifu".equals(auditType)){
        	excelType = 5;//代付订单申请
        }else{
        	excelType = 6;//全部申请
        }
        excelUtil.toExcel3(req, resp, auditList, excelType, startDate, endDate);
    }
}
