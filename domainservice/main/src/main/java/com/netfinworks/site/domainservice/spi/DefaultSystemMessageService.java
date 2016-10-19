package com.netfinworks.site.domainservice.spi;

import com.netfinworks.basis.service.domain.ActivityVo;
import com.netfinworks.basis.service.domain.RowVo;
import com.netfinworks.basis.service.response.Response;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;

public interface DefaultSystemMessageService {
	/**
	 * 获取系统消息 详情
	 * @return
	 * @throws ServiceException
	 */
	public RestResponse getSystemMessage(String paramString)throws ServiceException;
	
	/**
	 * 获取系统消息 列表
	 * @return  paramInt1  当前页
	 * 			paramInt2   每页条数
	 * @throws ServiceException
	 */
	public RestResponse getSystemMsgLimit(String paramString, int paramInt1, int paramInt2)throws ServiceException;
	
	

	/**
	 * 获取活动 列表
	 * @return  paramInt1  当前页
	 * 			paramInt2   每页条数
	 * @throws ServiceException
	 */
	public Response<RowVo<ActivityVo>>  getActivityLimit(String paramString, int paramInt1, int paramInt2)throws ServiceException;
	
	/**
	 * 获取活动 详情
	 * @return
	 * @throws ServiceException
	 */
	public RestResponse getActivityById(String paramString)throws ServiceException;
}
