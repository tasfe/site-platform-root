package com.netfinworks.site.ext.integration.member.impl;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netfinworks.basis.service.api.ActivityFacade;
import com.netfinworks.basis.service.api.SystemMsgFacade;
import com.netfinworks.basis.service.domain.ActivityVo;
import com.netfinworks.basis.service.domain.RowVo;
import com.netfinworks.basis.service.domain.SystemMsgVo;
import com.netfinworks.basis.service.response.Response;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.member.SystemMessageService;

@Service("systemMessageService")
public class SystemMessageServiceImpl implements SystemMessageService{
	
	private Logger logger = LoggerFactory.getLogger(SystemMessageServiceImpl.class); 
	
	@Resource(name = "systemMsgFacadeService")
    private SystemMsgFacade systemmsgfacade;
	
	@Resource(name = "activityfacadeService")
    private ActivityFacade activityfacade;
	
	@Override
	public Response<RowVo<SystemMsgVo>> getSystemMsgLimit(String paramString, int paramInt1,
			int paramInt2) throws BizException {
		 try{
			 Response<RowVo<SystemMsgVo>>  response = systemmsgfacade.getSystemMsgLimit(paramString, paramInt1, paramInt2);
			 if(response.isSuccess()){
				 return response;
			 }else {
	             throw new BizException(ErrorCode.SYSTEM_ERROR, response.getResultMessage());
	         }
		}catch(Exception e){
			if (e instanceof BizException) {
             throw (BizException) e;
         } else {
             logger.error("查询系统列表消息:异常信息：{},{}"+e.getMessage());
             throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
         }
		}
	}
	
	@Override
	public Response<SystemMsgVo> getSystemMessage(String paramString) throws BizException {
		try{
			Response<SystemMsgVo> response = systemmsgfacade.getSystemMsgById(paramString);
			 if(response.isSuccess()){
				 return response;
			 }else {
	             throw new BizException(ErrorCode.SYSTEM_ERROR, response.getResultMessage());
	         }
		}catch(Exception e){
			if (e instanceof BizException) {
	         throw (BizException) e;
	     } else {
	         logger.error("查询系统列表消息:异常信息：{},{}"+e.getMessage());
	         throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
	     }
	}
	}

	@Override
	public Response<ActivityVo> getActivityById(String paramString)
			throws BizException {
		try{
			 Response<ActivityVo> response = activityfacade.getActivityById(paramString);
			 if(response.isSuccess()){
				 return response;
			 }else {
	             throw new BizException(ErrorCode.SYSTEM_ERROR, response.getResultMessage());
	         }
		}catch(Exception e){
			if (e instanceof BizException) {
	         throw (BizException) e;
	     } else {
	         logger.error("查询系统列表消息:异常信息：{},{}"+e.getMessage());
	         throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
	     }
		}
	}

	@Override
	public Response<RowVo<ActivityVo>> getActivityLimit(String paramString,
			int paramInt1, int paramInt2) throws BizException {
		 try{
			 Response<RowVo<ActivityVo>>  response = activityfacade.getActivityLimit(paramString, paramInt1, paramInt2);
			 if(response.isSuccess()){
				 return response;
			 }else {
	             throw new BizException(ErrorCode.SYSTEM_ERROR, response.getResultMessage());
	         }
		}catch(Exception e){
			if (e instanceof BizException) {
             throw (BizException) e;
         } else {
             logger.error("查询系统列表消息:异常信息：{},{}"+e.getMessage());
             throw new BizException(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getCause());
         }
		}
	}
}
