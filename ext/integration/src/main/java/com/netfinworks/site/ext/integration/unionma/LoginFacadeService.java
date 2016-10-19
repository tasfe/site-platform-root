package com.netfinworks.site.ext.integration.unionma;

import com.netfinworks.site.domain.domain.request.LoginNameEditRequest;
import com.netfinworks.site.domain.domain.request.LoginRequest;
import com.netfinworks.site.domain.domain.response.LoginNameEditResp;
import com.netfinworks.site.domain.domain.response.UnionmaBaseResponse;
import com.netfinworks.site.domain.exception.BizException;

public interface LoginFacadeService {

	/**
	 * 登录
	 * @param loginRequestParam
	 * @return
	 */
	public LoginNameEditResp login(LoginRequest loginRequest)throws BizException;
	/**
	 * 登录名修改
	 * @param LoginNameEditRequest
	 * @return
	 */

	public UnionmaBaseResponse loginNameEdit(LoginNameEditRequest loginNameEditRequest) throws BizException;
}
