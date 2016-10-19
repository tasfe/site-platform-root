package com.netfinworks.site.ext.integration.trade;


import java.util.List;


import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.batchQuery.BatchQuery;
import com.netfinworks.site.ext.integration.batchQuery.BatchQueryResponse;

public interface QueryDetailService {
	
	 public BatchQueryResponse batchQueryDetail(BatchQuery req)throws BizException;
}
