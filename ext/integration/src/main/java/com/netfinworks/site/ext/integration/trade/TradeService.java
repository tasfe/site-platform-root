package com.netfinworks.site.ext.integration.trade;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.site.domain.domain.info.BaseInfo;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.response.CoTradeQueryResponse;
import com.netfinworks.site.domain.domain.trade.CoTradeRequest;
import com.netfinworks.site.domain.domain.trade.TradeRefundRequset;
import com.netfinworks.site.domain.domain.trade.TradeRequest;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.tradeservice.facade.request.RefundRequest;
import com.netfinworks.tradeservice.facade.response.PayMethodQueryResponse;
import com.netfinworks.tradeservice.facade.response.PaymentResponse;
import com.netfinworks.tradeservice.facade.response.RefundResponse;
import com.netfinworks.tradeservice.facade.response.TradeDetailQueryResponse;

/**
 *
 * <p>交易查询</p>
 * @author qinde
 * @version $Id: TradeService.java, v 0.1 2013-12-16 上午10:31:56 qinde Exp $
 */
public interface TradeService {
    /**
     * 查询 个人钱包会员交易信息
     * @param req
     * @param env
     * @return
     * @throws BizException
     */
    public PageResultList queryTradeList(TradeRequest req, OperationEnvironment env)
                                                                                    throws BizException;

    /**
     * 退出交易查询
     * @param req
     * @param env
     * @return
     * @throws BizException
     */
    public PageResultList queryRefundList(TradeRequest req, OperationEnvironment env)
                                                                                     throws BizException;

    /**
     * 查询交易明细
     * @param paramString
     * @param paramOperationEnvironment
     * @return
     */
    public TradeDetailQueryResponse queryDetail(String paramString,
                                                OperationEnvironment paramOperationEnvironment);

    /**
     * 创建交易订单
     * @param req
     */
    public void createOrder(TradeRequestInfo req) throws BizException;
    
    /**
     * 支付
     * @param req
     */
    public PaymentResponse pay(TradeRequestInfo req) throws BizException;
    
    /**
     * zhangyun.m
     * 查询交易记录
     * @param req
     * @return
     * @throws BizException
     */
    public CoTradeQueryResponse  queryList(CoTradeRequest req) throws BizException;
    
    /**
     * zhaozhenqiang
     * 网关交易-退款-
     * 
     * */
    public RefundResponse tradeRefund(BaseInfo rep,TradeRefundRequset tr,OperationEnvironment env);
    
    
    /**
     * HYj 
     * 退款审核
     * @param rep
     * @param tr
     * @param env
     * @return
     */
    public RefundResponse tradeRefund(CoTradeQueryResponse rep,TradeRefundRequset tr,OperationEnvironment env);
    /**
     * 
     * 简单退款
     * */
    public RefundResponse simpleRefund(RefundRequest refundRequest,OperationEnvironment env);
    
    /**
     * 根据交易凭证号查询成功的或者最后一条支付订单的支付方式
     * @param tradeVoucherNo
     * @param env
     * @return
     */
    public String queryPayMethods(String tradeVoucherNo,OperationEnvironment env);

    
    /**
     * 根据交易凭证号和退款来源判断渠道是否存在退款接口
     * @param tradeVoucherNo
     * @param env
     * @return
     */
    public boolean queryIfExistsRefundApi(String tradeVoucherNo,OperationEnvironment env);

}
