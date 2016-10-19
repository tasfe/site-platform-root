package com.netfinworks.site.web.action.trade;


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
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.WalletCheckInfo;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.response.WalletCheckResponse;
import com.netfinworks.site.domain.domain.trade.WalletCheckRequest;
import com.netfinworks.site.domain.enums.DealType;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.netfinworks.site.ext.integration.trade.CoAccountService;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.action.form.QueryTradeForm;
import com.netfinworks.site.web.util.DateUtils;
import com.netfinworks.site.web.util.ExcelUtil;
/**
 * 收支明细
 */
@Controller
public class PayDetailsAction extends BaseAction {
	@Resource(name = "coAccountService")
	private CoAccountService coAccountService;

	@Resource(name = "defaultMemberService")
	private DefaultMemberService defaultMemberService;

    @Resource(name="accountService")
    private AccountService accountService;
    
    private SimpleDateFormat           sdf = new SimpleDateFormat("yyyy-MM-dd");
    
	@RequestMapping(value = "/my/pay-detail.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView searchAll(HttpServletRequest req,
			HttpServletResponse resp, boolean error, ModelMap model,
			QueryTradeForm form) throws Exception {
		RestResponse restP = new RestResponse();
		restP.setData(new HashMap<String, Object>());
		restP.setSuccess(false);
		Map<String, String> errorMap = new HashMap<String, String>();
		EnterpriseMember member = null;
		EnterpriseMember user = getUser(req);

		// 分页
		QueryBase queryBase = new QueryBase();
		queryBase.setNeedQueryAll(true);
		String currentPage = form.getCurrentPage();
		if (StringUtils.isBlank(currentPage)) {
			currentPage = "1";
		}

		checkUser(user, errorMap, restP);
		// 查询会员所以需要的信息
		member = defaultMemberService.queryCompanyMember(user, env);
		member.setAccount(accountService.queryAccountById(user.getDefaultAccountId(), env));
		WalletCheckRequest wcRequest = new WalletCheckRequest();
		wcRequest.setMemberId(user.getMemberId());
		wcRequest.setAccountNo(user.getDefaultAccountId());
		wcRequest.setClientIp(req.getRemoteAddr());
		// 账户类型
		wcRequest.setAccountType(301);
		// 交易类型(枚举)
		wcRequest.setTxnType(DealType.ALLDEALTYPE);
		// 查询时间
        String queryStartTime = form.getQueryStartTime();
        String queryEndTime = form.getQueryEndTime();
        
        if (StringUtils.isNotBlank(queryStartTime)) {
        	wcRequest.setBeginTime(DateUtils.parseDate(queryStartTime + ":00"));
        } else {
            queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
        	wcRequest.setBeginTime(DateUtils.parseDate(queryStartTime + ":00"));
        }
        if (StringUtils.isNotBlank(queryEndTime)) {
        	wcRequest.setEndTime(DateUtils.parseDate(queryEndTime + ":59"));
        } else {
        	wcRequest.setEndTime(DateUtils.parseDate(sdf.format(new Date())+" 23:59:59"));
            queryEndTime = sdf.format(new Date())+" 23:59";
        }
        restP.getData().put("queryStartTime", queryStartTime);
        restP.getData().put("queryEndTime", queryEndTime);
		// 是否需要汇总
		wcRequest.setNeedSummary(true);

		// 设置分页信息
		queryBase.setCurrentPage(Integer.valueOf(currentPage));
		wcRequest.setQueryBase(queryBase);
		
		String sysTraceNo = req.getParameter("sysTraceNo");
		if(sysTraceNo != null){
			wcRequest.setSysTraceNo(sysTraceNo);
		}
		
		// 设置交易类型 
		String txnType = req.getParameter("txnType");
		if(txnType==null){
			txnType="1";
		}
		if(txnType.equals("3")){
			wcRequest.setTxnType(DealType.INCOME);
		}else if(txnType.equals("2")){
			wcRequest.setTxnType(DealType.PAYOUT);
		}else{
			wcRequest.setTxnType(DealType.ALLDEALTYPE);
		}
		
		restP.getData().put("mobile", user.getMobileStar());
		restP.getData().put("member", member);
        restP.getData().put("txnType", txnType);
		// 页面请求Mapping
		restP.getData().put("pageReqMapping", "/my/pay-detail.htm");
		
		WalletCheckResponse rep = coAccountService
				.queryWalletCheckList(wcRequest);
		List<WalletCheckInfo> list = new ArrayList<WalletCheckInfo>();
		if(rep.getList()!=null){
			list=rep.getList();
		}
		// 对于不是线下充值的记录，summary置为空
		for (WalletCheckInfo tran : list) {
			if (!CommonConstant.OFFLINE_RECHARGE_PRODUCTCODE.equals(tran.getProductCode())) {
				tran.setSummary("");
			}
		}
		BigDecimal income=new BigDecimal(Double.toString(0));
		BigDecimal outpay=new BigDecimal(Double.toString(0));
		for (WalletCheckInfo tran : list) {
			if (tran.getTxnType().toString().equals("INCOME")) {
				income = (tran.getTxnAmt().getAmount()).add(income);
			}else if (tran.getTxnType().toString().equals("PAYOUT")) {
				outpay = (tran.getTxnAmt().getAmount()).add(outpay);
			}
		}
		if ((rep != null) && !"".equals(rep)) {
			restP.getData().put("balanceList", rep.getList());
			restP.getData().put("summary", rep);
			restP.getData().put("income", income);
			restP.getData().put("outpay", outpay);
			restP.getData().put("page", rep.getQueryBase());
		}
		return new ModelAndView(CommonConstant.URL_PREFIX + "/trade/PaymentDetailsRecords", "response", restP);
	}
	
