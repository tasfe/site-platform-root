package com.netfinworks.site.web.action.trade;

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
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.response.CoTradeQueryResponse;
import com.netfinworks.site.domain.domain.trade.CoTradeRequest;
import com.netfinworks.site.domain.domain.trade.TradeRequest;
import com.netfinworks.site.domainservice.trade.DefaultTradeQueryService;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.netfinworks.site.ext.integration.trade.TradeService;
import com.netfinworks.site.web.action.common.BaseAction;
@Controller
public class RefundDetailAction extends BaseAction{
	
	@Resource(name = "defaultTradeQueryService")
	private DefaultTradeQueryService defaultTradeQueryService;
	
	@Resource(name = "accountService")
	private AccountService accountService;
	
	@Resource(name = "tradeService")
	private TradeService tradeService;
	
	@RequestMapping(value = "/my/refundDetail.htm")
	public ModelAndView searchRefundDetail(HttpServletRequest request,
			HttpServletResponse resp, boolean error, ModelMap model,
			OperationEnvironment env) throws Exception {
		String refresh = request.getParameter("refresh");// 1
		if (StringUtils.isNotEmpty(refresh)
				&& refresh.equals(CommonConstant.TRUE_STRING)) {// 2
			super.updateSessionObject(request);// 2
		}
		RestResponse restP = new RestResponse();// zzq返回数据封装 // 3
		String tradeVoucherNo = request.getParameter("tradeVoucherNo");// 1
		String tradeSourceVoucherNo = request
				.getParameter("tradeSourceVoucherNo");
		PersonMember user = getUser(request);// 8、
		restP.setData(new HashMap<String, Object>());// 6、Map<String, Object>
		Map<String, String> errorMap = new HashMap<String, String>();// 7、
		checkUser(user, errorMap, restP);// 9、

		if (user.getMemberId() == null) {
			throw new IllegalAccessException("illegal user error!");
		}
		TradeRequest tradeRequest = new TradeRequest();
		tradeRequest.setGmtStart(DateUtil.getDateNearCurrent(-366));// 参照查询退款接口默认时间
		tradeRequest.setGmtEnd(DateUtil.addMinutes(new Date(), 30));
		// 分页信息
		QueryBase queryBase5 = new QueryBase();
		queryBase5.setCurrentPage(1);
		tradeRequest.setQueryBase(queryBase5);
		tradeRequest.setTradeVoucherNo(tradeVoucherNo);
		PageResultList page = defaultTradeQueryService.queryRefundList(
				tradeRequest, env);
		restP.getData().put("page", page);
		restP.getData().put("mobile", user.getMobileStar());
		restP.getData().put("member", user);
		restP.setSuccess(true);

//		// 查询订单详情
//		CoTradeRequest breq = new CoTradeRequest();// zzq查询条件封装
//		breq.setQueryBase(queryBase5);
//		breq.setNeedSummary(true);// 需要汇总
//		// breq.setMemberId(user.getMemberId());
//		breq.setTradeVoucherNo(tradeSourceVoucherNo);
//		breq.setBeginTime(DateUtil.getDateNearCurrent(-366));
//		breq.setEndTime(new Date());
//		CoTradeQueryResponse rep = tradeService.queryList(breq);
//		if (rep != null) {
//			restP.getData().put("list", rep);
//		}
		return new ModelAndView(CommonConstant.URL_PREFIX
				+ "/trade/RefundDetail", "response", restP);
	}
	
}
