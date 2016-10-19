package com.netfinworks.site.web.action.trade;

import java.io.UnsupportedEncodingException;
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
import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.deposit.api.domain.DepositListRequest;
import com.netfinworks.deposit.api.domain.PageInfo;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.audit.Audit;
import com.netfinworks.site.domain.domain.info.EncryptData;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.member.OperatorVO;
import com.netfinworks.site.domain.domain.request.AuthCodeRequest;
import com.netfinworks.site.domain.domain.response.CoTradeQueryResponse;
import com.netfinworks.site.domain.domain.trade.AuditListQuery;
import com.netfinworks.site.domain.domain.trade.CoTradeRequest;
import com.netfinworks.site.domain.domain.trade.DownloadBillRequest;
import com.netfinworks.site.domain.domain.trade.FundoutQuery;
import com.netfinworks.site.domain.enums.DeciphedQueryFlag;
import com.netfinworks.site.domain.enums.DeciphedType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.audit.AuditServiceImpl;
import com.netfinworks.site.domainservice.deposit.DefaultDepositInfoService;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.domainservice.spi.DefaultSmsService;
import com.netfinworks.site.domainservice.trade.DefaultDownloadBillService;
import com.netfinworks.site.domainservice.trade.DefaultFundoutService;
import com.netfinworks.site.domainservice.ues.DefaultUesService;
import com.netfinworks.site.ext.integration.auth.outer.OperatorAuthOuterService;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.netfinworks.site.ext.integration.member.MemberService;
import com.netfinworks.site.ext.integration.member.OperatorService;
import com.netfinworks.site.ext.integration.trade.TradeService;
import com.netfinworks.site.ext.integration.ues.UesServiceClient;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.action.form.QueryTradeForm;
import com.netfinworks.site.web.common.constant.AuditType;
import com.netfinworks.site.web.common.constant.FunctionType;
import com.netfinworks.site.web.util.CommonUtils;
import com.netfinworks.site.web.util.DateUtils;
import com.netfinworks.site.web.util.ExcelUtil;

/**
 * 
 * <p>
 * 交易记录查询action
 * </p>
 * 
 * @author Guan Xiaoxu
 * @version $Id: TradeAction.java, v 0.1 2013-11-27 下午3:22:46 Guanxiaoxu Exp $
 */
@Controller
public class TradeAction extends BaseAction {
	protected Logger log = LoggerFactory.getLogger(getClass());

	@Resource(name = "memberService")
	private MemberService memberService;

	@Resource(name = "defaultSmsService")
	private DefaultSmsService defaultSmsService;

	@Resource(name = "defaultUesService")
	private DefaultUesService defaultUesService;

	@Resource(name = "defaultFundoutService")
	private DefaultFundoutService defaultFundoutService;

	@Resource(name = "defaultMemberService")
	private DefaultMemberService defaultMemberService;

	@Resource(name = "defaultDepositInfoService")
	private DefaultDepositInfoService defaultDepositInfoService;

	@Resource(name = "tradeService")
	private TradeService tradeService;

	@Resource(name = "defaultDownloadBillService")
	private DefaultDownloadBillService defaultDownloadBillService;

	@Resource(name = "auditService")
	private AuditServiceImpl auditService;
	
	@Resource(name = "uesService")
	private UesServiceClient uesServiceClient; 
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	private SimpleDateFormat sdfhms = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	@Resource
	private OperatorAuthOuterService operatorAuthOuterService;

	@Resource(name = "accountService")
	private AccountService accountService;
	
