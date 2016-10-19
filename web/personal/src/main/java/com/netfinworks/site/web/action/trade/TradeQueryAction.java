package com.netfinworks.site.web.action.trade;

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
import com.netfinworks.deposit.api.domain.DepositListRequest;
import com.netfinworks.deposit.api.domain.PageInfo;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.trade.FundoutQuery;
import com.netfinworks.site.domain.domain.trade.TradeRequest;
import com.netfinworks.site.domain.enums.ResourceInfo;
import com.netfinworks.site.domain.enums.TradeTypeRequest;
import com.netfinworks.site.domainservice.deposit.DefaultDepositInfoService;
import com.netfinworks.site.domainservice.trade.DefaultDownloadBillService;
import com.netfinworks.site.domainservice.trade.DefaultFundoutService;
import com.netfinworks.site.domainservice.trade.DefaultTradeQueryService;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.util.DateUtils;

/**
 *
 * <p>交易查询</p>
 * @author qinde
 * @version $Id: TradeQueryAction.java, v 0.1 2013-12-6 下午9:42:19 qinde Exp $
 */
@Controller
public class TradeQueryAction extends BaseAction {

    @Resource(name = "defaultTradeQueryService")
    private DefaultTradeQueryService  defaultTradeQueryService;

    @Resource(name = "defaultDepositInfoService")
    private DefaultDepositInfoService defaultDepositInfoService;

    @Resource(name = "defaultFundoutService")
    private DefaultFundoutService     defaultFundoutService;

    @Resource(name = "defaultDownloadBillService")
    private DefaultDownloadBillService defaultDownloadBillService;

    @Resource(name="accountService")
    private AccountService accountService;

