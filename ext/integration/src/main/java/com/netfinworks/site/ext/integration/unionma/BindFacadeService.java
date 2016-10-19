package com.netfinworks.site.ext.integration.unionma;


import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.domain.domain.request.UnionmaBindEmailRequest;
import com.netfinworks.site.domain.domain.request.UnionmaBindMobileEditRequest;
import com.netfinworks.site.domain.domain.request.UnionmaCheckEmailTokenRequest;
import com.netfinworks.site.domain.domain.request.UnionmaUnBindEmailRequest;
import com.netfinworks.site.domain.domain.request.UnionmaVerifyAuthCodeRequest;
import com.netfinworks.site.domain.domain.response.UnionmaBaseResponse;
import com.netfinworks.site.domain.domain.response.UnionmaBindEmailResponse;
import com.netfinworks.site.domain.domain.response.UnionmaBindMobileEditResponse;
import com.netfinworks.site.domain.exception.BizException;

public interface BindFacadeService {
	/**
	 * 修改绑定手机
	 * 
	 * @param request
	 * @return
	 */

	public UnionmaBindMobileEditResponse bindMobileEdit(UnionmaBindMobileEditRequest req)throws BizException;
	
	/**
	 * 校验验证码
	 * 
	 * @param request
	 * @return
	 */
	public UnionmaBaseResponse verifyAuthCode(UnionmaVerifyAuthCodeRequest request,OperationEnvironment env)throws BizException;
	
	/**
	 * 绑定邮箱
	 * 
	 * @param request
	 * @return
	 */
	public UnionmaBindEmailResponse bindEmail(UnionmaBindEmailRequest request)throws BizException;

	/**
	 * 验证邮箱凭证
	 * 
	 * @param request
	 * @return
	 */
	public UnionmaBaseResponse checkEmailToken(UnionmaCheckEmailTokenRequest request)throws BizException;

	/**
	 * 解绑邮箱
	 * 
	 * @param request
	 * @return
	 */
	public UnionmaBaseResponse unBindEmail(UnionmaUnBindEmailRequest request)throws BizException;

	
}