	@Resource(name="operatorService")
	private OperatorService operatorService;
	/**
	 * zhangyun.m 查询询交易记录
	 * 
	 * @return
	 */
	@RequestMapping(value = "/my/all-trade.htm", method = { RequestMethod.POST,
			RequestMethod.GET })
	public ModelAndView searchAll(HttpServletRequest request,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env, QueryTradeForm form) throws Exception {
		String refresh = request.getParameter("refresh");
		if (StringUtils.isNotEmpty(refresh)
				&& refresh.equals(CommonConstant.TRUE_STRING)) {
			super.updateSessionObject(request);
		}
		EnterpriseMember member = null;
		CoTradeQueryResponse rep = null;
		Map<String, String> errorMap = new HashMap<String, String>();
		QueryBase queryBase = new QueryBase();
		CoTradeRequest breq = new CoTradeRequest();
		RestResponse restP = new RestResponse();
		restP.setData(new HashMap<String, Object>());

		String currentPage = form.getCurrentPage();
		// try {
		if (StringUtils.isBlank(currentPage)) {
			currentPage = "1";
		}
		queryBase.setCurrentPage(Integer.valueOf(currentPage));
		breq.setQueryBase(queryBase);
		EnterpriseMember user = getUser(request);
		checkUser(user, errorMap, restP);
		// 查询会员所以需要的信息
		member = defaultMemberService.queryCompanyMember(user, env);
		member.setAccount(accountService.queryAccountById(
				user.getDefaultAccountId(), env));
		// 不需要汇总
		breq.setNeedSummary(false);

		breq.setMemberId(user.getMemberId());
		// 查询时间
		String queryStartTime = form.getQueryStartTime();
		String queryEndTime = form.getQueryEndTime();
		if (StringUtils.isNotBlank(queryStartTime)) {
			breq.setBeginTime(DateUtils.parseDate(queryStartTime + " 00:00:00"));
		} else {
			breq.setBeginTime(DateUtil.getDateNearCurrent(-30));
			queryStartTime = DateUtils.getDateString();

		}
		if (StringUtils.isNotBlank(queryEndTime)) {
			breq.setEndTime(DateUtils.parseDate(queryEndTime + " 23:59:59"));
		} else {
			breq.setEndTime(new Date());
			queryEndTime = sdf.format(new Date());
		}

		restP.getData().put("mobile", member.getMobileStar());
		restP.getData().put("member", member);
		restP.getData().put("queryStartTime", queryStartTime);
		restP.getData().put("queryEndTime", queryEndTime);
		// 页面请求Mapping
		restP.getData().put("pageReqMapping", "/my/all-trade.htm");
		// 查询所有交易信息
		rep = tradeService.queryList(breq);

		restP.setSuccess(true);
		if (rep != null) {
			restP.getData().put("list", rep.getBaseInfoList());
			restP.getData().put("page", rep.getQueryBase());
		}

		return new ModelAndView(CommonConstant.URL_PREFIX
				+ "/trade/trade_recode", "response", restP);
	}

