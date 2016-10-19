package com.netfinworks.site.web.action.settlement;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import com.netfinworks.site.domain.domain.response.CoTradeQueryResponse;
import com.netfinworks.site.domain.domain.trade.CoTradeRequest;
import com.netfinworks.site.domainservice.spi.DefaultMemberService;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.netfinworks.site.ext.integration.trade.impl.SettlementServiceImpl;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.action.form.QueryTradeForm;
import com.netfinworks.site.web.common.util.FormatForDate;

/**
 *
 * <p>
 * 结算对账action
 * </p>
 *
 * @author zhangyun.m
 * @version $Id: SettlementAction.java, v 0.1 2013-11-27 上午10:58:45 Guanxiaoxu
 *          Exp $
 */
@Controller
public class SettlementAction extends BaseAction {

	@Resource(name = "enterpriseTradeService")
	private SettlementServiceImpl settlementServiceImpl;

	@Resource(name = "defaultMemberService")
	private DefaultMemberService defaultMemberService;

    @Resource(name="accountService")
    private AccountService accountService;

	@RequestMapping(value = "/my/all-settlement.htm", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView searchAll(HttpServletRequest req,
			HttpServletResponse resp, boolean error, ModelMap model,
			QueryTradeForm form) throws Exception {
		EnterpriseMember member = null;
		CoTradeQueryResponse rep = null;
		RestResponse restP = new RestResponse();
		restP.setData(new HashMap<String, Object>());
		restP.setSuccess(false);
		FormatForDate formatForDate = new FormatForDate();
		Date fristDay = null;
		Date lastDay = null;
		QueryBase queryBase = new QueryBase();
		String currentPage = form.getCurrentPage();
		if (StringUtils.isBlank(currentPage)) {
			currentPage = "1";
		}
		// 查询时间
		String queryTime = null;

		Map<String, String> errorMap = new HashMap<String, String>();

		HttpSession session = req.getSession();
		EnterpriseMember user = getUser(req);
		checkUser(user, errorMap, restP);
		// 查询会员所以需要的信息
		member = defaultMemberService.queryCompanyMember(user, env);
		member.setAccount(accountService.queryAccountById(user.getDefaultAccountId(), env));
		// 1.查询请求：结算对账单流水明细
		CoTradeRequest listSreq = new CoTradeRequest();
		// 2.查询请求：汇总
		CoTradeRequest sumSreq = new CoTradeRequest();

		String memberId = user.getMemberId();
		listSreq.setMemberId(memberId);
		sumSreq.setMemberId(memberId);
		// 分页
		queryBase.setCurrentPage(Integer.valueOf(currentPage));
		listSreq.setQueryBase(queryBase);
		// 需要汇总 (汇总请求 querySummary ，不在用 queryList里的汇总，所以这里值为fasle)
		listSreq.setNeedSummary(false);
		// 是否发生过结算
		listSreq.setHasSettled(true);
		sumSreq.setHasSettled(true);
		// 交易状态
		List<String> tradeStatus = new ArrayList<String>();
		String[] sumTradeStatus = new String[] { "401", "201", "301", "951",
				"901" };
		tradeStatus = Arrays.asList(sumTradeStatus);
		sumSreq.setStatus(tradeStatus);

		queryTime = form.getQueryTime();
		if (StringUtils.isBlank(queryTime)) {
			// 默认：开始时间：当月第一天（零点零分零秒开始）
			fristDay = formatForDate.getFirstDayOfMonth();
			listSreq.setBeginTime(fristDay);
			// 默认：结束时间：当月请求时间
			lastDay = new Date();
			listSreq.setEndTime(lastDay);

			sumSreq.setBeginTime(fristDay);
			sumSreq.setEndTime(lastDay);
			// 默认查询时间
			queryTime = new SimpleDateFormat("yyyy-MM").format(lastDay);
		} else {
			String[] yearAndMonth = queryTime.split("-");
			if (yearAndMonth != null && yearAndMonth.length != 0) {
				int gYear = Integer.parseInt(yearAndMonth[0]);
				int gMonth = Integer.parseInt(yearAndMonth[1]);
				// 获取指定年月的第一天（零点零分零秒开始）
				fristDay = formatForDate.getFirstDayOfMonthD(gYear, gMonth);
				// 获取指定年月的最后一天（23点59分59秒结束）
				lastDay = formatForDate.getLastDayOfMonthD(gYear, gMonth);
				listSreq.setBeginTime(fristDay);
				listSreq.setEndTime(lastDay);

				sumSreq.setBeginTime(fristDay);
				sumSreq.setEndTime(lastDay);
			}
		}

		restP.getData().put("mobile", user.getMobileStar());
		restP.getData().put("member", member);
		restP.getData().put("queryTime", queryTime);
		// 页面请求Mapping
		restP.getData().put("pageReqMapping", "/my/all-settlement.htm");
		restP.getData().put("fristDay", fristDay);
		restP.getData().put("lastDay", lastDay);

		// 查询结算流水、汇总信息
		rep = settlementServiceImpl.queryList(listSreq, sumSreq);

		restP.setSuccess(true);

		if (rep != null) {
			restP.getData().put("settleList", rep.getBaseInfoList());
			restP.getData().put("summary", rep.getSummaryInfo());
			restP.getData().put("page", rep.getQueryBase());
		}
		return new ModelAndView(CommonConstant.URL_PREFIX
				+ "/settlement/settlement", "response", restP);

	}

}
