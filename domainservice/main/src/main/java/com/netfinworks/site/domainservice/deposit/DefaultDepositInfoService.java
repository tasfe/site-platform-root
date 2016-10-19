
package com.netfinworks.site.domainservice.deposit;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.deposit.api.domain.DepositInfo;
import com.netfinworks.deposit.api.domain.DepositListRequest;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.domain.info.DepositBasicInfo;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.exception.BizException;


public interface DefaultDepositInfoService {
    /**
     * 查询充值记录
     * @param req
     * @param paramOperationEnvironment
     * @return
     * @throws BizException
     */
    public PageResultList<DepositBasicInfo> queryList(DepositListRequest req,
                                       OperationEnvironment paramOperationEnvironment) throws ServiceException;
}