	/**
	 * 查询提现记录
	 * 
	 * @param req
	 * @param resp
	 * @param error
	 * @param model
	 * @param env
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/my/all-cach.htm", method = { RequestMethod.POST,
			RequestMethod.GET })
	public ModelAndView searchAllCach(HttpServletRequest req,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env) throws Exception {
		String refresh = req.getParameter("refresh");
		if (StringUtils.isNotEmpty(refresh)
				&& refresh.equals(CommonConstant.TRUE_STRING)) {
			super.updateSessionObject(req);
		}
		RestResponse restP = new RestResponse();
		String currentPage = req.getParameter("currentPage");
		String startDate = req.getParameter("startDate");
		String endDate = req.getParameter("endDate");
		if (StringUtils.isBlank(currentPage)) {
			currentPage = "1";
		}
		Map<String, Object> data = new HashMap<String, Object>();
		Map<String, String> errorMap = new HashMap<String, String>();
		EnterpriseMember user = getUser(req);
		checkUser(user, errorMap, restP);
		EnterpriseMember member = defaultMemberService.queryCompanyMember(user,
				env);
		member.setAccount(accountService.queryAccountById(
				user.getDefaultAccountId(), env));
		FundoutQuery fundoutQuery = new FundoutQuery();
		fundoutQuery.setMemberId(user.getMemberId());
		fundoutQuery.setAccountNo(user.getDefaultAccountId());
		fundoutQuery.setCurrentPage(Integer.valueOf(currentPage));
		if (StringUtils.isNotBlank(startDate)) {
			fundoutQuery.setOrderTimeStart(DateUtils.parseDate(startDate
					+ " 00:00:01"));
		} else {
			fundoutQuery.setOrderTimeStart(DateUtil.getDateNearCurrent(-30));
			startDate = DateUtils.getDateString();

		}
		if (StringUtils.isNotBlank(endDate)) {
			fundoutQuery.setOrderTimeEnd(DateUtils.parseDate(endDate
					+ " 23:59:59"));
		} else {
			fundoutQuery.setOrderTimeEnd(new Date());
			endDate = sdf.format(new Date());
		}
		PageResultList page = defaultFundoutService.queryFundoutInfo(
				fundoutQuery, env);
		data.put("page", page);
		data.put("mobile", user.getMobileStar());
		data.put("member", member);
		data.put("startDate", startDate);
		data.put("endDate", endDate);
		data.put("pageReqMapping", "/my/all-cach.htm");
		restP.setData(data);
		restP.setSuccess(true);
		return new ModelAndView(CommonConstant.URL_PREFIX
				+ "/list/withdrawal-list", "response", restP);

	}

	/**
	 * 查询转账记录
	 * 
	 * @param req
	 * @param resp
	 * @param error
	 * @param model
	 * @param env
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/my/all-transfer.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView searchAllTransfer(HttpServletRequest request,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env) throws Exception {
		String refresh = request.getParameter("refresh");
		if (StringUtils.isNotEmpty(refresh)
				&& refresh.equals(CommonConstant.TRUE_STRING)) {
			super.updateSessionObject(request);
		}
		RestResponse restP = new RestResponse();
		String currentPage = request.getParameter("currentPage");
		String startDate = request.getParameter("startDate");
		String endDate = request.getParameter("endDate");
		if (StringUtils.isBlank(currentPage)) {
			currentPage = "1";
		}
		Map<String, String> errorMap = new HashMap<String, String>();
		EnterpriseMember user = getUser(request);
		checkUser(user, errorMap, restP);
		EnterpriseMember member = defaultMemberService.queryCompanyMember(user,
				env);
		member.setAccount(accountService.queryAccountById(
				user.getDefaultAccountId(), env));
		DownloadBillRequest reqInfo = new DownloadBillRequest();

		if (StringUtils.isNotBlank(startDate)) {
			reqInfo.setBeginTime(DateUtils.parseDate(startDate + " 00:00:01"));
		} else {
			reqInfo.setBeginTime(DateUtil.getDateNearCurrent(-30));
			startDate = DateUtils.getDateString();

		}
		if (StringUtils.isNotBlank(endDate)) {
			reqInfo.setEndTime(DateUtils.parseDate(endDate + " 23:59:59"));
		} else {
			reqInfo.setEndTime(new Date());
			endDate = sdf.format(new Date());
		}

		// reqInfo.setSellerId(user.getMemberId());
		reqInfo.setMemberId(user.getMemberId());
		QueryBase queryBase = new QueryBase();
		queryBase.setCurrentPage(Integer.parseInt(currentPage));
		reqInfo.setQueryBase(queryBase);

		PageResultList page = defaultDownloadBillService.queryTransfer(reqInfo,
				env);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("page", page);
		map.put("mobile", user.getMobileStar());
		map.put("member", member);
		map.put("startDate", startDate);
		map.put("endDate", endDate);
		map.put("pageReqMapping", "/my/all-transfer.htm");
		restP.setData(map);
		restP.setSuccess(true);
		return new ModelAndView(CommonConstant.URL_PREFIX
				+ "/list/transfer-list", "response", restP);

	}

	/**
	 * 查询钱包用户的充值记录
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/my/all-recharge.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView searchAllRecharge(HttpServletRequest request,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env) throws Exception {
		String refresh = request.getParameter("refresh");
		if (StringUtils.isNotEmpty(refresh)
				&& refresh.equals(CommonConstant.TRUE_STRING)) {
			super.updateSessionObject(request);
		}
		RestResponse restP = new RestResponse();
		String currentPage = request.getParameter("currentPage");
		String startDate = request.getParameter("startDate");
		String endDate = request.getParameter("endDate");
		if (StringUtils.isBlank(currentPage)) {
			currentPage = "1";
		}
		Map<String, String> errorMap = new HashMap<String, String>();
		EnterpriseMember user = getUser(request);
		checkUser(user, errorMap, restP);
		EnterpriseMember member = defaultMemberService.queryCompanyMember(user,
				env);
		member.setAccount(accountService.queryAccountById(
				user.getDefaultAccountId(), env));
		DepositListRequest dRequest = new DepositListRequest();
		dRequest.setMemberId(user.getMemberId());
		dRequest.setAccountNo(user.getDefaultAccountId());
		PageInfo pageInfo = new PageInfo();
		pageInfo.setCurrentPage(Integer.valueOf(currentPage));
		dRequest.setPageInfo(pageInfo);
		dRequest.setRequestId(System.currentTimeMillis() + user.getMemberId());
		if (StringUtils.isNotBlank(startDate)) {
			dRequest.setTimeBegin(DateUtils.parseDate(startDate + " 00:00:01"));
		} else {
			dRequest.setTimeBegin(DateUtil.getDateNearCurrent(-30));
			startDate = DateUtils.getDateString();

		}
		if (StringUtils.isNotBlank(endDate)) {
			dRequest.setTimeEnd(DateUtils.parseDate(endDate + " 23:59:59"));
		} else {
			dRequest.setTimeEnd(new Date());
			endDate = sdf.format(new Date());
		}
		PageResultList page = defaultDepositInfoService
				.queryList(dRequest, env);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("page", page);
		map.put("mobile", user.getMobileStar());
		map.put("member", member);
		map.put("startDate", startDate);
		map.put("endDate", endDate);
		map.put("pageReqMapping", "/my/all-recharge.htm");
		restP.setData(map);
		restP.setSuccess(true);
		return new ModelAndView(CommonConstant.URL_PREFIX
				+ "/list/recharge-list", "response", restP);

	}

	/**
	 * 查询审核记录
	 * 
	 * @param req
	 * @param resp
	 * @param error
	 * @param model
	 * @param env
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/my/all-audit.htm", method = { RequestMethod.POST,
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
		// String txType = req.getParameter("txType");// 提现类型 (提现审核)

		String shStatus = req.getParameter("shStatus");// 审核状态
		if(shStatus==null){
		    shStatus="";
		}
		String selectType = req.getParameter("selectType");// 关键字类型
		String value = req.getParameter("value");// 关键值

		String queryByTimeType = req.getParameter("queryByTime");// 申请时间、审核时间

		if (StringUtils.isBlank(currentPage)) {
			currentPage = "1";
		}

		Map<String, Object> data = new HashMap<String, Object>();

		data.put("startDate", startDate);
		data.put("endDate", endDate);
		Map<String, String> errorMap = new HashMap<String, String>();
		EnterpriseMember user = getUser(req);
		checkUser(user, errorMap, restP);

		// 当前操作员审核权限
		boolean isDraw = auth(req, FunctionType.EW_MY_APPROVE.getCode());

		// 分页信息及信息列表
		PageResultList resultList = new PageResultList();
		PageInfo pageInfo = new PageInfo();
		pageInfo.setCurrentPage(Integer.parseInt(currentPage));
		if (isDraw) {
			EnterpriseMember member = defaultMemberService.queryCompanyMember(
					user, env);
			member.setAccount(accountService.queryAccountById(
					user.getDefaultAccountId(), env));

			/* 查询审核列表 */
			AuditListQuery query = new AuditListQuery();
			if("tranSourceVoucherNo".equals(selectType)){
			    query.setSelectType("tranSourceVoucherNo");// 设置需要查询的类型
			    if(value!=""&& value!=null){
			        query.setAuditSubType("single");
			    }
			}else if("tranSourceVoucherBatchNo".equals(selectType)){
			    query.setSelectType("tranSourceVoucherNo");// 设置需要查询的类型
			    if(value!=""&& value!=null){
			        query.setAuditSubType("batch");
			    }
			}else {
			    query.setSelectType("ext");
			}
			query.setValue(value);// 设置需要查询的值
			query.setMemberId(user.getMemberId());// 查询出来的是转账审核
			query.setAuditType(auditType);// 设置审核类型
			// query.setTxType(txType);//设置提现类型
			query.setStatus(shStatus);//

