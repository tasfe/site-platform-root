/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-domainservice-main
 * @date 2014年8月19日
 */
package com.netfinworks.site.domainservice.trade.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import com.netfinworks.batchservice.facade.api.BatchServiceFacade;
import com.netfinworks.batchservice.facade.enums.ProductType;
import com.netfinworks.batchservice.facade.enums.SubmitType;
import com.netfinworks.batchservice.facade.model.BatchDetail;
import com.netfinworks.batchservice.facade.request.BatchRequest;
import com.netfinworks.batchservice.facade.response.BatchResponse;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domainservice.trade.DefaultRefundService;

/**
 * 退款业务
 * @author xuwei
 * @date 2014年8月19日
 */
public class DefaultRefundServiceImpl implements DefaultRefundService {
	@Resource(name = "batchServiceFacade")
    private BatchServiceFacade batchServiceFacade;

	@Override
	public String batchRefundApply(String sourceBatchNo, EnterpriseMember user, Money totalAmount, 
			int totalCount, List<BatchDetail> batchDetails) throws Exception {
		BatchRequest req = new BatchRequest();
		req.setBatchNo(UUID.randomUUID().toString());
		req.setSourceBatchNo(sourceBatchNo);
		req.setSubmitTime(new Date());
		req.setPartnerId(user.getMemberId());
		req.setSubmitType(SubmitType.API.getCode());
		req.setProductType(ProductType.REFUND.getCode());
		req.setSubmitId(user.getMemberId());
		req.setSubmitName(user.getMemberName());
		req.setBatchDetails(batchDetails);
		
		BatchResponse resp = batchServiceFacade.create(req);
		if (resp != null && "F002".equals(resp.getResultCode())) {
			throw new Exception(resp.getResultMessage());
		}
		
		return req.getBatchNo();
	}

	@Override
	public String batchRefundSubmit(String sourceBatchNo, EnterpriseMember user, Money totalAmount, 
			int totalCount, List<BatchDetail> batchDetails) throws Exception {
		BatchRequest req = new BatchRequest();
		req.setBatchNo(UUID.randomUUID().toString());
		req.setSourceBatchNo(sourceBatchNo);
		req.setSubmitTime(new Date());
		req.setPartnerId(user.getMemberId());
		req.setSubmitType(SubmitType.API.getCode());
		req.setProductType(ProductType.REFUND.getCode());
		req.setSubmitId(user.getMemberId());
		req.setSubmitName(user.getMemberName());
		req.setBatchDetails(batchDetails);
		
		BatchResponse resp = batchServiceFacade.createAndPay(req);
		if (resp != null && "F002".equals(resp.getResultCode())) {
			throw new Exception(resp.getResultMessage());
		}
		
		return req.getBatchNo();
	}

}