    /**
     * 查询个人钱包用户的所有记录
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/my/all-trade.htm")
    public ModelAndView searchAll(HttpServletRequest request, HttpServletResponse resp,
                                  boolean error, ModelMap model, OperationEnvironment env)
                                                                                          throws Exception {
        String refresh=request.getParameter("refresh");
        if(StringUtils.isNotEmpty(refresh)&&refresh.equals(CommonConstant.TRUE_STRING)){
            super.updateSessionObject(request);
        }
        RestResponse restP = new RestResponse();
        String currentPage = request.getParameter("currentPage");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");

        Map<String, Object> map = new HashMap<String, Object>();
        PersonMember user = getUser(request);
        if(user.getMemberId() == null) throw  new IllegalAccessException("illegal user error!");
        //封装交易请求
        TradeRequest tradeRequest = new TradeRequest();
        tradeRequest.setMemberId(user.getMemberId());
        //设置查询时间
        if(StringUtils.isNotBlank(startDate)) {
            if(logger.isInfoEnabled())
            logger.info("查询交易的起始时间为："+ startDate);
            tradeRequest.setGmtStart(DateUtils.parseDate(startDate+ " 00:00:01"));
        } else {
            tradeRequest.setGmtStart(DateUtil.getDateNearCurrent(-30));

        }
        if(StringUtils.isNotBlank(endDate))  {
            tradeRequest.setGmtEnd(DateUtils.parseDate(endDate+ " 23:59:59"));
        } else {
            tradeRequest.setGmtEnd(DateUtil.addMinutes(new Date(), 30));
        }

        //分页信息
        QueryBase queryBase = new QueryBase();
        if (StringUtils.isBlank(currentPage)) {
            currentPage = "1";
        }
        queryBase.setCurrentPage(Integer.valueOf(currentPage));
        tradeRequest.setQueryBase(queryBase);
        user.setAccount(accountService.queryAccountById(user.getDefaultAccountId(), env));

        //查询交易记录
        PageResultList page = defaultTradeQueryService.queryTradeList(tradeRequest, env);
        map.put("page", page);
        map.put("mobile", user.getMobileStar());
        map.put("member", user);
        map.put("startDate", DateUtil.getWebDateString(tradeRequest.getGmtStart()));
        map.put("endDate", DateUtil.getWebDateString(tradeRequest.getGmtEnd()));
        restP.setData(map);
        restP.setSuccess(true);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/list/all-trade-list", "response", restP);
    }

    /**
     * 查询个人钱包用户的充值记录
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/my/all-recharge.htm")
    public ModelAndView searchAllRecharge(HttpServletRequest request, HttpServletResponse resp,
                                          boolean error, ModelMap model, OperationEnvironment env)
                                                                                                  throws Exception {
        String refresh=request.getParameter("refresh");
        if(StringUtils.isNotEmpty(refresh)&&refresh.equals(CommonConstant.TRUE_STRING)){
            super.updateSessionObject(request);
        }
        RestResponse restP = new RestResponse();
        String currentPage = request.getParameter("currentPage");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        if (StringUtils.isBlank(currentPage)) {
            currentPage = "1";
        }
        PersonMember user =  getUser(request);
        if(user.getMemberId() == null) throw  new IllegalAccessException("illegal user error!");
        user.setAccount(accountService.queryAccountById(user.getDefaultAccountId(), env));
        DepositListRequest dRequest = new DepositListRequest();
        dRequest.setMemberId(user.getMemberId());
        dRequest.setAccountNo(user.getDefaultAccountId());
        PageInfo pageInfo = new PageInfo();
        pageInfo.setCurrentPage(Integer.valueOf(currentPage));
        dRequest.setPageInfo(pageInfo);
        dRequest.setRequestId(System.currentTimeMillis() + user.getMemberId());
        if(StringUtils.isNotBlank(startDate)) {
            dRequest.setTimeBegin(DateUtils.parseDate(startDate+ " 00:00:01"));
        } else {
            dRequest.setTimeBegin(DateUtil.getDateNearCurrent(-30));

        }
        if(StringUtils.isNotBlank(endDate))  {
            dRequest.setTimeEnd(DateUtils.parseDate(endDate+ " 23:59:59"));
        } else {
            dRequest.setTimeEnd(DateUtil.addMinutes(new Date(), 30));
        }
        PageResultList page = defaultDepositInfoService.queryList(dRequest, env);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("page", page);
        restP.setData(map);
        map.put("mobile", user.getMobileStar());
        map.put("member", user);
        map.put("startDate", DateUtil.getWebDateString(dRequest.getTimeBegin()));
        map.put("endDate", DateUtil.getWebDateString(dRequest.getTimeEnd()));
        restP.setSuccess(true);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/list/recharge-list", "response",
            restP);

    }

    /**
     * 查询个人钱包用户的提现记录
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/my/all-cach.htm")
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
        PersonMember user = getUser(req);
        if(user.getMemberId() == null) throw  new IllegalAccessException("illegal user error!");
        user.setAccount(accountService.queryAccountById(user.getDefaultAccountId(), env));
        FundoutQuery fundoutQuery = new FundoutQuery();
        fundoutQuery.setMemberId(user.getMemberId());
        fundoutQuery.setAccountNo(user.getDefaultAccountId());
        fundoutQuery.setCurrentPage(Integer.valueOf(currentPage));
        if(StringUtils.isNotBlank(startDate)) {
            fundoutQuery.setOrderTimeStart(DateUtils.parseDate(startDate+ " 00:00:01"));
        } else {
            fundoutQuery.setOrderTimeStart(DateUtil.getDateNearCurrent(-30));

        }
        if(StringUtils.isNotBlank(endDate))  {
            fundoutQuery.setOrderTimeEnd(DateUtils.parseDate(endDate+ " 23:59:59"));
        } else {
            fundoutQuery.setOrderTimeEnd(DateUtil.addMinutes(new Date(), 30));
        }
        PageResultList page = defaultFundoutService.queryFundoutInfo(fundoutQuery, env);
        data.put("page", page);
        data.put("mobile", user.getMobileStar());
        data.put("member", user);
        data.put("startDate", DateUtil.getWebDateString(fundoutQuery.getOrderTimeStart()));
        data.put("endDate", DateUtil.getWebDateString(fundoutQuery.getOrderTimeEnd()));
        restP.setData(data);
        restP.setSuccess(true);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/list/cach-list", "response", restP);

    }

    /**
     * 查询个人钱包用户的退款记录
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/my/all-refund.htm")
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
        if(StringUtils.isNotBlank(startDate)) {
            tradeRequest.setGmtStart(DateUtils.parseDate(startDate+ " 00:00:01"));
        } else {
            tradeRequest.setGmtStart(DateUtil.getDateNearCurrent(-30));

        }
        if(StringUtils.isNotBlank(endDate))  {
            tradeRequest.setGmtEnd(DateUtils.parseDate(endDate+ " 23:59:59"));
        } else {
            tradeRequest.setGmtEnd(DateUtil.addMinutes(new Date(), 30));
        }
        //分页信息
        QueryBase queryBase = new QueryBase();
        if (StringUtils.isBlank(currentPage)) {
            currentPage = "1";
        }
        queryBase.setCurrentPage(Integer.valueOf(currentPage));
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
        return new ModelAndView(CommonConstant.URL_PREFIX + "/list/refund-list", "response", restP);

    }

    @RequestMapping(value = "/my/all-transfer.htm")
    public ModelAndView searchAllTransfer(HttpServletRequest request, HttpServletResponse resp,
                                          boolean error, ModelMap model, OperationEnvironment env)
                                                                                                  throws Exception {
        String refresh=request.getParameter("refresh");
        if(StringUtils.isNotEmpty(refresh)&&refresh.equals(CommonConstant.TRUE_STRING)){
            super.updateSessionObject(request);
        }
        RestResponse restP = new RestResponse();
        String currentPage = request.getParameter("currentPage");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        if (StringUtils.isBlank(currentPage)) {
            currentPage = "1";
        }
        PersonMember user = getUser(request);
        if(user.getMemberId() == null) throw  new IllegalAccessException("illegal user error!");
        user.setAccount(accountService.queryAccountById(user.getDefaultAccountId(), env));
        TradeRequest tradeRequest = new TradeRequest();

        if (StringUtils.isNotBlank(startDate)) {
            tradeRequest.setGmtStart(DateUtils.parseDate(startDate + " 00:00:01"));
        } else {
            tradeRequest.setGmtStart(DateUtil.getDateNearCurrent(-30));
        }
        if (StringUtils.isNotBlank(endDate)) {
            tradeRequest.setGmtEnd(DateUtils.parseDate(endDate + " 23:59:59"));
        } else {
            tradeRequest.setGmtEnd(DateUtil.addMinutes(new Date(), 30));
        }

        List<TradeTypeRequest> tradeTypeList = new ArrayList<TradeTypeRequest>();
        tradeTypeList.add(TradeTypeRequest.INSTANT_TRASFER);
        tradeRequest.setTradeType(tradeTypeList);
        tradeRequest.setMemberId(user.getMemberId());
        //分页信息
        QueryBase queryBase = new QueryBase();
        if (StringUtils.isBlank(currentPage)) {
            currentPage = "1";
        }
        queryBase.setCurrentPage(Integer.valueOf(currentPage));
        tradeRequest.setQueryBase(queryBase);

        PageResultList page = defaultTradeQueryService.queryTradeList(tradeRequest, env);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("page", page);
        map.put("mobile", user.getMobileStar());
        map.put("member", user);
        map.put("startDate", DateUtil.getWebDateString(tradeRequest.getGmtStart()));
        map.put("endDate", DateUtil.getWebDateString(tradeRequest.getGmtEnd()));
        restP.setData(map);
        restP.setSuccess(true);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/list/transfer-list", "response",
            restP);

    }

    /**
     * 合并付款查询
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/my/megerPay.htm")
    public ModelAndView searchMeger(HttpServletRequest req, HttpServletResponse resp,
                                    ModelMap model, OperationEnvironment env) throws Exception {
        String refresh=req.getParameter("refresh");
        if(StringUtils.isNotEmpty(refresh)&&refresh.equals(CommonConstant.TRUE_STRING)){
            super.updateSessionObject(req);
        }
        PersonMember user = getUser(req);
        if(user.getMemberId() == null) throw  new IllegalAccessException("illegal user error!");
        user.setAccount(accountService.queryAccountById(user.getDefaultAccountId(), env));
        String currentPage = req.getParameter("currentPage");
        String startDate = req.getParameter("startDate");
        String endDate = req.getParameter("endDate");
        if (StringUtils.isBlank(currentPage)) {
            currentPage = "1";
        }
        Map<String, Object> data = null;
        Map<String, String> error = new HashMap<String, String>();
        RestResponse restP = new RestResponse();
        checkUser(user, error, restP);
        TradeRequest tradeRequest = new TradeRequest();
        List<String> tradeStatus = new ArrayList<String>();
        tradeStatus.add(WAIT_BUYER_PAY);
        tradeStatus.add(WAIT_BUYER_PAY_BANK);
        tradeRequest.setTradeStatus(tradeStatus);
        tradeRequest.setBuyerId(user.getMemberId());
        //分页信息
        QueryBase queryBase = new QueryBase();
        if (StringUtils.isBlank(currentPage)) {
            currentPage = "1";
        }
        queryBase.setCurrentPage(Integer.valueOf(currentPage));
        tradeRequest.setQueryBase(queryBase);

        if (StringUtils.isNotBlank(startDate)) {
            tradeRequest.setGmtStart(DateUtils.parseDate(startDate + " 00:00:01"));
        } else {
            tradeRequest.setGmtStart(DateUtil.getDateNearCurrent(-30));
        }
        if (StringUtils.isNotBlank(endDate)) {
            tradeRequest.setGmtEnd(DateUtils.parseDate(endDate + " 23:59:59"));
        } else {
            tradeRequest.setGmtEnd(DateUtil.addMinutes(new Date(), 30));
        }
        PageResultList page = defaultTradeQueryService.queryTradeList(tradeRequest, env);
        data = new HashMap<String, Object>();
        data.put("mobile", user.getMobileStar());
        data.put("member", user);
        data.put("page", page);
        data.put("startDate", DateUtil.getWebDateString(tradeRequest.getGmtStart()));
        data.put("endDate", DateUtil.getWebDateString(tradeRequest.getGmtEnd()));
        restP.setData(data);
        return new ModelAndView(CommonConstant.URL_PREFIX + ResourceInfo.MEGER_PAY.getUrl(),
            "response", restP);
    }

}
