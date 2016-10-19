package com.netfinworks.site.ext.integration.trade.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.trade.DownloadBillRequest;
import com.netfinworks.site.ext.integration.ClientEnvironment;
import com.netfinworks.site.ext.integration.trade.DownloadBillService;
import com.netfinworks.site.ext.integration.trade.convert.SettlementConvert;
import com.netfinworks.site.ext.integration.trade.convert.TradeConvert;
import com.netfinworks.tradeservice.facade.api.TradeQueryFacade;
import com.netfinworks.tradeservice.facade.model.query.RefundTradeBasicInfo;
import com.netfinworks.tradeservice.facade.model.query.TradeBasicInfo;
import com.netfinworks.tradeservice.facade.request.RefundQueryRequest;
import com.netfinworks.tradeservice.facade.request.TradeBasicQueryRequest;
import com.netfinworks.tradeservice.facade.response.RefundQueryResponse;
import com.netfinworks.tradeservice.facade.response.TradeBasicQueryResponse;

@Service("downloadBillService")
public class DownloadBillServiceImpl extends ClientEnvironment implements DownloadBillService {
    private Logger           logger = LoggerFactory.getLogger(DownloadBillServiceImpl.class);
    @Resource(name = "tradeQueryFacade")
    private TradeQueryFacade tradeQueryFacade;
   
    /**
     * 支付流水
     */
    @Override
    public PageResultList queryPayWater(DownloadBillRequest reqInfo, OperationEnvironment env) {
        TradeBasicQueryRequest req = TradeConvert.convertDownloadBillQueryRequset(reqInfo);
        logger.info("[支付流水查询参数：]"+req.toString());
        TradeBasicQueryResponse response = tradeQueryFacade.queryList(req, env);
        if (response.isSuccess()) {
            PageResultList page = new PageResultList();
            List<TradeBasicInfo> result = response.getTradeBasicInfoList();
            if (result != null && result.size() > 0) {
                try {
                    page.setInfos(TradeConvert.convertPayWaterList(result));
                } catch (IllegalAccessException e) {
                    logger.error("[支付流水查询异常信息1]" + e);
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    logger.error("[支付流水插叙异常信息2]" + e);
                    e.printStackTrace();
                } catch (Exception e) {
                    logger.error("[支付流水查询接口异常信息]" + e);
                }
            }
            page.setPageInfo(response.getQueryBase());
            return page;
        }

        return null;
    }

    /**
     * 订单结算
     */
    @Override
    public PageResultList queryOrderSettle(DownloadBillRequest reqInfo, OperationEnvironment env) {
        TradeBasicQueryRequest req = TradeConvert.convertDownloadBillQueryOrderRequset(reqInfo);
        logger.info("[订单结算查询参数：]"+req.toString());
        TradeBasicQueryResponse response = tradeQueryFacade.queryList(req, env);
        if (response.isSuccess()) {
            PageResultList page = new PageResultList();
            List<TradeBasicInfo> result = response.getTradeBasicInfoList();
            if (result != null && result.size() > 0) {
                try {
                    page.setInfos(TradeConvert.convertOrderSettleList(result));
                } catch (IllegalAccessException e) {
                    logger.error("[订单结算查询异常信息1]" + e);
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    logger.error("[订单结算查询异常信息2]" + e);
                    e.printStackTrace();
                } catch (Exception e) {
                    logger.error("[订单结算查询接口异常信息]" + e);
                }
            }
            page.setPageInfo(response.getQueryBase());
            return page;
        }

        return null;
    }

    /**
     * 退款流水
     */
    @Override
    public PageResultList queryRefundWater(DownloadBillRequest reqInfo, OperationEnvironment env) {
        RefundQueryRequest req = TradeConvert.convertDownloadBillQueryRefundRequset(reqInfo);
        logger.info("[退款流水查询参数：]"+req.toString());
        RefundQueryResponse response = tradeQueryFacade.queryRefundList(req, env);
        if (response.isSuccess()) {
            PageResultList page = new PageResultList();
            List<RefundTradeBasicInfo> result = response.getRefundTradeInfos();
            if (result != null && result.size() > 0) {
                try {
                    page.setInfos(TradeConvert.convertrefundWaterList(result));
                } catch (IllegalAccessException e) {
                    logger.error("[退款流水查询异常信息1]" , e);
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    logger.error("[退款流水查询查询异常信息2]" , e);
                    e.printStackTrace();
                } catch (Exception e) {
                    logger.error("[退款流水查询查询接口异常信息]" , e);
                }
            }
            page.setPageInfo(response.getQueryBase());
            return page;
        }
        return null;
    }
    /**
     * 转账查询
     */
    @Override
    public PageResultList queryTransfer(DownloadBillRequest reqInfo, OperationEnvironment env) {
        TradeBasicQueryRequest req = TradeConvert.convertTransferQueryRequset(reqInfo);
        logger.info("[转账查询参数：]"+req.toString());
        req.setBuyerId(reqInfo.getMemberId());
        TradeBasicQueryResponse response = tradeQueryFacade.queryList(req, env);
        if (response.isSuccess()) {
            PageResultList page = new PageResultList();
            List<TradeBasicInfo> result = response.getTradeBasicInfoList();
            if (result != null && result.size() > 0) {
                try {
                    page.setInfos(TradeConvert.convertTransferList(result,reqInfo));
                } catch (IllegalAccessException e) {
                    logger.error("[转账查询异常信息1]" , e);
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    logger.error("[转账查询异常信息2]" , e);
                    e.printStackTrace();
                } catch (Exception e) {
                    logger.error("[转账查询接口异常信息]" , e);
                }
            }
            if(response.getTradeInfoSummary()!=null)
            {
            	page.setSummaryInfo(SettlementConvert.convertSummaryReponse(response.getTradeInfoSummary(),"kjt"));
            }
            page.setPageInfo(response.getQueryBase());
            return page;
        }

        return null;
    }
    
    
    
}
