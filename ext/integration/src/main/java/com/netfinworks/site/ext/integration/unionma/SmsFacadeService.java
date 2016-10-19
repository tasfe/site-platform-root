package com.netfinworks.site.ext.integration.unionma;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.domain.domain.request.UnionmaSendEmailRequest;
import com.netfinworks.site.domain.domain.request.UnionmaSendMessageRequest;
import com.netfinworks.site.domain.domain.response.UnionmaBaseResponse;
import com.netfinworks.site.domain.domain.response.UnionmaSendEmailResponse;
import com.netfinworks.site.domain.exception.BizException;

public interface SmsFacadeService {
	/**
	 * 发送邮箱邮件
	 * 
	 * @param request
	 * @return
	 */

	public UnionmaSendEmailResponse sendEmail(UnionmaSendEmailRequest request)throws BizException;
	
	/**
	 * 发送手机短信
	 * 
	 * @param request
	 * @return
	 */

	public UnionmaBaseResponse sendMessage(UnionmaSendMessageRequest request,OperationEnvironment env) throws BizException;
}