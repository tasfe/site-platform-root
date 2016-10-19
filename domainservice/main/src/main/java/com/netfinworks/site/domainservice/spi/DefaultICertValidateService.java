package com.netfinworks.site.domainservice.spi;

import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.domain.auth.ICertValidateVO;
/** 
 * <p>国政通实名认证</p>
 * 
 * @author tangL
 * @date 2014年12月26日
 * @since 1.6
 */ 
public interface DefaultICertValidateService {
	/**
	 * 验证当前身份信息 和 代办人信息
	 * @param environment 
	 * @param iCertValidateVo 验证vo对象
	 * @return boolean
	 * @throws ServiceException
	 */
	public boolean validPersonAndProxyPerson(OperationEnvironment environment, ICertValidateVO iCertValidateVo) throws ServiceException;
	/**
	 * 验证身份信息
	 * @param environment
	 * @param realName
	 * @param idCard
	 * @return boolean
	 * @throws ServiceException
	 */
	public boolean IdCardValidate(OperationEnvironment environment, String realName, String idCard) throws ServiceException;
}