	  /**
	   * 下载收支明细
	   */
	  @RequestMapping(value = "/my/pay-detail-download.htm", method = { RequestMethod.POST, RequestMethod.GET })
	  public void downloadToExcel(HttpServletRequest req, HttpServletResponse resp,
              boolean error, ModelMap model,QueryTradeForm form)throws Exception {
		  RestResponse restP = new RestResponse();
			restP.setData(new HashMap<String, Object>());
			restP.setSuccess(false);
			Map<String, String> errorMap = new HashMap<String, String>();
			EnterpriseMember member = null;
			EnterpriseMember user = getUser(req);

			// 分页
			QueryBase queryBase = new QueryBase();
			queryBase.setPageSize(50000);
			String currentPage = "1";

			checkUser(user, errorMap, restP);
			// 查询会员所以需要的信息
			member = defaultMemberService.queryCompanyMember(user, env);
			member.setAccount(accountService.queryAccountById(user.getDefaultAccountId(), env));
			WalletCheckRequest wcRequest = new WalletCheckRequest();
			wcRequest.setMemberId(user.getMemberId());
			wcRequest.setAccountNo(user.getDefaultAccountId());
			wcRequest.setClientIp(req.getRemoteAddr());
			// 账户类型
			wcRequest.setAccountType(301);
			// 交易类型(枚举)
			wcRequest.setTxnType(DealType.ALLDEALTYPE);
			// 查询时间
	        String queryStartTime = req.getParameter("queryStartTime");
	        String queryEndTime = req.getParameter("queryEndTime");
	        if (StringUtils.isNotBlank(queryStartTime)) {
	            wcRequest.setBeginTime(DateUtils.parseDate(queryStartTime + ":00"));
	        } else {
	            queryStartTime = sdf.format(DateUtil.getDateNearCurrent(-30)) + " 00:00";
	            wcRequest.setBeginTime(DateUtils.parseDate(queryStartTime + ":00"));
	        }
	        if (StringUtils.isNotBlank(queryEndTime)) {
	            wcRequest.setEndTime(DateUtils.parseDate(queryEndTime + ":59"));
	        } else {
	            wcRequest.setEndTime(DateUtils.parseDate(sdf.format(new Date())+" 23:59:59"));
	            queryEndTime = sdf.format(new Date())+" 23:59";
	        }

			// 是否需要汇总
			wcRequest.setNeedSummary(true);

			// 设置分页信息
			queryBase.setCurrentPage(Integer.valueOf(currentPage));
			wcRequest.setQueryBase(queryBase);
			
			// 设置交易类型 
			String txnType = req.getParameter("txnType");
			if(txnType==null){
				txnType="1";
			}
			if(txnType.equals("3")){
				wcRequest.setTxnType(DealType.INCOME);
			}else if(txnType.equals("2")){
				wcRequest.setTxnType(DealType.PAYOUT);
			}else{
				wcRequest.setTxnType(DealType.ALLDEALTYPE);
			}

			restP.getData().put("mobile", user.getMobileStar());
			restP.getData().put("member", member);
			restP.getData().put("queryStartTime", queryStartTime);
	        restP.getData().put("queryEndTime", queryEndTime);
	        restP.getData().put("txnType", txnType);
			// 页面请求Mapping
			restP.getData().put("pageReqMapping", "/my/pay-detail.htm");
			
			WalletCheckResponse rep = coAccountService
					.queryWalletCheckList(wcRequest);
			if ((rep != null) && !"".equals(rep)) {
				restP.getData().put("balanceList", rep.getList());
				restP.getData().put("summary", rep);
				restP.getData().put("page", rep.getQueryBase());
			}
			if(rep.getList()==null){
				List<WalletCheckInfo> transList = new ArrayList<WalletCheckInfo>();
				rep.setList(transList);
			}
			ExcelUtil excelUtil = new ExcelUtil();
			excelUtil.toExcel(req, resp,rep.getList(), 4, queryStartTime, queryEndTime);
	}
}
