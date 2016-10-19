package com.netfinworks.site.web.action.walletCheck;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.biz.common.util.QueryBase;
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
import com.netfinworks.site.web.common.util.FormatForDate;

/**
 *
 * <p>
 * 钱包对账
 * </p>
 *
 * @author Guan Xiaoxu
 * @version $Id: WalletCheckAction.java, v 0.1 2013-11-27 上午11:01:59 Guanxiaoxu
 *          Exp $
 */
@Controller
public class WalletCheckAction extends BaseAction {

	@Resource(name = "coAccountService")
	private CoAccountService coAccountService;

	@Resource(name = "defaultMemberService")
	private DefaultMemberService defaultMemberService;

    @Resource(name="accountService")
    private AccountService accountService;

	@RequestMapping(value = "/my/all-walletCheck.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView searchAll(HttpServletRequest req,
			HttpServletResponse resp, boolean error, ModelMap model,
			QueryTradeForm form) throws Exception {
		RestResponse restP = new RestResponse();
		restP.setData(new HashMap<String, Object>());
		restP.setSuccess(false);
		FormatForDate formatForDate = new FormatForDate();
		Map<String, String> errorMap = new HashMap<String, String>();
		EnterpriseMember member = null;
		HttpSession session = req.getSession();
		EnterpriseMember user = getUser(req);

		// 分页
		QueryBase queryBase = new QueryBase();
		String currentPage = form.getCurrentPage();
		if (StringUtils.isBlank(currentPage)) {
			currentPage = "1";
		}
		// 查询时间
		String queryTime = null;

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

		queryTime = form.getQueryTime();
		if (StringUtils.isBlank(queryTime)) {
			// 默认：开始时间：当月第一天（零点零分零秒开始）
			wcRequest.setBeginTime(formatForDate.getFirstDayOfMonth());
			// 默认：结束时间：当月请求时间
			wcRequest.setEndTime(new Date());
			// 默认：当月上一个月的最后一天（用于计算上月余额，精确到天即可）
			wcRequest.setLmlday(formatForDate.getPreviousMonthEndD());
			// 默认查询时间
			queryTime = new SimpleDateFormat("yyyy-MM").format(new Date());
		} else {
			String[] yearAndMonth = queryTime.split("-");
			if (yearAndMonth != null && yearAndMonth.length != 0) {
				int gYear = Integer.parseInt(yearAndMonth[0]);
				int gMonth = Integer.parseInt(yearAndMonth[1]);
				// 获取指定年月的第一天（零点零分零秒开始）
				Date fristDay = formatForDate
						.getFirstDayOfMonthD(gYear, gMonth);
				// 获取指定年月的最后一天（23点59分59秒结束）
				Date lastDay = formatForDate.getLastDayOfMonthD(gYear, gMonth);
				// 获取指定年月上一个月的最后一天（用于计算上月余额，精确到天即可）
				Date preMonthLastDay = formatForDate.getLastDayOfMonthD(gYear,
						gMonth - 1);
				wcRequest.setBeginTime(fristDay);
				wcRequest.setEndTime(lastDay);
				wcRequest.setLmlday(preMonthLastDay);
			}
		}

		// 是否需要汇总
		wcRequest.setNeedSummary(true);

		// 设置分页信息
		queryBase.setCurrentPage(Integer.valueOf(currentPage));
		wcRequest.setQueryBase(queryBase);

		restP.getData().put("mobile", user.getMobileStar());
		restP.getData().put("member", member);
		restP.getData().put("queryTime", queryTime);
		// 页面请求Mapping
		restP.getData().put("pageReqMapping", "/my/all-walletCheck.htm");

		WalletCheckResponse rep = coAccountService
				.queryWalletCheckList(wcRequest);
		if (rep != null && !"".equals(rep)) {
			restP.getData().put("balanceList", rep.getList());
			restP.getData().put("summary", rep);
			restP.getData().put("page", rep.getQueryBase());
		}
		restP.setSuccess(true);
		return new ModelAndView(CommonConstant.URL_PREFIX
				+ "/walletCheck/wallet_check", "response", restP);

	}

}
