package com.netfinworks.site.domainservice.spi.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.basis.service.domain.ActivityVo;
import com.netfinworks.basis.service.domain.RowVo;
import com.netfinworks.basis.service.domain.SystemMsgVo;
import com.netfinworks.basis.service.response.Response;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.spi.DefaultSystemMessageService;
import com.netfinworks.site.ext.integration.member.SystemMessageService;


/**
 * 获取微信系统消息
 * @author admin
 *
 */
@Service("defaultSystemMessageService")
public class DefaultSystemMessageServiceImpl implements DefaultSystemMessageService{
	protected Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource(name = "systemMessageService")
    private SystemMessageService systemmessageservice;
	
	
	@Override
	public RestResponse getSystemMsgLimit(String paramString, int paramInt1,
			int paramInt2) throws ServiceException {
		RestResponse restresponse = new RestResponse();
		try {
			Map<String,Object> map = new HashMap<String,Object>();
			Response<RowVo<SystemMsgVo>> response = systemmessageservice.getSystemMsgLimit(paramString, paramInt1, paramInt2);
			map.put("msgList",response.getBean().getRows());
			restresponse.setData(map);
			//业务逻辑处理
		} catch (BizException e) {
			log.info("获取微信系统消息，调用接口异常!");
			log.error(e.getMessage(), e.getCause());
			throw new ServiceException(e.getMessage(), e.getCause());
		}
		return restresponse;
	}
	
	@Override
	public RestResponse getSystemMessage(String paramString) throws ServiceException {
		RestResponse restResponse = new RestResponse();
		try { 
			Response<SystemMsgVo> result = systemmessageservice.getSystemMessage(paramString);
			Map<String,Object> map = new HashMap<String,Object>();
			//业务逻辑处理
			map.put("newsContent", result.getBean().getNewsContent());
			restResponse.setData(map);
		} catch (BizException e) {
			log.info("获取微信系统消息，调用接口异常!");
			log.error(e.getMessage(), e.getCause());
			throw new ServiceException(e.getMessage(), e.getCause());
		}
		return restResponse;
	}

	@Override
	public Response<RowVo<ActivityVo>>  getActivityLimit(String paramString, int paramInt1,
			int paramInt2) throws ServiceException {
		try {
			Map<String,Object> map = new HashMap<String,Object>();
			Response<RowVo<ActivityVo>> response = systemmessageservice.getActivityLimit(paramString, paramInt1, paramInt2);
			return response;
		} catch (BizException e) {
			log.info("获取微信系统消息，调用接口异常!");
			log.error(e.getMessage(), e.getCause());
			throw new ServiceException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public RestResponse getActivityById(String paramString)
			throws ServiceException {
		RestResponse restResponse = new RestResponse();
		try { 
			Response<ActivityVo> result = systemmessageservice.getActivityById(paramString);
			Map<String,Object> map = new HashMap<String,Object>();
			//业务逻辑处理
			map.put("newsContent", result.getBean().getActivityContent());
			restResponse.setData(map);
		} catch (BizException e) {
			log.info("获取微信系统消息，调用接口异常!");
			log.error(e.getMessage(), e.getCause());
			throw new ServiceException(e.getMessage(), e.getCause());
		}
		return restResponse;
	}

	
		
}
