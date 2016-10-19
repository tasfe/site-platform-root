package com.netfinworks.site.ext.integration.security.impl;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.netfinworks.biz.common.util.QueryBase;
import com.netfinworks.cert.service.facade.ICertificationFacade;
import com.netfinworks.cert.service.model.CertInfo;
import com.netfinworks.cert.service.request.CertActivateRequest;
import com.netfinworks.cert.service.request.CertApplyRequest;
import com.netfinworks.cert.service.request.CertDetailRequest;
import com.netfinworks.cert.service.request.CertRelieveLossRequest;
import com.netfinworks.cert.service.request.CertRenewRequest;
import com.netfinworks.cert.service.request.CertReportLossRequest;
import com.netfinworks.cert.service.request.CertRevokeRequest;
import com.netfinworks.cert.service.request.CertUnbindRequest;
import com.netfinworks.cert.service.request.QueryCertsRequest;
import com.netfinworks.cert.service.response.BaseResponse;
import com.netfinworks.cert.service.response.CertApplyResponse;
import com.netfinworks.cert.service.response.CertDetailResponse;
import com.netfinworks.cert.service.response.CertRenewResponse;
import com.netfinworks.cert.service.response.QueryCertsResponse;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.util.JsonMapUtil;
import com.netfinworks.site.domain.domain.info.CertificationDetail;
import com.netfinworks.site.domain.domain.info.PageResultList;
import com.netfinworks.site.domain.domain.request.CertificationInfoRequest;
import com.netfinworks.site.domain.enums.CertificationStatus;
import com.netfinworks.site.domain.enums.CertificationType;
import com.netfinworks.site.domain.enums.ErrorCode;
import com.netfinworks.site.domain.enums.ResponseCode;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.ClientEnvironment;
import com.netfinworks.site.ext.integration.security.CertificationService;
import com.netfinworks.site.ext.integration.security.convert.CertificationConvert;

@Service("certificationService")
public class CertificationServiceImpl extends ClientEnvironment implements CertificationService {
	
	private Logger logger = LoggerFactory.getLogger(CertificationServiceImpl.class);
	
	@Resource(name = "certificationFacade")
	private ICertificationFacade certificationFacade;
	
	/**
	 * 分页条数
	 */
	private static final Integer DEFAULTPAGESIZE = 20;
	
