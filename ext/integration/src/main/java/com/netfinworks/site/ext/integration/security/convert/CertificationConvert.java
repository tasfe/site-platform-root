package com.netfinworks.site.ext.integration.security.convert;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

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
import com.netfinworks.site.domain.domain.info.CertificationDetail;
import com.netfinworks.site.domain.domain.info.TradeInfo;
import com.netfinworks.site.domain.domain.request.CertificationInfoRequest;
import com.netfinworks.site.domain.enums.AcqTradeType;
import com.netfinworks.site.domain.enums.CertificationType;
import com.netfinworks.tradeservice.facade.model.query.TradeBasicInfo;

public class CertificationConvert {
	
	private static final String ORG = "itrus";
	
	private static final String ORG_UNIT = "RA";
	
	private static final String RENEW_CERTVALIDITY = "365";
	/**
	 * 创建数字证书申请的request
	 * @param req
	 * @return
	 */
	public static CertApplyRequest createCertApplyRequest(CertificationInfoRequest req) {
		CertApplyRequest request = new CertApplyRequest();
		request.setMemberId(req.getMemberId());
		request.setOperatorId(req.getOperatorId());
		request.setCsr(req.getCsr());
		if(req.getPosition() != null && !"".equals(req.getPosition())) {
			request.setPosition(req.getPosition());
		}
		if(req.getUserIp() != null && !"".equals(req.getUserIp())) {
			request.setUserIp(req.getUserIp());
		}
		if(req.getUserMac() != null && !"".equals(req.getUserMac())) {
			request.setUserMac(req.getUserMac());
		}
		request.setRequestTime(req.getRequestTime());
		if(req.getMobileNo() != null && !"".equals(req.getMobileNo())) {
			request.setMobileNo(req.getMobileNo());
		}
		if(req.getSerialNo() != null && !"".equals(req.getSerialNo())) {
			request.setSerialNo(req.getSerialNo());
		}
		request.setCertificationType(req.getCertificationType());
		request.setAccountHash(req.getAccountHash());
		request.setOrg(ORG);
		request.setOrgUnit(ORG_UNIT);
		return request;
	}
	
	/**
	 * 创建查询所有证书列表的request
	 * @param req
	 * @return
	 */
	public static QueryCertsRequest createQueryCertsRequest(CertificationInfoRequest req, Integer currentPage ,Integer pageSize) {
		QueryCertsRequest request = new QueryCertsRequest();
		request.setMemberId(req.getMemberId());
		request.setOperatorId(req.getOperatorId());
		request.setCurrentPage(currentPage);
		request.setPageSize(pageSize);
		request.setCertificationType(req.getCertificationType());
		if(req.getCertificationStatus() != null && !"".equals(req.getCertificationStatus())) {
			request.setCertificationStatus(req.getCertificationStatus());
		}
		return request;
	}
	
	/**
	 * 创建查询单个证书详情的request
	 * @param req
	 * @return
	 */
	public static CertDetailRequest createCertDetailRequest(CertificationInfoRequest req) {
		CertDetailRequest request = new CertDetailRequest();
		request.setMemberId(req.getMemberId());
		request.setOperatorId(req.getOperatorId());
		request.setCertSn(req.getCertSn());
		return request;
	}
	
	/**
	 * 创建证书解绑的request
	 * @param req
	 * @return
	 */
	public static CertUnbindRequest createCertUnbindRequest(CertificationInfoRequest req) {
		CertUnbindRequest request = new CertUnbindRequest();
		request.setMemberId(req.getMemberId());
		request.setOperatorId(req.getOperatorId());
		request.setCertificationType(req.getCertificationType());
		request.setCertSn(req.getCertSn());
		return request;
	}
	
	/**
	 * 创建证书挂失的request
	 * @param req
	 * @return
	 */
	public static CertReportLossRequest createCertReportLossRequest(CertificationInfoRequest req) {
		CertReportLossRequest request = new CertReportLossRequest();
		request.setMemberId(req.getMemberId());
		request.setOperatorId(req.getOperatorId());
		request.setCertSn(req.getCertSn());
		return request;
	}
	
	/**
	 * 创建证书解除挂失的requst
	 * @param req
	 * @return
	 */
	public static CertRelieveLossRequest createCertRelieveLossRequest(CertificationInfoRequest req) {
		CertRelieveLossRequest request = new CertRelieveLossRequest();
		request.setMemberId(req.getMemberId());
		request.setOperatorId(req.getOperatorId());
		request.setCertSn(req.getCertSn());
		return request;
	}
	
	/**
	 * 创建证书注销的request
	 * @param req
	 * @return
	 */
	public static CertRevokeRequest createCertRevokeRequest(CertificationInfoRequest req) {
		CertRevokeRequest request = new CertRevokeRequest();
		request.setMemberId(req.getMemberId());
		request.setCertSn(req.getCertSn());
		request.setOperatorId(req.getOperatorId());
		request.setReason(req.getReason());
		return request;
	}
	
	/**
	 * 创建证书激活的request
	 * @param req
	 * @return
	 */
	public static CertActivateRequest createCertActivateRequest(CertificationInfoRequest req) {
		CertActivateRequest request = new CertActivateRequest();
		request.setMemberId(req.getMemberId());
		request.setOperatorId(req.getOperatorId());
		request.setCertSn(req.getCertSn());
		return request;
	}
	
	public static CertRenewRequest createCertRenewRequest(CertificationInfoRequest req) {
		CertRenewRequest request = new CertRenewRequest();
		request.setMemberId(req.getMemberId());
		request.setOperatorId(req.getOperatorId());
		request.setCertSn(req.getCertSn());
		request.setOrigCert(req.getOrigCert());
		request.setPkcsInfo(req.getPkcsInfo());
		request.setCsr(req.getCsr());
		request.setCertValidity(RENEW_CERTVALIDITY);
		return request;
	}
	/**
	 * 转换证书详情
	 * @param certInfo
	 * @return
	 */
	public static CertificationDetail convertCertificationDetail(CertInfo certInfo){
		CertificationDetail certificationDetail = new CertificationDetail();
		if(certInfo != null) {
			BeanUtils.copyProperties(certInfo, certificationDetail);
		}
		return certificationDetail;
	}
	
	/**
	 * 转换证书列表
	 * @param list
	 * @return
	 */
	public static List<CertificationDetail> convertCertificationDetailList(List<CertInfo> list) {
		List<CertificationDetail> result = new ArrayList<CertificationDetail>();
		if (list != null && list.size() > 0) {
			for (CertInfo info : list) {
				CertificationDetail certificationDetail = convertCertificationDetail(info); 
				result.add(certificationDetail);
			}
		}
		return result;
	}
}
