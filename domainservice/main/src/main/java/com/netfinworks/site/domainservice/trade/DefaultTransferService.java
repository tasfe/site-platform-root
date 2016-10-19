/**
 * 
 */
package com.netfinworks.site.domainservice.trade;

import java.util.List;

import com.netfinworks.batchservice.facade.enums.ProductType;
import com.netfinworks.batchservice.facade.model.BatchDetail;
import com.netfinworks.batchservice.facade.response.BatchResponse;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.enums.TradeType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.tradeservice.facade.response.PaymentResponse;

/**
 * <p>钱包转账服务</p>
 * @author fjl
 * @version $Id: DefaultTransferService.java, v 0.1 2013-11-29 下午1:38:03 fjl Exp $
 */
public interface DefaultTransferService {

    /**
     * <li>落地凭证</li>
     * <li>落地交易订单</li>
     * <li>获取收银台地址</li>
     * <li>调用者获得地址后跳转<li>
     * @param 交易请求对象 
     * @return 返回收银台地址
     */
    public String transfer(TradeRequestInfo req) throws BizException;

    /**
     * <li>落地凭证</li>
     * <li>落地交易订单</li>
     */
    public void etransfer(TradeRequestInfo req, TradeType tradeType) throws BizException;
    
    /**
     * 支付
     * @param req
     */
    public PaymentResponse pay(TradeRequestInfo req) throws BizException;
    
    /**
     * 获取转账凭证号
     * @param req
     */
    public void getTransferVoucherNo(TradeRequestInfo req) throws BizException ;
    
    /**
     * 批量转账申请
     * @param req
     * @throws BizException
     */
    public String batchTransferApply(String batchNo,String sourceBatchNo, ProductType productType, EnterpriseMember user, List<BatchDetail> batchDetails, String totalAmount) throws Exception;
    
    /**
     * 批量转账确认
     * @param req 请求对象
     * @throws BizException 业务操作异常
     */
    public String batchTransferSubmit(String batchNo,String sourceBatchNo, ProductType productType, EnterpriseMember user, List<BatchDetail> batchDetails, String totalAmount) throws Exception;
    
    /**
     * 批量转账支付
     * @param batcNo 批次号
     * @return
     * @throws Exception
     */
    public String batchTransferPay(String batcNo) throws Exception;
    
    /**
     * 按批次解冻
     * @param batchNo
     * @return
     */
    public BatchResponse unfreezeBatch(String batchNo) throws Exception;
}
