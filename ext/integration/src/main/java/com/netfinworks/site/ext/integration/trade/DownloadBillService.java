package com.netfinworks.site.ext.integration.trade;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.trade.DownloadBillRequest;

public interface DownloadBillService {
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
     * 查询退款流水
     * @return
     */
    public PageResultList queryRefundWater(DownloadBillRequest reqInfo, OperationEnvironment env);
    /**
     * 转账
     * @return
     */
    public PageResultList queryTransfer(DownloadBillRequest reqInfo, OperationEnvironment env);
    
}
