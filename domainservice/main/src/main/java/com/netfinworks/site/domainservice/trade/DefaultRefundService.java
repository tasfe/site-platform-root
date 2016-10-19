/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-domainservice-main
 * @date 2014年8月19日
 */
package com.netfinworks.site.domainservice.trade;

import java.util.List;

import com.netfinworks.batchservice.facade.model.BatchDetail;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.exception.BizException;

/**
 * 退款业务
 * @author xuwei
 * @date 2014年8月19日
 */
public interface DefaultRefundService {
	/**
     * 批量转账申请
     * @param req
     * @throws BizException
     */
    public String batchRefundApply(String sourceBatchNo, EnterpriseMember user, Money totalAmount, 
			int totalCount, List<BatchDetail> batchDetails) throws Exception;
    
    /**
     * 批量转账确认
     * @param req 请求对象
     * @throws BizException 业务操作异常
     */
    public String batchRefundSubmit(String sourceBatchNo, EnterpriseMember user, Money totalAmount, 
			int totalCount, List<BatchDetail> batchDetails) throws Exception;
}
