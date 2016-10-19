package com.yongda.site.ext.service.facade.personal.common;

import org.apache.commons.lang.StringUtils;

import com.netfinworks.cert.service.model.AuthInfo;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.domain.request.AuthInfoRequest;
import com.netfinworks.site.domain.enums.AuthType;
import com.netfinworks.site.domainservice.spi.DefaultCertService;

public class PersonalCommonQueryRealName {
	/**
	 * 根据认证类型查询认证结果
	 * @param memberId
	 * @param operatorId
	 * @param authType
	 * @return
	 * @throws ServiceException
	 */
	public static String queryRealName(String memberId, String operatorId, AuthType authType,DefaultCertService defaultCertService) throws ServiceException {
		AuthInfoRequest authInfoReq = new AuthInfoRequest();
		authInfoReq.setMemberId(memberId);
		authInfoReq.setAuthType(authType);
		authInfoReq.setOperator(operatorId);

		AuthInfo info = defaultCertService.queryRealName(authInfoReq);

		if ((info != null) && (info.getResult()!=null)) {
			return info.getResult().getCode();
		}

		return StringUtils.EMPTY;
	}
	
	public static AuthInfo queryUserInfo(String memberId, String operatorId, AuthType authType,DefaultCertService defaultCertService) throws ServiceException {
		AuthInfoRequest authInfoReq = new AuthInfoRequest();
		authInfoReq.setMemberId(memberId);
		authInfoReq.setAuthType(authType);
		authInfoReq.setOperator(operatorId);

		AuthInfo info = defaultCertService.queryRealName(authInfoReq);

		return info;
	}
}