	@Override
	public RestResponse certApply(CertificationInfoRequest req, OperationEnvironment environment) throws BizException {
		RestResponse restP = new RestResponse();
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			CertApplyRequest request = CertificationConvert.createCertApplyRequest(req);
			long beginTime = 0L;
			if (logger.isInfoEnabled()) {
				logger.info("证书申请，请求参数：{}", request);
				beginTime = System.currentTimeMillis();
			}
			CertApplyResponse response = certificationFacade.certApply(request, environment);
			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("远程调用证书申请， 耗时:{} (ms); 响应结果:{} ", new Object[] {
						consumeTime, response });
			}
			if(ResponseCode.SUCCESS.getCode().equals(response.getResultCode())) {
				restP.setSuccess(true);
				Map<String, String> result = JsonMapUtil.jsonToMapStr(response.getCertData());
				String certSerialNumber = result.get("certSerialNumber");
				String certSignBufP7 = result.get("certSignBufP7");
				data.put("certSerialNumber", certSerialNumber);
				data.put("certSignBufP7", certSignBufP7);
				restP.setData(data);
			} else {
				restP.setMessage(response.getReturnMessage());
				restP.setSuccess(false);
			}
		} catch(Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error("证书申请 {}信息异常:请求信息" + req, e);
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}
		}
		return restP;
	}

	@Override
	public PageResultList queryCerts(CertificationInfoRequest req, OperationEnvironment environment, Integer currentPage, Integer pageSize) throws BizException {
		try {
			QueryCertsRequest request = CertificationConvert.createQueryCertsRequest(req, currentPage, pageSize);
			long beginTime = 0L;
			if (logger.isInfoEnabled()) {
				logger.info("查询所有证书列表信息，请求参数：{}", request);
				beginTime = System.currentTimeMillis();
			}
			QueryCertsResponse response = certificationFacade.queryCerts(request, environment);
			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("远程调用查询所有证书列表信息， 耗时:{} (ms); 响应结果:{} ", new Object[] {
						consumeTime, response });
			}
			if(ResponseCode.SUCCESS.getCode().equals(response.getResultCode())) {
				PageResultList page = new PageResultList();
				List<CertInfo> result = response.getCertList();
				if(result != null && result.size() > 0) {
					page.setInfos(result);
				}
				QueryBase pageInfo = new QueryBase();
				pageInfo.setCurrentPage(currentPage);
				pageInfo.setPageSize(pageSize);
				pageInfo.setTotalItem(response.getTotalItem());
				page.setPageInfo(pageInfo);
				return page;
			}
		}catch(Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error("查询所有证书列表 {}信息异常:请求信息" + req, e);
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}
		}
		return null;
	}

	@Override
	public CertificationDetail certDetail(CertificationInfoRequest req, OperationEnvironment environment) throws BizException {
		CertificationDetail certificationDetail = new CertificationDetail();
		try {
			CertDetailRequest request = CertificationConvert.createCertDetailRequest(req);
			long beginTime = 0L;
			if (logger.isInfoEnabled()) {
				logger.info("查询单个证书详情信息，请求参数：{}", request);
				beginTime = System.currentTimeMillis();
			}
			CertDetailResponse response = certificationFacade.certDetail(request, environment);
			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("远程调用查询单个证书详情信息， 耗时:{} (ms); 响应结果:{} ", new Object[] {
						consumeTime, response });
			}
			if(ResponseCode.SUCCESS.getCode().equals(response.getResultCode())) {
				certificationDetail = CertificationConvert.convertCertificationDetail(response.getCertData());
			}
		} catch(Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error("查询单个证书详情 {}信息异常:请求信息" + req, e);
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}
		}
		return certificationDetail;
	}

	@Override
	public RestResponse certUnbind(CertificationInfoRequest req, OperationEnvironment environment) throws BizException {
		BaseResponse response = null;
		RestResponse restP = new RestResponse();
		restP.setSuccess(false);
		try {
			CertUnbindRequest request = CertificationConvert.createCertUnbindRequest(req);
			long beginTime = 0L;
			if (logger.isInfoEnabled()) {
				logger.info("证书解绑，请求参数：{}", request);
				beginTime = System.currentTimeMillis();
			}
			response = certificationFacade.certUnbind(request, environment);
			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("远程调用证书解绑， 耗时:{} (ms); 响应结果:{} ", new Object[] {
						consumeTime, response });
			}
			if(ResponseCode.SUCCESS.getCode().equals(response.getResultCode())) {
				restP.setSuccess(true);
			}
		}catch(Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error("证书解绑 {}信息异常:请求信息" + req, e);
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}
		}
		return restP;
	}

	@Override
	public RestResponse certReportLoss(CertificationInfoRequest req, OperationEnvironment environment) throws BizException {
		BaseResponse response = null;
		RestResponse restP = new RestResponse();
		restP.setSuccess(false);
		try {
			CertReportLossRequest request = CertificationConvert.createCertReportLossRequest(req);
			long beginTime = 0L;
			if (logger.isInfoEnabled()) {
				logger.info("证书挂失，请求参数：{}", request);
				beginTime = System.currentTimeMillis();
			}
			response = certificationFacade.certReportLoss(request, environment);
			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("远程调用证书挂失， 耗时:{} (ms); 响应结果:{} ", new Object[] {
						consumeTime, response });
			}
			if(ResponseCode.SUCCESS.getCode().equals(response.getResultCode())) {
				restP.setSuccess(true);
			}
		}catch(Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error("证书挂失 {}信息异常:请求信息" + req, e);
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}
		}
		return restP;
	}

	@Override
	public RestResponse certRelieveLoss(CertificationInfoRequest req, OperationEnvironment environment) throws BizException {
		BaseResponse response = null;
		RestResponse restP = new RestResponse();
		restP.setSuccess(false);
		try {
			CertRelieveLossRequest request = CertificationConvert.createCertRelieveLossRequest(req);
			long beginTime = 0L;
			if (logger.isInfoEnabled()) {
				logger.info("证书解除挂失，请求参数：{}", request);
				beginTime = System.currentTimeMillis();
			}
			response = certificationFacade.certRelieveLoss(request, environment);
			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("远程调用证书解除挂失， 耗时:{} (ms); 响应结果:{} ", new Object[] {
						consumeTime, response });
			}
			if(ResponseCode.SUCCESS.getCode().equals(response.getResultCode())) {
				restP.setSuccess(true);
			}
		}catch(Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error("证书解除挂失 {}信息异常:请求信息" + req, e);
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}
		}
		return restP;
	}

	@Override
	public RestResponse certRevoke(CertificationInfoRequest req, OperationEnvironment environment) throws BizException {
		BaseResponse response = null;
		RestResponse restP = new RestResponse();
		restP.setSuccess(false);
		try {
			CertRevokeRequest request = CertificationConvert.createCertRevokeRequest(req);
			long beginTime = 0L;
			if (logger.isInfoEnabled()) {
				logger.info("证书注销，请求参数：{}", request);
				beginTime = System.currentTimeMillis();
			}
			response = certificationFacade.certRevoke(request, environment);
			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("远程调用证书注销， 耗时:{} (ms); 响应结果:{} ", new Object[] {
						consumeTime, response });
			}
			if(ResponseCode.SUCCESS.getCode().equals(response.getResultCode())) {
				restP.setSuccess(true);
			}
		}catch(Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error("证书注销 {}信息异常:请求信息" + req, e);
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}
		}
		return restP;
	}

	@Override
	public RestResponse certActivate(CertificationInfoRequest req, OperationEnvironment environment) throws BizException {
		BaseResponse response = null;
		RestResponse restP = new RestResponse();
		restP.setSuccess(false);
		try {
			CertActivateRequest request = CertificationConvert.createCertActivateRequest(req);
			long beginTime = 0L;
			if (logger.isInfoEnabled()) {
				logger.info("证书激活，请求参数：{}", request);
				beginTime = System.currentTimeMillis();
			}
			response = certificationFacade.certActivate(request, environment);
			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("远程调用证书激活， 耗时:{} (ms); 响应结果:{} ", new Object[] {
						consumeTime, response });
			}
			if(ResponseCode.SUCCESS.getCode().equals(response.getResultCode())) {
				restP.setSuccess(true);
			}
		}catch(Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error("证书激活{}信息异常:请求信息" + req, e);
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}
		}
		return restP;
	}
	
	private void validate(CertificationInfoRequest req) {
        // 参数验证
        Assert.notNull(req.getMemberId(), "memberId不能为null");
        Assert.notNull(req.getOperatorId(), "operatorId不能为null");
//        Assert.notNull(req.getCertificationType(), "certificationType不能为空");
    }
	
	@Override
	public RestResponse getCertByCertStatus(CertificationInfoRequest req, OperationEnvironment environment) throws BizException {
		RestResponse restP = new RestResponse();
		restP.setSuccess(false);
		List<String> certSns = new ArrayList<String>();
		List<String> csrs = new ArrayList<String>();
		List<CertificationDetail> certificationDetails = new ArrayList<CertificationDetail>();
		Map<String, Object> data =new HashMap<String, Object>();
		try{
			//参数校验
			validate(req);
			Integer currentPage = 1;
			while(true) {
				//分页查询证书列表信息，每次查询20条记录
				PageResultList pageResultLists = queryCerts(req, environment, currentPage, DEFAULTPAGESIZE);
				if(pageResultLists == null || "".equals(pageResultLists)) {
					return restP;
				}
				List<CertInfo> certInfos = pageResultLists.getInfos();
				if(certInfos == null || certInfos.size() == 0) {
					break;
				}
				for(CertInfo certInfo : certInfos) {
					String certSn = certInfo.getCertSn();
					String csr = certInfo.getCsr();
					certSns.add(certSn);
					csrs.add(csr);
					certificationDetails.add(CertificationConvert.convertCertificationDetail(certInfo));
					restP.setSuccess(true);
					data.put("certSns", certSns);
					data.put("csrs", csrs);
					data.put("certificationDetails", certificationDetails);
					restP.setData(data);
				}
				currentPage++;
			}
		} catch(Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error("查询证书序列号{}信息异常:请求信息" + req, e);
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}
		}
		return restP;
	}

	@Override
	public CertRenewResponse certRenew(CertificationInfoRequest req, OperationEnvironment environment) throws BizException {
		CertRenewResponse response = null;
		try{
			CertRenewRequest request = CertificationConvert.createCertRenewRequest(req);
			long beginTime = 0L;
			if (logger.isInfoEnabled()) {
				logger.info("证书更新，请求参数：{}", request);
				beginTime = System.currentTimeMillis();
			}
			response = certificationFacade.certRenew(request, environment);
			if (logger.isInfoEnabled()) {
				long consumeTime = System.currentTimeMillis() - beginTime;
				logger.info("远程调用证书更新， 耗时:{} (ms); 响应结果:{} ", new Object[] {
						consumeTime, response });
			}
			
		} catch(Exception e) {
			if (e instanceof BizException) {
				throw (BizException) e;
			} else {
				logger.error("证书更新{}信息异常:请求信息" + req, e);
				throw new BizException(ErrorCode.SYSTEM_ERROR);
			}
		}
		return response;
	}
}
