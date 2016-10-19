package com.netfinworks.site.ext.integration.deposit;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.deposit.api.domain.DepositInfo;
import com.netfinworks.deposit.api.domain.DepositListRequest;
import com.netfinworks.site.domain.domain.info.DepositBasicInfo;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.exception.BizException;

/**
 *
 * <p>充值服务</p>
 * @author qinde
 * @version $Id: DepositInfoService.java, v 0.1 2013-12-4 下午9:01:51 qinde Exp $
 */
public interface DepositInfoService {
    /**
     * 查询充值记录
     * @param req
     * @param paramOperationEnvironment
     * @return
     * @throws BizException
     */
    public PageResultList<DepositBasicInfo> queryList(DepositListRequest req,
                                       OperationEnvironment paramOperationEnvironment)
                                                                                      throws BizException;
}