			if ((queryByTimeType == null) || "gmtCreated".equals(queryByTimeType)) {
				query.setQueryByTime("gmtCreated");
			} else {
				query.setQueryByTime("gmtModified");
			}
			data.put("queryByTimeType", queryByTimeType);
			if (StringUtils.isNotBlank(startDate)) {
				query.setStartDate(DateUtils.parseDate(startDate + ":00"));
			} else {
				startDate = sf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
				query.setStartDate(DateUtils.parseDate(startDate + ":00"));
			}

			if (StringUtils.isNotBlank(endDate)) {
				query.setEndDate(DateUtils.parseDate(endDate+ ":59"));
			} else {
				query.setEndDate(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
				endDate = sdf.format(new Date()) + " 23:59";
			}

			int totalItem = auditService.count(query);

			query.setQueryBase(pageInfo);

			List<Audit> auditList = auditService.queryAuditList(query);
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

			for (int i = 0; i < auditList.size(); i++) {
				Audit audit = auditList.get(i);
				Map<String, Object> map = new HashMap<String, Object>();// 页面数据从此map中提取

				Map<String, Object> auditData = JSONObject.parseObject(audit
						.getAuditData());// 获取待审核交易数据
				
				// Map<String, Object> requestMap = (Map<String, Object>)
				// auditData.get("tradeReqest");
				// Map<String, Object> payeeBankAcctMap = (Map<String, Object>)
				// auditData.get("payeeBankAcctInfo");
				// Map<String, Object> payerMap = new HashMap<String, Object>();
				Money amount = audit.getAmount();
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				String createTime = sdf.format(audit.getGmtCreated());
				map.put("gmtCreated", createTime);
				map.put("amount", amount.getAmount().toString());
				map.put("operatorName", audit.getOperatorName());// 设置操作员(申请人)
				if (audit.getFee() == null) {
					audit.setFee(new Money("0.00"));
				}
//				map.put("tranSourceId", audit.getTranSourceVoucherNo());//商户订单号
				Money fee = audit.getFee();
				if(fee.equals(new Money("0"))){
					map.put("fee", fee);
				}else{
					map.put("fee","-" + fee);
				}
				
				map.put("transId", audit.getTranVoucherNo());// 订单号
				map.put("auditType", audit.getAuditType());
				map.put("auditorName", audit.getAuditorName());// 审核操作员
				map.put("shStatus", audit.getStatus());// 设置审核状态
				map.put("id", audit.getId());
				
				map.put("tranSourceVoucherNo", audit.getTranSourceVoucherNo());//商户订单号
				if("refund".equals(audit.getAuditType())){
    				if(audit.getExt() != null && !audit.getExt().isEmpty()){
    				    Map<String, Object> ext = new HashMap<String,Object>();
    				    if(audit.getExt().indexOf(":")!=-1){
        				    ext = JSONObject.parseObject(audit.getExt());
                            String serialNumber = (String)ext.get("serialNumber");
                            map.put("ext", serialNumber);//退款原商户订单号先放在ext字段里
    				    }else{
    				        map.put("ext", audit.getExt());
    				    }
                    }
				}
				map.put("auditSubType", audit.getAuditSubType());
				String[] arr = null;
				String payeeNo = null;
				String payeeAccountName = null;
				if (audit.getPayeeNo() != null) {
					arr = audit.getPayeeNo().split("\\|");
					if ((arr != null) && (arr.length > 1)) {
						payeeNo = arr[0];
						payeeAccountName = arr[1];
						if(AuditType.AUDIT_TRANSFER_BANK.getCode().equals(audit.getAuditType())){
							payeeNo = CommonUtils.getMaskData(uesServiceClient.getDataByTicket(payeeNo));
						}
					} else {
						payeeNo = arr[0];
					}
				}
				
				map.put("payeeNo", payeeNo);// 设置收款账号
				map.put("payeeAccountName", payeeAccountName);// 设置收款账户名称
				map.put("payeeBankInfo", audit.getPayeeBankInfo());// 设置收款支行信息
				map.put("otransId", audit.getOrigTranVoucherNo());// 原交易订单号
																	// 添加日期：2014-8-6
																	// 16:32

				String skipURL = null;// 用于待审核页面每条记录的跳转地址
				if (AuditType.AUDIT_TRANSFER_BANK.getCode().equals(
						audit.getAuditType()) || AuditType.AUDIT_PAY_TO_CARD.getCode().equals(
								audit.getAuditType())) {
					skipURL = "my/transferBankAudit.htm";
				}
				if (AuditType.AUDIT_TRANSFER_KJT.getCode().equals(
						audit.getAuditType())||AuditType.AUDIT_TRANSFER.getCode().equals(
								audit.getAuditType())) {
					skipURL = "my/transferKjtAudit.htm";
				}
				if (AuditType.AUDIT_WITHDRAW.getCode().equals(
						audit.getAuditType())) {
					skipURL = "my/withdrawAudit.htm";
				}
				if (AuditType.AUDIT_REFUND.getCode().equals(
						audit.getAuditType())) {
					skipURL = "my/refundAudit.htm";
				}
				if (AuditType.AUDIT_PAYOFF_BANK.getCode().equals(
						audit.getAuditType())) {
					skipURL = "my/payoffBankAudit.htm";
				}
				if (AuditType.AUDIT_PAYOFF_KJT.getCode().equals(
						audit.getAuditType())) {
					skipURL = "my/payoffKjtAudit.htm";
				}
				if ("daifu".equals(audit.getAuditType())) {
					skipURL = "my/appAudit_daifu.htm";
				}
				map.put("skipURL", skipURL);

				// 类型为提现的待审核数据组装
				if (AuditType.AUDIT_WITHDRAW.getCode().equals(
						audit.getAuditType())) {
					// map.put("memberName",
					// audit.getPayeeBankInfo());//获得银行卡账户信息
					list.add(map);
				} else if (AuditType.AUDIT_TRANSFER_BANK.getCode().equals(audit.getAuditType())
						|| AuditType.AUDIT_TRANSFER_KJT.getCode().equals(audit.getAuditType())
						) {
					// payerMap = (Map<String, Object>) requestMap.get("payer");
					// Map<String, Object> payeeMap = (Map<String, Object>)
					// requestMap.get("payee");
					// map.put("memberName",
					// audit.getPayeeBankInfo());//获得银行卡账户信息
					list.add(map);
				}else if(AuditType.AUDIT_TRANSFER.getCode().equals(audit.getAuditType())
						|| AuditType.AUDIT_PAY_TO_CARD.getCode().equals(audit.getAuditType())){
					String operatorId = (String)auditData.get("operatorId");
					try{
						OperatorVO operator = operatorService.getOperatorById(operatorId, env);
						map.put("operatorName", operator.getLoginName());
					}catch(Exception e){
						logger.error("操作员不存在", e);
					}
					list.add(map);
				} else if (AuditType.AUDIT_PAYOFF_BANK.getCode().equals(
						audit.getAuditType())) {
					// map.put("memberName",
					// audit.getPayeeBankInfo());//获得银行卡账户信息
					list.add(map);
				} else if (AuditType.AUDIT_PAYOFF_KJT.getCode().equals(
						audit.getAuditType())) {
					// map.put("memberName",
					// audit.getPayeeBankInfo());//获得银行卡账户信息
					list.add(map);
				} else if ("daifu".equals(audit.getAuditType())) {
					// map.put("memberName",
					// audit.getPayeeBankInfo());//获得银行卡账户信息
					list.add(map);
				} else if (AuditType.AUDIT_REFUND.getCode().equals(
						audit.getAuditType())) {
					// map.put("memberName",
					// audit.getPayeeBankInfo());//获得银行卡账户信息
					// map.put("otransId",
					// requestMap.get("tradeVoucherNo"));//原交易订单号
					list.add(map);
				}
			}

			pageInfo.setTotalItem(totalItem);
			resultList.setPageInfo(pageInfo);// 分页信息
			resultList.setInfos(list);// list
			data.put("auditType", auditType);
			data.put("member", member);
			// pageInfo.setCurrentPage();
		}
		data.put("queryByTime", queryByTimeType);

