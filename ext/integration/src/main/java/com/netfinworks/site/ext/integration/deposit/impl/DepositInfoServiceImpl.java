package com.netfinworks.site.ext.integration.deposit.impl;

import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.meidusa.fastjson.JSONObject;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.deposit.api.DepositService;
import com.netfinworks.deposit.api.domain.DepositInfo;
import com.netfinworks.deposit.api.domain.DepositListRequest;
import com.netfinworks.deposit.api.domain.DepositListResponse;
import com.netfinworks.site.domain.domain.info.DepositBasicInfo;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.deposit.DepositInfoService;
import com.netfinworks.site.ext.integration.trade.convert.TradeConvert;


/**
 *
 * <p>充值服务</p>
 * @author qinde
 * @version $Id: DepositInfoServiceImpl.java, v 0.1 2013-12-4 下午9:02:06 qinde Exp $
 */
@Service("depositInfoService")
public class DepositInfoServiceImpl implements DepositInfoService {
    private Logger         logger = LoggerFactory.getLogger(DepositInfoServiceImpl.class);

    @Resource(name = "depositFacade")
    private DepositService depositService;

    @Override
    public PageResultList<DepositBasicInfo> queryList(DepositListRequest req,
                                       OperationEnvironment paramOperationEnvironment)  throws BizException{
        try {
            long beginTime = 0L;
            if (logger.isInfoEnabled()) {
                logger.info("查询充值记录，请求参数：{}", req);
                beginTime = System.currentTimeMillis();
            }

            DepositListResponse response = depositService.queryList(req,
                paramOperationEnvironment);
            if (logger.isInfoEnabled()) {
                long consumeTime = System.currentTimeMillis() - beginTime;
                logger.info("远程充值记录， 耗时:{} (ms); 响应结果:{} ",
                    new Object[] { consumeTime, response });
            }
            
            if (ResponseCode.DEPOSITY_SUCCESS.getCode().equals(response.getRespResult().getBizCode())) {
                PageResultList<DepositBasicInfo> page = new PageResultList<DepositBasicInfo>();
                page.setPageInfo(response.getPageInfo());
                page.setInfos(TradeConvert.convertRechargeList(response.getDepositInfos()));
                return page;
            } else {
                logger.error("查询充值记录信息异常:{}", response.getRespResult().getResultMessage());
                return null;
            }

        } catch (Exception e) {
            logger.error("充值记录： {}信息异常:请求信息", req, e);
            throw new BizException(ErrorCode.SYSTEM_ERROR);
        }
    }
}
