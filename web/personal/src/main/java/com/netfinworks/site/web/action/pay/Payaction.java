/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.web.action.pay;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.info.TradeInfo;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.domain.trade.TradeRequest;
import com.netfinworks.site.domain.enums.AcqTradeType;
import com.netfinworks.site.domainservice.trade.DefaultPaymentService;
import com.netfinworks.site.domainservice.trade.DefaultTradeQueryService;
import com.netfinworks.site.web.action.common.BaseAction;

/**
 * 通用说明： 付款action
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-22 下午4:40:57
 *
 */
@Controller
public class Payaction extends BaseAction {

    @Resource(name = "defaultPaymentService")
    private DefaultPaymentService defaultPaymentService;
    
    @Resource(name = "defaultTradeQueryService")
    private DefaultTradeQueryService defaultTradeQueryService;

    /**
     * 单笔支付
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/my/onePay.htm", method = RequestMethod.GET)
    public ModelAndView payOne(HttpServletRequest req, ModelMap model, OperationEnvironment env ) throws ServiceException {
        PersonMember user = getUser(req);
        Map<String, String> error = new HashMap<String, String>();
        RestResponse restP = new RestResponse();
        checkUser(user, error, restP);
        String tradeVoucherNo = req.getParameter("tradeVoucherNo");
        if(tradeVoucherNo==null){
            String tradeOrderNo = req.getParameter("tradeOrderNo");
            TradeRequest tradeRequest = new TradeRequest();
            PageResultList<TradeInfo> page = new PageResultList<TradeInfo>();
            tradeRequest.setGmtStart(DateUtil.getDateNearCurrent(-366));
            tradeRequest.setGmtEnd(new Date());
            tradeRequest.setTradeSourceVoucherNo(tradeOrderNo);
            QueryBase queryBase = new QueryBase();
            queryBase.setCurrentPage(1);
            tradeRequest.setQueryBase(queryBase);
            page = defaultTradeQueryService.queryTradeList(tradeRequest, env);
            tradeVoucherNo = page.getInfos().get(0).getTradeVoucherNo();
        }
        String tradeTypeTxt = req.getParameter("tradeTypeCode");
        List<String> list = new ArrayList<String>();
        list.add(tradeVoucherNo);
        TradeEnvironment tradeEnvironment = new TradeEnvironment();
        BeanUtils.copyProperties(env, tradeEnvironment);
        AcqTradeType tradeType = AcqTradeType.getByCode(tradeTypeTxt);
        String url = defaultPaymentService.applyPayment(user, list,tradeType, tradeEnvironment);
        return new ModelAndView("redirect:"+url);
    }
    /**
     * 合并付款
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/my/somePay.htm", method = RequestMethod.POST)
    public ModelAndView paySome(HttpServletRequest req, ModelMap model, OperationEnvironment env ) throws ServiceException {
        PersonMember user = getUser(req);
        Map<String, String> error = new HashMap<String, String>();
        RestResponse restP = new RestResponse();
        checkUser(user, error, restP);
        String[] tradeVoucherNos = req.getParameterValues("tradeVoucherNo");
        if(tradeVoucherNos == null || tradeVoucherNos.length <= 0) {
            logger.info("没有需要支付的凭证号，跳转回合并支付页面");
            return new ModelAndView("redirect:megerPay.htm");
        }
        List<String> list = new ArrayList<String>();
        for(String no :tradeVoucherNos) {
            list.add(no);
        }
        TradeEnvironment tradeEnvironment = new TradeEnvironment();
        BeanUtils.copyProperties(env, tradeEnvironment);
        String tradeTypeTxt = req.getParameter("tradeTypeCode");
        AcqTradeType tradeType = AcqTradeType.getByCode(tradeTypeTxt);
        String url = defaultPaymentService.applyPayment(user, list,tradeType, tradeEnvironment);
        return new ModelAndView("redirect:"+url);
    }
}
