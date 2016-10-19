package com.netfinworks.site.ext.integration.unionma;

import com.netfinworks.site.domain.domain.request.UnionmaPerfectionIdentityReq;
import com.netfinworks.site.domain.domain.request.UnionmaRegisterRequest;
import com.netfinworks.site.domain.domain.request.UnionmaSetPaymentReq;
import com.netfinworks.site.domain.domain.response.UnionmaBaseResponse;
import com.netfinworks.site.domain.domain.response.UnionmaRegisterResponse;
import com.netfinworks.site.domain.exception.BizException;

public interface UnionmaRegisterService {
	/**
	 * 系统注册激活
	 * @param 
	 * @return
	 */
	public UnionmaRegisterResponse register(UnionmaRegisterRequest request)throws BizException;
	/**
	 * 完善身份信息
	 * @param 
	 * @return
	 */
	public UnionmaBaseResponse perfectionIdentity(UnionmaPerfectionIdentityReq request)throws BizException;
	/**
	 * 设置支付方式
	 * @param 
	 * @return
	 */
	public UnionmaBaseResponse setPayment(UnionmaSetPaymentReq request)throws BizException;

	/**
	 * 设置登陆支付密码
	 * @param 
	 * @return
	 */
//	public UnionmaRegisterResponse registerSetPwdAndPayPwd(RegisterSetPwdAndPayPwdRequestParam registerPPwdRequestParam)throws BizException;
}
