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
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.info.TradeInfo;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.trade.TradeRequest;
import com.netfinworks.site.domainservice.trade.DefaultTradeQueryService;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.util.DateUtils;
import com.netfinworks.site.web.util.ExcelUtil;

@Controller
public class RefundRecordsAction extends BaseAction{
	@Resource(name = "defaultTradeQueryService")
		private DefaultTradeQueryService  defaultTradeQueryService;
	
	@Resource(name="accountService")
	   private AccountService accountService;
	
	private SimpleDateFormat           sdf = new SimpleDateFormat("yyyy-MM-dd");
	/**
     * 查询个人钱包用户的退款记录
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/my/all-refund1.htm")
    public ModelAndView searchAllRefund(HttpServletRequest req, HttpServletResponse resp,
                                        boolean error, ModelMap model, OperationEnvironment env)
                                                                                                throws Exception {
        String refresh=req.getParameter("refresh");
        if(StringUtils.isNotEmpty(refresh)&&refresh.equals(CommonConstant.TRUE_STRING)){
            super.updateSessionObject(req);
        }
        RestResponse restP = new RestResponse();
        String currentPage = req.getParameter("currentPage");
        String startDate = req.getParameter("startDate");
        String endDate = req.getParameter("endDate");
        if (StringUtils.isBlank(currentPage)) {
            currentPage = "1";
        }

        PersonMember user = getUser(req);
        if(user.getMemberId() == null) throw  new IllegalAccessException("illegal user error!");
        user.setAccount(accountService.queryAccountById(user.getDefaultAccountId(), env));
        TradeRequest tradeRequest = new TradeRequest();
        tradeRequest.setBuyerId(user.getMemberId());
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        if(StringUtils.isNotBlank(startDate)) {
        	tradeRequest.setGmtStart(DateUtils.parseDate(startDate+ ":00"));
        } else {
        	startDate=sdf.format(DateUtil.getDateNearCurrent(-30));
        	tradeRequest.setGmtStart(DateUtils.parseDate(startDate.substring(0,10)+ " 00:00:01"));
        	startDate = DateUtils.getDateString()+" 00:00";
        }
        if(StringUtils.isNotBlank(endDate))  {
        	tradeRequest.setGmtEnd(DateUtils.parseDate(endDate+ ":59"));
        } else {
        	tradeRequest.setGmtEnd(new Date());
        	endDate = sdf.format(new Date())+" 23:59";
        }
        //分页信息
        QueryBase queryBase = new QueryBase();
        if (StringUtils.isBlank(currentPage)) {
            currentPage = "1";
        }
        queryBase.setCurrentPage(Integer.valueOf(currentPage));
        tradeRequest.setQueryBase(queryBase);

        PageResultList page = defaultTradeQueryService.queryRefundList(tradeRequest, env);
        map.put("page", page);
        map.put("mobile", user.getMobileStar());
        map.put("member", user);
        restP.setData(map);
        restP.setSuccess(true);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/trade/refundrecords", "response", restP);

    }
    
    @RequestMapping(value = "/my/all-refund-download.htm")
    public void download(HttpServletRequest req, HttpServletResponse resp,
                                        boolean error, ModelMap model, OperationEnvironment env)
                                                                                                throws Exception {
        String refresh=req.getParameter("refresh");
        if(StringUtils.isNotEmpty(refresh)&&refresh.equals(CommonConstant.TRUE_STRING)){
            super.updateSessionObject(req);
        }
        RestResponse restP = new RestResponse();
        String currentPage = "1";
        String startDate = req.getParameter("startDate");
        String endDate = req.getParameter("endDate");

        PersonMember user = getUser(req);
        if(user.getMemberId() == null) throw  new IllegalAccessException("illegal user error!");
        user.setAccount(accountService.queryAccountById(user.getDefaultAccountId(), env));
        TradeRequest tradeRequest = new TradeRequest();
        tradeRequest.setBuyerId(user.getMemberId());
        if(StringUtils.isNotBlank(startDate)) {
        	tradeRequest.setGmtStart(DateUtils.parseDate(startDate+ ":00"));
        } else {
        	startDate=sdf.format(DateUtil.getDateNearCurrent(-30));
        	tradeRequest.setGmtStart(DateUtils.parseDate(startDate.substring(0,10)+ " 00:00:01"));
        	startDate = DateUtils.getDateString()+" 00:00";
        }
        if(StringUtils.isNotBlank(endDate))  {
        	tradeRequest.setGmtEnd(DateUtils.parseDate(endDate+ ":59"));
        } else {
        	tradeRequest.setGmtEnd(new Date());
        	endDate = sdf.format(new Date())+" 23:59";
        }
        //分页信息
        QueryBase queryBase = new QueryBase();
        queryBase.setCurrentPage(Integer.valueOf(currentPage));
        queryBase.setPageSize(50000);
        tradeRequest.setQueryBase(queryBase);

        PageResultList page = defaultTradeQueryService.queryRefundList(tradeRequest, env);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("page", page);
        map.put("mobile", user.getMobileStar());
        map.put("member", user);
        map.put("startDate", DateUtil.getWebDateString(tradeRequest.getGmtStart()));
        map.put("endDate", DateUtil.getWebDateString(tradeRequest.getGmtEnd()));
        restP.setData(map);
        restP.setSuccess(true);
        if(page.getInfos()==null)
		{
			List<TradeInfo>  tradeInfo=new ArrayList<TradeInfo>();
			page.setInfos(tradeInfo);
		}
        ExcelUtil excelUtil = new ExcelUtil();
		excelUtil.toExcel1(req, resp,page.getInfos(),user.getMemberId(), 6, startDate, endDate);
    }
}
