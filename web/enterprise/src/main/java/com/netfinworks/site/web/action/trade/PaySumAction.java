package com.netfinworks.site.web.action.trade;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
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

@Controller
public class PaySumAction extends BaseAction{
	@Resource(name = "coAccountService")
	private CoAccountService coAccountService;


	@Resource(name = "defaultMemberService")
	private DefaultMemberService defaultMemberService;
	
	@Resource(name="accountService")
    private AccountService accountService;
	
    private SimpleDateFormat           sdf = new SimpleDateFormat("yyyy-MM-dd");
	/**
	 * 进入收支汇总页面
	 * 
	 * @return
	 */
	@RequestMapping(value = "/my/pay-summary.htm")
	public ModelAndView searchAll(HttpServletRequest req,
			HttpServletResponse resp, boolean error, ModelMap model,
			QueryTradeForm form) throws Exception {
		RestResponse restP = new RestResponse();
		restP.setData(new HashMap<String,Object>());
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
		wcRequest.setAccountType(201);
		// 交易类型(枚举)
		wcRequest.setTxnType(DealType.ALLDEALTYPE);
		// 查询时间
        String queryStartTime = form.getQueryStartTime();
        String queryEndTime = form.getQueryEndTime();
        if (StringUtils.isNotBlank(queryStartTime)) {
        	wcRequest.setBeginTime(DateUtils.parseDate(queryStartTime + " 00:00:00"));
        } else {
        	wcRequest.setBeginTime(DateUtil.getDateNearCurrent(-30));
            queryStartTime = DateUtils.getDateString();

        }
        if (StringUtils.isNotBlank(queryEndTime)) {
        	wcRequest.setEndTime(DateUtils.parseDate(queryEndTime + " 23:59:59"));
        } else {
        	wcRequest.setEndTime(new Date());
            queryEndTime = sdf.format(new Date());
        }

		// 是否需要汇总
		wcRequest.setNeedSummary(true);
		
		// 设置分页信息
		queryBase.setCurrentPage(Integer.valueOf(currentPage));
		wcRequest.setQueryBase(queryBase);

		restP.getData().put("mobile", user.getMobileStar());
		restP.getData().put("member", member);
		restP.getData().put("queryStartTime", queryStartTime);
        restP.getData().put("queryEndTime", queryEndTime);
		// 页面请求Mapping
		restP.getData().put("pageReqMapping", "/my/pay-summary.htm");
		
		WalletCheckResponse rep = coAccountService
				.queryWalletCheckList(wcRequest);
		if (rep != null && !"".equals(rep)) {
			restP.getData().put("balanceList", rep.getList());
			restP.getData().put("summary", rep);
			restP.getData().put("page", rep.getQueryBase());
		}
		return new ModelAndView(CommonConstant.URL_PREFIX + "/trade/PaymentSummaryRecords");
	}
}
