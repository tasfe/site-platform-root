package com.netfinworks.site.ext.integration.security;

import com.netfinworks.cert.service.response.CertRenewResponse;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.domain.domain.info.CertificationDetail;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.request.CertificationInfoRequest;
import com.netfinworks.site.domain.exception.BizException;

public interface CertificationService {
	
	/**
	 * 证书申请
	 * @param req
	 * @param environment
	 * @throws BizException
	 */
	public RestResponse certApply(CertificationInfoRequest req, OperationEnvironment environment) throws BizException;
	
	/**
	 * 查询所有证书列表
	 * @param request
	 * @param environment
	 * @throws BizException
	 */
	public PageResultList queryCerts(CertificationInfoRequest request, OperationEnvironment environment, Integer currentPage, Integer pageSize) throws BizException;
	
	/**
	 * 证书详情查询
	 * @param request
	 * @param environment
	 * @return	CertificationDetail 证书详情信息
	 */
	public CertificationDetail certDetail(CertificationInfoRequest request, OperationEnvironment environment) throws BizException;
	
	/**
	 * 证书解绑
	 * @param request
	 * @param environment
	 */
	public RestResponse certUnbind(CertificationInfoRequest request, OperationEnvironment environment) throws BizException;
	
	/**
	 * 证书挂失
	 * @param request
	 * @param environment
	 */
	public RestResponse certReportLoss(CertificationInfoRequest request, OperationEnvironment environment) throws BizException;
	
	/**
	 * 证书解除挂失
	 * @param request
	 * @param environment
	 */
	public RestResponse certRelieveLoss(CertificationInfoRequest request, OperationEnvironment environment) throws BizException;
	
	/**
	 * 证书注销
	 * @param request
	 * @param environment
	 */
	public RestResponse certRevoke(CertificationInfoRequest request, OperationEnvironment environment) throws BizException;
	
	/**
	 * 证书激活
	 * @param request
	 * @param environment
	 */
	public RestResponse certActivate(CertificationInfoRequest request, OperationEnvironment environment) throws BizException;
	
	/**
	 * @param request
	 * @param environment
	 * @return
	 * @throws BizException
	 */
	public RestResponse getCertByCertStatus(CertificationInfoRequest request, OperationEnvironment environment)throws BizException;
	
	/**
	 * 证书更新
	 * @param request
	 * @param environment
	 * @return
	 * @throws BizException
	 */
	public CertRenewResponse certRenew(CertificationInfoRequest request, OperationEnvironment environment) throws BizException;
}

