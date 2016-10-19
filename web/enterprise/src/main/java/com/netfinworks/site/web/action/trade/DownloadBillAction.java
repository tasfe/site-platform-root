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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.trade.DownloadBillRequest;
import com.netfinworks.site.domainservice.trade.DefaultDownloadBillService;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.site.web.util.CsvUtils;
import com.netfinworks.site.web.util.DateUtils;
import com.netfinworks.site.web.util.ExcelUtil;

/**
 *
 * <p>对账单下载</p>
 * @author Guan Xiaoxu
 * @version $Id: DownloadBillAction.java, v 0.1 2013-12-10 下午4:08:33 Guanxiaoxu Exp $
 */
@Controller
public class DownloadBillAction extends BaseAction {
    @Resource(name = "defaultDownloadBillService")
    private DefaultDownloadBillService defaultDownloadBillService;

    @Resource(name="accountService")
    private AccountService accountService;
    private SimpleDateFormat           sdf = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 账单下载首页   支付流水
     * @param req
     * @param resp
     * @param env
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/my/downloadBill.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    public ModelAndView queryPayWater(HttpServletRequest req, HttpServletResponse resp,
                                      OperationEnvironment env) throws Exception {
        RestResponse response = new RestResponse();
        DownloadBillRequest reqInfo = new DownloadBillRequest();
        String currentPage = req.getParameter("currentPage");
        String startDate = req.getParameter("startDate");
        String endDate = req.getParameter("endDate");
        if (StringUtils.isBlank(currentPage)) {
            currentPage = "1";
        }
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

        EnterpriseMember user = getUser(req);
        user.setAccount(accountService.queryAccountById(user.getDefaultAccountId(), env));
        //设置当前页
        QueryBase queryBase = new QueryBase();
        queryBase.setCurrentPage(Integer.parseInt(currentPage));
        reqInfo.setQueryBase(queryBase);
        reqInfo.setTradeStatus(getStateList(1));
        reqInfo.setMemberId(user.getMemberId());
        PageResultList pageList = defaultDownloadBillService.queryPayWater(reqInfo, env);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("page", pageList);
        map.put("member", user);
        map.put("mobile", user.getMobileStar());
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        response.setData(map);
        response.setSuccess(true);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/downloadBill/paywater-list",
            "response", response);

    }

    /**
     * 订单结算
     * @param req
     * @param resp
     * @param env
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/my/all-orderSettle.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    public ModelAndView queryOrderSettle(HttpServletRequest req, HttpServletResponse resp,
                                         OperationEnvironment env) throws Exception {
        RestResponse response = new RestResponse();
        DownloadBillRequest reqInfo = new DownloadBillRequest();
        String currentPage = req.getParameter("currentPage");
        String startDate = req.getParameter("startDate");
        String endDate = req.getParameter("endDate");
        if (StringUtils.isBlank(currentPage)) {
            currentPage = "1";
        }
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

        EnterpriseMember user = getUser(req);
        user.setAccount(accountService.queryAccountById(user.getDefaultAccountId(), env));
        //设置当前页
        QueryBase queryBase = new QueryBase();
        queryBase.setCurrentPage(Integer.parseInt(currentPage));
        reqInfo.setQueryBase(queryBase);
        reqInfo.setTradeStatus(getStateList(2));
        reqInfo.setMemberId(user.getMemberId());
        PageResultList pageList = defaultDownloadBillService.queryOrderSettle(reqInfo, env);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("page", pageList);
        map.put("member", user);
        map.put("mobile", user.getMobileStar());
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        response.setData(map);
        response.setSuccess(true);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/downloadBill/ordersettle-list",
            "response", response);

    }

    /**
     * 退款流水
     * @param req
     * @param resp
     * @param env
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/my/all-refundWater.htm", method = { RequestMethod.POST,
            RequestMethod.GET })
    public ModelAndView queryRefundWater(HttpServletRequest req, HttpServletResponse resp,
                                         OperationEnvironment env) throws Exception {
        RestResponse response = new RestResponse();
        DownloadBillRequest reqInfo = new DownloadBillRequest();
        String currentPage = req.getParameter("currentPage");
        String startDate = req.getParameter("startDate");
        String endDate = req.getParameter("endDate");
        if (StringUtils.isBlank(currentPage)) {
            currentPage = "1";
        }
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

        EnterpriseMember user = getUser(req);
        user.setAccount(accountService.queryAccountById(user.getDefaultAccountId(), env));
        //设置当前页
        QueryBase queryBase = new QueryBase();
        queryBase.setCurrentPage(Integer.parseInt(currentPage));
        reqInfo.setQueryBase(queryBase);
        reqInfo.setTradeStatus(getStateList(3));
        reqInfo.setMemberId(user.getMemberId());
        PageResultList pageList = defaultDownloadBillService.queryRefundWater(reqInfo, env);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("page", pageList);
        map.put("member", user);
        map.put("mobile", user.getMobileStar());
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        response.setData(map);
        response.setSuccess(true);
        return new ModelAndView(CommonConstant.URL_PREFIX + "/downloadBill/refund-list",
            "response", response);

    }

    /**
     * 下载操作 导出csv
     * @param request
     * @param response
     * @param env
     * @throws Exception
     */
    @RequestMapping(value = "/my/downloadToCsv.htm", method = RequestMethod.GET)
    public void downloadToCsv(HttpServletRequest request, HttpServletResponse response,
                              OperationEnvironment env) throws Exception {
        EnterpriseMember user = getUser(request);

        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        int type = Integer.parseInt(request.getParameter("queryType"));
        DownloadBillRequest reqInfo = new DownloadBillRequest();
        if (StringUtils.isNotBlank(startDate)) {
            reqInfo.setBeginTime(DateUtils.parseDate(startDate + " 00:00:01"));
        } else {
            reqInfo.setBeginTime(DateUtil.getDateNearCurrent(-30));
        }
        if (StringUtils.isNotBlank(endDate)) {
            reqInfo.setEndTime(DateUtils.parseDate(endDate + " 23:59:59"));
        } else {
            reqInfo.setEndTime(new Date());
        }
        QueryBase queryBase = new QueryBase();
        queryBase.setNeedQeryTotal(true);
        reqInfo.setQueryBase(queryBase);
        reqInfo.setMemberId(user.getMemberId());
        PageResultList pageList = null;
        switch (type) {
            case 1:
                reqInfo.setTradeStatus(getStateList(1));
                pageList = defaultDownloadBillService.queryPayWater(reqInfo, env);
                CsvUtils.createCSVFile(request, response, pageList.getInfos(), 1, startDate,
                    endDate);
                break;
            case 2:
                reqInfo.setTradeStatus(getStateList(2));
                pageList = defaultDownloadBillService.queryOrderSettle(reqInfo, env);
                CsvUtils.createCSVFile(request, response, pageList.getInfos(), 2, startDate,
                    endDate);
                break;
            case 3:
                reqInfo.setTradeStatus(getStateList(3));
                pageList = defaultDownloadBillService.queryRefundWater(reqInfo, env);
                CsvUtils.createCSVFile(request, response, pageList.getInfos(), 3, startDate,
                    endDate);
                break;
            default:
                logger.error("[账单下载时，上传参数有误]"+reqInfo.toString());
                break;
        }

    }

    /**
     * 下载操作 导出Excel
     * @param request
     * @param response
     * @param env
     * @throws Exception
     */
    @RequestMapping(value = "/my/downloadToExcel.htm", method = RequestMethod.GET)
    public void downloadToExcel(HttpServletRequest request, HttpServletResponse response,
                                OperationEnvironment env) throws Exception {
        EnterpriseMember user = getUser(request);

        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        int type = Integer.parseInt(request.getParameter("queryType"));
        DownloadBillRequest reqInfo = new DownloadBillRequest();
        if (StringUtils.isNotBlank(startDate)) {
            reqInfo.setBeginTime(DateUtils.parseDate(startDate + " 00:00:01"));
        } else {
            reqInfo.setBeginTime(DateUtil.getDateNearCurrent(-30));
        }
        if (StringUtils.isNotBlank(endDate)) {
            reqInfo.setEndTime(DateUtils.parseDate(endDate + " 23:59:59"));
        } else {
            reqInfo.setEndTime(new Date());
        }
        QueryBase queryBase = new QueryBase();
        queryBase.setNeedQeryTotal(true);
        reqInfo.setQueryBase(queryBase);
        reqInfo.setMemberId(user.getMemberId());
        PageResultList pageList = null;
        ExcelUtil excelUtil = new ExcelUtil();
        switch (type) {
            case 1:
                reqInfo.setTradeStatus(getStateList(1));
                pageList = defaultDownloadBillService.queryPayWater(reqInfo, env);
                excelUtil.toExcel(request, response, pageList.getInfos(), 1, startDate, endDate);
                break;
            case 2:
                reqInfo.setTradeStatus(getStateList(2));
                pageList = defaultDownloadBillService.queryOrderSettle(reqInfo, env);
                excelUtil.toExcel(request, response, pageList.getInfos(), 2, startDate, endDate);
                break;
            case 3:
                reqInfo.setTradeStatus(getStateList(3));
                pageList = defaultDownloadBillService.queryRefundWater(reqInfo, env);
                excelUtil.toExcel(request, response, pageList.getInfos(), 3, startDate, endDate);
                break;
            default:
                logger.error("[账单下载时，上传参数有误]");
                break;
        }

    }

    private List<String> getStateList(int p) {
        List<String> list = new ArrayList<String>();
        switch (p) {
            case 1:
                list.add("201");
                list.add("301");
                list.add("401");
                break;
            case 2:
                list.add("401");
                break;
            case 3:
                list.add("");
                break;
            default:
                logger.info("输入的状态有误");
                break;
        }

        return list;
    }

}
