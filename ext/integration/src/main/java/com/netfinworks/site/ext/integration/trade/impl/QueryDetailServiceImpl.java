package com.netfinworks.site.ext.integration.trade.impl;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.batchservice.facade.api.BatchQueryServiceFacade;
import com.netfinworks.batchservice.facade.request.BatchDetailQueryRequest;
import com.netfinworks.batchservice.facade.response.BatchDetailQueryResponse;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.batchQuery.BatchQuery;
import com.netfinworks.site.ext.integration.batchQuery.BatchQueryResponse;
import com.netfinworks.site.ext.integration.trade.QueryDetailService;

@Service("queryDetailService")
public class QueryDetailServiceImpl implements QueryDetailService{
	
	private Logger logger = LoggerFactory.getLogger(QueryDetailServiceImpl.class);
	
	@Resource(name = "batchQueryServiceFacade")
	private BatchQueryServiceFacade batchQueryServiceFacade;
	
	@Override
    public BatchQueryResponse batchQueryDetail(BatchQuery req) throws BizException {
        String opMsg="";
        try {
            long beginTime = System.currentTimeMillis();
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"，请求参数：BatchQuery:{}", req);
            }
            //GetFunctionListFromOperatorRequest  request=AuthVOConvert.createGetFuncList4OpRequest(authVO);
            BatchDetailQueryRequest request = new BatchDetailQueryRequest();
            request.setBatchNo(req.getBatchNo());
            request.setPageNum(req.getPageNum());
            request.setPageSize(req.getPageSize());
            BatchDetailQueryResponse response = batchQueryServiceFacade.queryDetail(request);
            BatchQueryResponse res = new BatchQueryResponse();
            res.setResultCode(response.getResultCode());
            res.setResultDetails(response.getResultDetails());
            res.setResultMessage(response.getResultMessage());
            res.setTotalSize(response.getTotalSize());
            long consumeTime = System.currentTimeMillis() - beginTime;
            if (logger.isInfoEnabled()) {
                logger.info(opMsg+"， 耗时:{} (ms); 响应结果:{} ", new Object[] { consumeTime, response });
            }
            return res;
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            } else {
                logger.error(opMsg+"参数：BatchQuery:{},{}，异常信息{}", req, e.getMessage(), e);
                throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
            }
        }
    }


}
