package com.netfinworks.site.ext.integration.unionma;


import com.kjt.unionma.api.password.response.PayPasswordEditResponse;
import com.netfinworks.site.domain.domain.request.PayPasswordCheckReq;
import com.netfinworks.site.domain.domain.request.PayPasswordEditReq;
import com.netfinworks.site.domain.domain.request.PayPasswordSetReq;
import com.netfinworks.site.domain.domain.response.UnionmaBaseResponse;
import com.netfinworks.site.domain.exception.BizException;

public interface PayPasswordService {
	
	/**
	 * 修改支付密码
	 * @param payPasswordEditRequestParam
	 * @return
	 */
	public UnionmaBaseResponse payPasswordEdit (PayPasswordEditReq req)throws BizException;
	
	
	
	/**
	 * 设置支付密码
	 * @param PayPasswordSetReq
	 * @return
	 */
	public UnionmaBaseResponse payPasswordSet (PayPasswordSetReq req)throws BizException;
	
	/**
	 * 验证支付密码
	 * @param req
	 * @return
	 * @throws BizException
	 */
	public UnionmaBaseResponse payPasswordCheck (PayPasswordCheckReq req)throws BizException;
}
