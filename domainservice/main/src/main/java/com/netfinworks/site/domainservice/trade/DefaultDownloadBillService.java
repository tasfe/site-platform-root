package com.netfinworks.site.domainservice.trade;

import java.util.List;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.trade.DownloadBillRequest;
import com.netfinworks.site.domain.domain.trade.OrderSettleInfo;
import com.netfinworks.site.domain.domain.trade.RefundWaterInfo;

public interface DefaultDownloadBillService {
    /**
     * 查询支付流水
     * @return
     */
    public PageResultList queryPayWater(DownloadBillRequest reqInfo, OperationEnvironment env);

    /**
     * 查询订单结算
     * @return
     */
    public PageResultList queryOrderSettle(DownloadBillRequest reqInfo, OperationEnvironment env);

    /**
     * 查询转账交易
     * @return
     */
    public PageResultList queryTransfer(DownloadBillRequest reqInfo, OperationEnvironment env);

    /**
     * 查询退款流水
     * @return
     */
    public PageResultList queryRefundWater(DownloadBillRequest reqInfo, OperationEnvironment env);
    
}
