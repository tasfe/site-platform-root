/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.domainservice.spi;


import java.util.List;

import com.netfinworks.cert.service.model.AuthInfo;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.domain.bank.BankAccRequest;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.site.domain.domain.response.VerifyAmountResponse;
import com.netfinworks.site.domain.enums.AuthResultStatus;
import com.netfinworks.site.domain.exception.BizException;

/**
 * 通用说明： 支付密码service
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-20 上午9:17:08
 *
 */
public interface DefaultCertService {

    /**
     * 实名认证
     *
     * @param ctx
     * @return
     * @throws ServiceException
     */
    public boolean certification(AuthInfoRequest info) throws ServiceException;

    /**
     * 查询实名认证信息
     *
     * @param info
     * @return
     * @throws BizException
     */
    public AuthInfo queryRealName(AuthInfoRequest info) throws ServiceException;

    /**
     * 查询认证状态
     *
     * @param info
     * @return
     * @throws BizException
     */
    public AuthResultStatus queryAuthType(AuthInfoRequest info) throws ServiceException;

	/**
	 * 更改实名认证状态为成功
	 * 
	 * @param ctx
	 * @return
	 * @throws ServiceException
	 */
	public boolean verify(AuthInfoRequest info) throws ServiceException;

	/**
	 * 身份验证
	 * 
	 * @param memberId
	 * @param operatorId
	 * @param realName
	 * @param idCard
	 * @param idType
	 * @param environment
	 * @return
	 * @throws ServiceException
	 */
	public boolean verifyRealName(String memberId, String operatorId, String realName, String idCard, String idType,
			OperationEnvironment environment) throws ServiceException;
	
	/**
	 * 银行卡验证
	 * 
	 * @param bankAccRequest
	 * @param environment
	 * @return
	 * @throws ServiceException
	 */
	public boolean verifyBankCard(BankAccRequest bankAccRequest, OperationEnvironment environment)
			throws ServiceException;

	/**
	 * 打款验证
	 * 
	 * @param memberId
	 * @param operatorId
	 * @param amount
	 * @return
	 * @throws ServiceException
	 */
	public VerifyAmountResponse verifyAmount(String memberId, String operatorId, String amount) throws ServiceException;

	/**
	 * 撤销银行卡认证
	 * 
	 * @param memberId
	 * @param operatorId
	 * @return
	 * @throws ServiceException
	 */
	public boolean cancelBankVerify(String memberId, String operatorId) throws ServiceException;

	/**
	 * 缓存保存更新操作
	 * 
	 * @param key
	 * @param member
	 * @return
	 */
	public String saveOrUpdate(String key, String member);
	
	/**
	 * 修改认证状态
	 * 
	 * @param orderNoList
	 * @param operatorId
	 * @param isChecked
	 * @param result
	 * @return
	 * @throws ServiceException
	 */
	public boolean modifyAuthStatus(List<String> orderNoList, String operatorId, boolean isChecked, boolean result)
			throws ServiceException;

}