		data.put("mobile", user.getMobileStar());
		data.put("page", resultList);
		data.put("shStatus", shStatus);
		data.put("selectType", selectType);
		data.put("value", value);
		data.put("pageReqMapping", "/my/all-audit.htm");
		restP.setData(data);
		restP.setSuccess(true);
		
		String isPending = req.getParameter("isPending");//鍒ゆ柇鏄惁鏄緟瀹℃牳鏍囧織 1涓哄緟瀹℃牳
		
		// 页面传入参数auditType：提现类型为：withdraw，转账类型为：transfer 应用类型 application
		String backUrl = CommonConstant.URL_PREFIX;
		if(isPending != null){
			backUrl += "/list/myPending";// 转账审核
		}else{
			if ("transfer".equals(auditType)) {// 杞处瀹℃牳
				backUrl += "/list/transferPending";
			} else if (AuditType.AUDIT_WITHDRAW.getCode().equals(auditType)) {// 提现审核
				backUrl += "/list/getcashPending";
			} else if (AuditType.AUDIT_REFUND.getCode().equals(auditType)) {// 退款审核
				backUrl += "/list/refundPending";
			} else if ("payoff".equals(auditType)) {// 代发工资审核
				backUrl += "/list/app_payoffPending";
			} else if ("daifu".equals(auditType)) {// 代付订单审核
				backUrl += "/list/app_daifuPending";
			} else{
				backUrl += "/list/myPending";// 待审核
			}
		}
		return new ModelAndView(backUrl, "response", restP);
	}

	/**
	 * 导出审核记录
	 * 
	 * @param req
	 * @param resp
	 * @param error
	 * @param model
	 * @param env
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/my/downloadExaExcel.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public void downloadToExcel(HttpServletRequest req,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env) throws Exception {
		RestResponse restP = new RestResponse();
		String currentPage = "1";
		String startDate = req.getParameter("startDate");
		String endDate = req.getParameter("endDate");
		String queryByTimeType = req.getParameter("queryByTime");// 申请时间、审核时间
		String auditType = req.getParameter("auditType");// 审核类型
		if (auditType.equals("")) {
			auditType = "myPennding";
		}
		String txType = req.getParameter("txType");// 提现类型 (提现审核)

		String shStatus = req.getParameter("shStatus");// 审核状态
		String selectType = req.getParameter("selectType");// 关键字类型
		String value = req.getParameter("value");// 关键值
		String queryByTime = req.getParameter("queryByTime");

		Map<String, Object> data = new HashMap<String, Object>();
		Map<String, String> errorMap = new HashMap<String, String>();
		EnterpriseMember user = getUser(req);
		checkUser(user, errorMap, restP);

		// 当前操作员提现审核权限
		boolean isWithdraw = auth(req, "EW_WITHDRAW_AUDIT");
		// 当前操作员转账审核权限
		boolean isTransfer = auth(req, "EW_TRANSFER_AUDIT");
		if (isWithdraw) {
			data.put("isWithdraw", "1");
		} else {
			data.put("isWithdraw", "0");
		}

		if (isTransfer) {
			data.put("isTransfer", "1");
		} else {
			data.put("isTransfer", "0");
		}

		EnterpriseMember member = defaultMemberService.queryCompanyMember(user,
				env);
		member.setAccount(accountService.queryAccountById(
				user.getDefaultAccountId(), env));

		/* 查询审核列表 */
		AuditListQuery query = new AuditListQuery();
		query.setQueryByTime(queryByTime);
		if("tranSourceVoucherNo".equals(selectType)){
            query.setSelectType("tranSourceVoucherNo");// 设置需要查询的类型
            if(value!=""&& value!=null){
                query.setAuditSubType("single");
            }
        }else if("tranSourceVoucherBatchNo".equals(selectType)){
            query.setSelectType("tranSourceVoucherNo");// 设置需要查询的类型
            if(value!=""&& value!=null){
                query.setAuditSubType("batch");
            }
        }else {
            query.setSelectType("ext");
        }
		query.setValue(value);// 设置需要查询的值
		query.setMemberId(user.getMemberId());// 查询出来的是转账审核
		if ("myPennding".equals(auditType)) {
			query.setAuditType("");// 设置审核类型
		} else {
			query.setAuditType(auditType);
		}
		query.setTxType(txType);// 设置提现类型
		query.setStatus(shStatus);//

		/** 暂时注释 */
		if ((queryByTimeType == "") || "gmtCreated".equals(queryByTimeType)) {
            query.setQueryByTime("gmtCreated");
        } else {
            query.setQueryByTime("gmtModified");
        }
		if (StringUtils.isNotBlank(startDate)) {
            query.setStartDate(DateUtils.parseDate(startDate + ":00"));
        } else {
            startDate = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
            query.setStartDate(DateUtils.parseDate(startDate + ":00"));
        }

        if (StringUtils.isNotBlank(endDate)) {
            query.setEndDate(DateUtils.parseDate(endDate+ ":59"));
        } else {
            query.setEndDate(sdfhms.parse(sdf.format(new Date()) + " 23:59:59"));
            endDate = sdf.format(new Date()) + " 23:59";
        }

		int totalItem = auditService.count(query);

		PageInfo pageInfo = new PageInfo();
		pageInfo.setCurrentPage(Integer.parseInt(currentPage));
		pageInfo.setPageSize(50000);// 导出审核记录为查询结果过滤的前5万条记录
		query.setQueryBase(pageInfo);

		List<Audit> auditList = auditService.queryAuditList(query);

		ExcelUtil excelUtil = new ExcelUtil();
		excelUtil.setUesServiceClient(this.uesServiceClient);
		Integer excelType = null;
		if ("transfer".equals(auditType)) {
			excelType = 5;// 转账审核
		} else if (AuditType.AUDIT_WITHDRAW.getCode().equals(auditType)) {
			excelType = 6;// 提现审核
		} else if (AuditType.AUDIT_REFUND.getCode().equals(auditType)) {
			excelType = 7;// 退款审核
		} else if ("payoff".equals(auditType)) {
			excelType = 8;// 代发工资
		} else if ("daifu".equals(auditType)) {
			excelType = 9;// 代付订单
		} else if ("myPennding".equals(auditType)) {
			excelType = 10;// 我的待审核
		}
		excelUtil.toExcel(req, resp, auditList, excelType, startDate, endDate);
	}

	// 异步验证手机校验码
	@RequestMapping(value = "/my/verifyCode.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody
	RestResponse verifyCode(HttpServletRequest request, HttpSession session,
			OperationEnvironment env) {
		String bizType = request.getParameter("bizType");
		RestResponse restP = new RestResponse();
		EnterpriseMember user = getUser(request);
		EncryptData edata = null;
		try {
			edata = memberService.decipherMember(user.getMemberId(),
					DeciphedType.CELL_PHONE, DeciphedQueryFlag.ALL, env);
		} catch (BizException e1) {
			logger.error("网关异常!", e1);
		}
		String mobile = edata.getPlaintext();
		String mobileCaptcha = request.getParameter("mobileCaptcha");
		log.info("mobile:{},mobileCaptcha:{}", mobile, mobileCaptcha);
		// 封装校验短信验证码请求
		AuthCodeRequest req = new AuthCodeRequest();
		req.setMemberId(user.getMemberId());
		req.setMobile(mobile);
		String ticket = null;
		try {
			ticket = defaultUesService.encryptData(mobile);
		} catch (ServiceException e1) {
			logger.error("加密单个数据出现异常!", e1);
		}
		req.setMobileTicket(ticket);
		req.setAuthCode(mobileCaptcha);
		req.setBizId(user.getMemberId());
		req.setBizType(bizType);
		// 校验短信验证码
		boolean verifyResult = false;
		try {
			verifyResult = defaultSmsService.verifyMobileAuthCode(req, env);
		} catch (ServiceException e) {
			logger.error("验证校验码出现异常!", e);
		}

//		verifyResult = true;// 先设置为验证正确
		if (verifyResult) {
			session.setAttribute("mobileCaptcha", mobileCaptcha);// 将校验码放入session
																	// 用于表单提交时验证(原因:校验码服务只能校验一次)
			restP.setMessage("校验码正确");
			restP.setSuccess(true);
			return restP;
		} else {
			restP.setSuccess(false);
			restP.setMessage("您输入的校验码错误，请重新输入!");
			return restP;
		}
	}

	
	// 异步验证签名
	@RequestMapping(value = "/my/verifySign.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody
	RestResponse verifySign(HttpServletRequest request, HttpSession session,
			OperationEnvironment env) {
		RestResponse restP = new RestResponse();
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
		restP.setSuccess(true);
		return restP;
	}
		
	
	// 异步验证支付密码
	@RequestMapping(value = "/my/verifyPaypass.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody
	RestResponse verifyPaypass(HttpServletRequest request, HttpSession session,
			OperationEnvironment env) {
		RestResponse restP = new RestResponse();
		EnterpriseMember user = getUser(request);
		Map<String, Object> data = new HashMap<String, Object>();
		if (!validatePayPassword(request, user, restP, null)) {
			log.info("verifyPaypass:", restP.getMessage());
			restP.setSuccess(false);
			restP.setMessage(restP.getMessage());
			return restP;
		}
		return restP;
	}
}
