package com.yongda.site.service.personal.facade.api;

import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.yongda.site.service.personal.facade.response.BaseResponse;


public interface IRoutService extends InitializingBean{
	
	public BaseResponse process(Map<String,String> reqParams)throws Exception;
}
