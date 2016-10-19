package com.netfinworks.site.ext.integration.trade;

import com.netfinworks.site.domain.domain.response.WalletCheckResponse;
import com.netfinworks.site.domain.domain.trade.WalletCheckRequest;
import com.netfinworks.site.domain.exception.BizException;

/**
 * <p>企业钱包对账单接口</p>
 * @author zhangyun.m
 * @version $Id: CoAccountService.java, v 0.1 2013-12-12 下午4:23:07 HP Exp $
 */
public interface CoAccountService {
    

    /**
     * zhangyun.m
     * 企业查询钱包对账单
     * @param req
     * @return
     * @throws BizException
     */
    public WalletCheckResponse queryWalletCheckList(WalletCheckRequest req) throws BizException;

}
