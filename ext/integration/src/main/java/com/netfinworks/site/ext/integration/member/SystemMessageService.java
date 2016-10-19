package com.netfinworks.site.ext.integration.member;

import com.netfinworks.basis.service.domain.ActivityVo;
import com.netfinworks.basis.service.domain.RowVo;
import com.netfinworks.basis.service.domain.SystemMsgVo;
import com.netfinworks.basis.service.response.Response;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.domain.exception.BizException;

public interface SystemMessageService {
	/**
	 * 获取系统消息 详情
	 * @return
	 * @throws ServiceException
	 */
	public Response<SystemMsgVo> getSystemMessage(String paramString)throws BizException;
	
	/**
	 * 获取系统消息 列表
	 * @return
	 * @throws ServiceException
	 */
	public Response<RowVo<SystemMsgVo>> getSystemMsgLimit(String paramString, int paramInt1, int paramInt2)throws BizException;
	
	
	/**
	 * 获取活动 详情
	 * @return
	 * @throws ServiceException
	 */
	public  Response<ActivityVo> getActivityById(String paramString)throws BizException;
	
	/**
	 * 获取活动 列表
	 * @return
	 * @throws ServiceException
	 */
	public  Response<RowVo<ActivityVo>>  getActivityLimit(String paramString, int paramInt1, int paramInt2)throws BizException;
}
