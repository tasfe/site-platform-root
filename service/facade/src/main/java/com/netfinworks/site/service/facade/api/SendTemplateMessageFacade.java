package com.netfinworks.site.service.facade.api;

import javax.jws.WebService;

import com.yongda.site.service.personal.facade.request.WxMessageRequest;
import com.yongda.site.service.personal.facade.response.BaseResponse;

@WebService(targetNamespace = "http://facade.site.netfinworks.com")
public interface SendTemplateMessageFacade {
	/**
	 * 发送微信消息
	 * @param request
	 * @return
	 */
	 public BaseResponse sendMessage(WxMessageRequest request);
}
