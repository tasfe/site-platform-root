/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-ext-integration
 * @date 2014年8月4日
 */
package com.netfinworks.site.domainservice.lflt;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.lflt.api.request.check.GetQuatoTimesRequest;
import com.netfinworks.lflt.api.request.check.VerifyRequest;
import com.netfinworks.lflt.api.response.AvailableResponse;
import com.netfinworks.lflt.api.response.CheckLfltResponse;

/**
 * 限额限次服务接口
 * @author xuwei
 * @date 2014年8月4日
 */
public interface LfltService {
	/**
	 * 验证额度和次数是否超限
	 * @param request 请求对象
	 * @param env
	 * @return 检查结果
	 */
	public CheckLfltResponse verifyLimit(VerifyRequest request, OperationEnvironment env);
	
	/**
	 * 查询可用额度和次数
	 * @param request 请求对象
	 * @param env
	 * @return 响应结果（可用额度和次数）
	 */
	public AvailableResponse getAvailableLimit(GetQuatoTimesRequest request, OperationEnvironment env);
}
