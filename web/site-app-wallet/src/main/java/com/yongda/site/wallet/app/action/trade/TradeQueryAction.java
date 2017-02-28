package com.yongda.site.wallet.app.action.trade;

import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.DateUtil;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.info.TradeInfo;
import com.netfinworks.site.domain.domain.member.PersonMember;
import com.netfinworks.site.domain.domain.trade.TradeRequest;
import com.netfinworks.site.domainservice.deposit.DefaultDepositInfoService;
import com.netfinworks.site.domainservice.trade.DefaultDownloadBillService;
import com.netfinworks.site.domainservice.trade.DefaultFundoutService;
import com.netfinworks.site.domainservice.trade.DefaultTradeQueryService;
import com.netfinworks.site.ext.integration.member.AccountService;
import com.yongda.site.wallet.app.action.common.BaseAction;
import com.yongda.site.wallet.app.util.DateUtils;
import com.yongda.site.wallet.app.util.ResponseUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 *
 * <p>交易查询</p>
 * @author qinde
 * @version $Id: TradeQueryAction.java, v 0.1 2013-12-6 下午9:42:19 qinde Exp $
 */
@Controller
@RequestMapping(value = "/my/trade")
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
     * 查询交易记录
     *
     * @return
     */
    @RequestMapping(value = "searchall",method = RequestMethod.POST)
    @ResponseBody
    public RestResponse searchAll(HttpServletRequest request, HttpServletResponse resp,
                                  OperationEnvironment env)
                                                           throws Exception {
        logger.info("searchall..start");
        RestResponse restP      = ResponseUtil.buildSuccessResponse();
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            String refresh=request.getParameter("refresh");
            if(StringUtils.isNotEmpty(refresh)&&refresh.equals(CommonConstant.TRUE_STRING)){
                super.updateSessionObject(request);
            }
            String currentPage = request.getParameter("currentPage");
            String pageSize = request.getParameter("pageSize");
            String startDate = request.getParameter("startDate");
            String endDate = request.getParameter("endDate");

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
            List<String> tradeStatus = new ArrayList<String>();
            //tradeStatus.add("111");
            //tradeStatus.add("121");
            tradeStatus.add("201");//付款成功
            tradeStatus.add("301");//交易成功
            tradeStatus.add("401");//交易结束
           // tradeStatus.add("999");//交易关闭
            tradeStatus.add("998");//交易失败
            tradeStatus.add("951");//退款成功
           // tradeStatus.add("952");退款失败不展示
            tradeRequest.setTradeStatus(tradeStatus);
            List<String>    productCodes = new ArrayList<String>();
            productCodes.add("20203");//话费充值
            productCodes.add("20201");
            productCodes.add("20205");//post交易
            tradeRequest.setProductCodes(productCodes);

            //分页信息
            QueryBase queryBase = new QueryBase();
            if (StringUtils.isBlank(currentPage)) {
                currentPage = "1";
            }
            if (StringUtils.isBlank(pageSize)){
                pageSize = "10";
            }
            queryBase.setCurrentPage(Integer.valueOf(currentPage));
            queryBase.setPageSize(Integer.valueOf(pageSize));
            tradeRequest.setQueryBase(queryBase);

            user.setAccount(accountService.queryAccountById(user.getDefaultAccountId(), env));

            //查询交易记录
            PageResultList tradeListPage = defaultTradeQueryService.queryTradeList(tradeRequest, env);

            map.put("pageInfo", resultConvert(tradeListPage,env));
            map.put("mobile", user.getMobileStar());
            map.put("startDate", DateUtil.getWebDateString(tradeRequest.getGmtStart()));
            map.put("endDate", DateUtil.getWebDateString(tradeRequest.getGmtEnd()));
            restP.setData(map);
            restP.setSuccess(true);
            logger.info("查询交易记录,响应信息：{}",restP.getData().toString());
        } catch (Exception e) {
            logger.error("查询出错：{}",e);
            restP.setSuccess(false);
            restP.setMessage("交易查询错误：{}"+e.getMessage());
        }
        return restP;
    }
    /**响应结果转换******/
    private List<TradeInfo> resultConvert(PageResultList tradeListPage,OperationEnvironment env){
        List<TradeInfo>  resultPageInfo = new ArrayList<TradeInfo>();
        List<TradeInfo>  listInfos    = tradeListPage.getInfos();
        if (listInfos!=null && listInfos.size()>0){
            for(TradeInfo info : listInfos){
                TradeInfo refundDetail = searchRefundDetail(info.getTradeVoucherNo(),env);
                if (refundDetail!=null){
                    if (StringUtils.isBlank(refundDetail.getTradeMemo())){
                        refundDetail.setTradeMemo(StringUtils.isNotBlank(info.getTradeMemo())?info.getTradeMemo():null);
                    }
                    //舔加一天退款成功的记录
                    resultPageInfo.add(refundDetail);
                    //将之前的一条记录置为交易撤销(985)
                    info.setStatus("985");
                }
                resultPageInfo.add(info);
            }
            return resultPageInfo;
        }
        return null;
    }

    /**查询退款接口*****/
    private TradeInfo searchRefundDetail(String tradeVoucherNo,OperationEnvironment env){
        try {
            TradeRequest tradeRequest = new TradeRequest();
            tradeRequest.setGmtStart(DateUtil.getDateNearCurrent(-366));// 参照查询退款接口默认时间
            tradeRequest.setGmtEnd(DateUtil.addMinutes(new Date(), 30));
            // 分页信息
            QueryBase queryBase5 = new QueryBase();
            queryBase5.setCurrentPage(1);
            tradeRequest.setQueryBase(queryBase5);
            tradeRequest.setOrigTradeVoucherNo(tradeVoucherNo);
            List<String> tradeStatus = new ArrayList<String>();
            tradeStatus.add("951");
            tradeRequest.setTradeStatus(tradeStatus);
            PageResultList refundListPage = defaultTradeQueryService.queryRefundList(
                    tradeRequest, env);
            logger.info("查询退款响应信息：{}",refundListPage.getInfos());
            return refundListPage.getInfos()==null?null:
                    (TradeInfo)refundListPage.getInfos().get(0);
        } catch (ServiceException e) {
           logger.error("",e);
        }
        return null;
    }
}
