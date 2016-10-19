package com.netfinworks.site.ext.integration.unionma;

import com.netfinworks.site.domain.domain.request.loginPasswordEditReq;
import com.netfinworks.site.domain.domain.response.UnionmaBaseResponse;
import com.netfinworks.site.domain.exception.BizException;

public interface LoginPasswordService {

	/**
	 * 修改登录密码
	 * @param loginPasswordEditRequestParam
	 * @return
	 */
	public UnionmaBaseResponse loginPasswordEdit(loginPasswordEditReq req)throws BizException;;
	/**
	 * 设置登录密码
	 * @param loginPasswordEditRequestParam
	 * @return
	 */
	//public LoginPasswordSetResponse loginPasswordSet(LoginPasswordSetRequestParam req)throws BizException;;

	
}
