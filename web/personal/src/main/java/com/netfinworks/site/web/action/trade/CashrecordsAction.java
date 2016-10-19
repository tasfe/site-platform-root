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

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.fos.Fundout;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.trade.FundoutQuery;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domainservice.trade.DefaultFundoutService;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.util.DateUtils;
import com.netfinworks.site.web.util.ExcelUtil;
/**
 * 查询个人钱包用户的提现记录
 *
 * @param model
 * @param request
 * @return
 */
@Controller
public class CashrecordsAction extends BaseAction {
	
    @Resource(name = "defaultFundoutService")
    private DefaultFundoutService     defaultFundoutService;

    @Resource(name="accountService")
    private AccountService accountService;
    
    private SimpleDateFormat           sdf = new SimpleDateFormat("yyyy-MM-dd");
	
    @RequestMapping(value = "/my/all-cach1.htm")
    public ModelAndView searchAllCach(HttpServletRequest req, HttpServletResponse resp,
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
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("startDate", startDate);
        data.put("endDate", endDate);
        PersonMember user = getUser(req);
        if(user.getMemberId() == null) throw  new IllegalAccessException("illegal user error!");
        user.setAccount(accountService.queryAccountById(user.getDefaultAccountId(), env));
        FundoutQuery fundoutQuery = new FundoutQuery();
        fundoutQuery.setProductCode(TradeType.WITHDRAW.getBizProductCode()
				+ "," + TradeType.WITHDRAW_INSTANT.getBizProductCode());
        fundoutQuery.setMemberId(user.getMemberId());
        fundoutQuery.setAccountNo(user.getDefaultAccountId());
        fundoutQuery.setCurrentPage(Integer.valueOf(currentPage));
        if(StringUtils.isNotBlank(startDate)) {
            fundoutQuery.setOrderTimeStart(DateUtils.parseDate(startDate+ ":00"));
        } else {
        	startDate=sdf.format(DateUtil.getDateNearCurrent(-30));
        	fundoutQuery.setOrderTimeStart(DateUtils.parseDate(startDate.substring(0,10)+ " 00:00:01"));
        	startDate = DateUtils.getDateString()+" 00:00";
        }
        if(StringUtils.isNotBlank(endDate))  {
            fundoutQuery.setOrderTimeEnd(DateUtils.parseDate(endDate+ ":59"));
        } else {
        	fundoutQuery.setOrderTimeEnd(new Date());
        	endDate = sdf.format(new Date())+" 23:59";
        }
        PageResultList page = defaultFundoutService.queryFundoutInfo(fundoutQuery, env);
        data.put("page", page);
        data.put("mobile", user.getMobileStar());
        data.put("member", user);
        restP.setData(data);
        restP.setSuccess(true);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/trade/cashrecords", "response", restP);

    }
    
    @RequestMapping(value = "/my/all-cach-download.htm")
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
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("startDate", startDate);
        data.put("endDate", endDate);
        PersonMember user = getUser(req);
        if(user.getMemberId() == null) throw  new IllegalAccessException("illegal user error!");
        user.setAccount(accountService.queryAccountById(user.getDefaultAccountId(), env));
        FundoutQuery fundoutQuery = new FundoutQuery();
        fundoutQuery.setProductCode(TradeType.WITHDRAW.getBizProductCode()
                + "," + TradeType.WITHDRAW_INSTANT.getBizProductCode());
        fundoutQuery.setMemberId(user.getMemberId());
        fundoutQuery.setAccountNo(user.getDefaultAccountId());
        fundoutQuery.setCurrentPage(Integer.valueOf(currentPage));
        if(StringUtils.isNotBlank(startDate)) {
            fundoutQuery.setOrderTimeStart(DateUtils.parseDate(startDate+ ":00"));
        } else {
        	startDate=sdf.format(DateUtil.getDateNearCurrent(-30));
        	fundoutQuery.setOrderTimeStart(DateUtils.parseDate(startDate.substring(0,10)+ " 00:00:01"));
        	startDate = DateUtils.getDateString()+" 00:00";
        }
        if(StringUtils.isNotBlank(endDate))  {
            fundoutQuery.setOrderTimeEnd(DateUtils.parseDate(endDate+ ":59"));
        } else {
        	fundoutQuery.setOrderTimeEnd(new Date());
        	endDate = sdf.format(new Date())+" 23:59";
        }
        fundoutQuery.setPageSize(50000);
        PageResultList page = defaultFundoutService.queryFundoutInfo(fundoutQuery, env);
        data.put("page", page);
        data.put("mobile", user.getMobileStar());
        data.put("member", user);
        restP.setData(data);
        restP.setSuccess(true);
        if(page.getInfos()==null)
		{
			List<Fundout> fundout = new ArrayList<Fundout>();
			page.setInfos(fundout);
		}
        ExcelUtil excelUtil = new ExcelUtil();
		excelUtil.toExcel(req, resp,page.getInfos(), 3, startDate, endDate);
    }
}
