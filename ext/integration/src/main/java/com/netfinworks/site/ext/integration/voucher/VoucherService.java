/**
 * 
 */
package com.netfinworks.site.ext.integration.voucher;

import com.meidusa.venus.annotations.Service;
import com.netfinworks.site.domain.domain.trade.TradeEnvironment;
import com.netfinworks.site.domain.domain.trade.TradeRequestInfo;
import com.netfinworks.site.domain.exception.BizException;

/**
 * <p>
 * 统一凭证服务
 * </p>
 * 
 * @author fjl
 * @version $Id: VoucherService.java, v 0.1 2013-11-28 上午11:13:44 fjl Exp $
 */
public interface VoucherService {

    /**
     * 注册（批量）凭证号 一次只可注册某一凭证类型的凭证号
     * 
     * @param registerRequest
     * @return
     */
    public String regist(String voucherType) throws BizException;

    /**
     * 记录凭证号
     * @param voucherNo 凭证号
     * @param voucherType 凭证类型
     * @param productCode 产品码
     * @param partnerId 商户ID
     * @param env 客户端环境信息
     * @throws BizException 记录失败抛出的业务异常
     */
    public void record(String voucherNo, String voucherType, String productCode, String partnerId,
            TradeEnvironment env) throws BizException;

    /**
     * 记录交易凭证
     * 
     * @param req
     * @return 凭证号
     */
    public String recordTradeVoucher(TradeRequestInfo req) throws BizException;

    /**
     * 记录支付凭证
     * 
     * @param req
     * @return 凭证号
     */
    public String recordPaymentVoucher(TradeRequestInfo req) throws BizException;
}
