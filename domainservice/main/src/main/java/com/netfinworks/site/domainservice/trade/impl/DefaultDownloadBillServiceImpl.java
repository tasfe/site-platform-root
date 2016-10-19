package com.netfinworks.site.domainservice.trade.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.dal.daointerface.AuditDAO;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.trade.DownloadBillRequest;
import com.netfinworks.site.domainservice.trade.DefaultDownloadBillService;
import com.netfinworks.site.ext.integration.trade.DownloadBillService;

@Service("defaultDownloadBillService")
public class DefaultDownloadBillServiceImpl implements DefaultDownloadBillService {
    protected Logger            log = LoggerFactory.getLogger(DefaultDownloadBillServiceImpl.class);
    @Resource(name = "downloadBillService")
    private DownloadBillService downloadBillService;
    @Resource(name = "auditDAO")
    private AuditDAO auditDAO;

    @Override
    public PageResultList queryPayWater(DownloadBillRequest reqInfo, OperationEnvironment env) {
        try {
            return downloadBillService.queryPayWater(reqInfo, env);
        } catch (Exception e) {
            log.error("支付流水查询异常3" + e);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public PageResultList queryOrderSettle(DownloadBillRequest reqInfo, OperationEnvironment env) {
        try {
            return downloadBillService.queryOrderSettle(reqInfo, env);
        } catch (Exception e) {
            log.error("订单结算查询异常3" + e);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public PageResultList queryRefundWater(DownloadBillRequest reqInfo, OperationEnvironment env) {
        try {
            return downloadBillService.queryRefundWater(reqInfo, env);
        } catch (Exception e) {
            log.error("退款流水查询异常3" + e);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public PageResultList queryTransfer(DownloadBillRequest reqInfo, OperationEnvironment env) {
        try {
            return downloadBillService.queryTransfer(reqInfo, env);
        } catch (Exception e) {
            log.error("转账查询" + e);
            e.printStackTrace();
            return null;
        }
    }

}
