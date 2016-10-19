package com.netfinworks.site.domainservice.spi.impl;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.netfinworks.cert.service.model.enums.CertValidateType;
import com.netfinworks.cert.service.request.CertValidateRequest;
import com.netfinworks.common.domain.OperationEnvironment;
import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.util.SampleExceptionUtils;
import com.netfinworks.site.domain.domain.auth.ICertValidateVO;
import com.netfinworks.site.domain.enums.CertType;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.domainservice.spi.DefaultICertValidateService;
import com.netfinworks.site.ext.integration.member.ICertValidateService;
/** 
 * <p>国政通实名认证</p>
 * 
 * @author tangL
 * @date 2014年12月26日
 * @since 1.6
 */ 
@Service("defaultICertValidateService")
public class DefaultICertValidateServiceImpl implements DefaultICertValidateService {
	protected Logger log = LoggerFactory.getLogger(DefaultICertValidateServiceImpl.class);

    @Resource(name = "iCertValidateService")
    private ICertValidateService iCertValidateService;

	@Override
	public boolean validPersonAndProxyPerson(OperationEnvironment environment, ICertValidateVO iCertValidateVO) throws ServiceException {
		String realName   = iCertValidateVO.getName(),
				idCard    = iCertValidateVO.getIdCard(),
				cardType1 = iCertValidateVO.getCardType1();
				
		boolean isDbrFlag = iCertValidateVO.getIsDbrFlag();
		boolean isSuccess = true;
		/**
		 * 返回错误信息 抛出异常
		 * 这里使用 "|" 来隔离错误消息 （用户认证失败|认证代理人身份信息失败)
		 * 如果其中只有一个错误那么 返回的结果就是 (用户认证失败|) 或者 (|认证代理人身份信息失败)
		 */
		String rstErrorMsg = "|"; 
		if (CertType.ID_CARD.getCode().equals(cardType1)) {
			try {
				this.IdCardValidate(environment, realName, idCard);
			} catch (Exception e) {
				isSuccess = false;
				log.error(e.getMessage(), e.getCause());
				
	            String msg = SampleExceptionUtils.addAfterIfTipMsg("认证用户身份失败", e.getMessage());
	            rstErrorMsg = msg + rstErrorMsg;
			}
		} else {
			CertType certType = CertType.getByCode(cardType1);
			String name = (certType == null ? "" : certType.getMessage());
			log.info(String.format("用户[%s],身份证类型为[%s]:不需用进行国政通认证", realName, name));
		}
		// 如果添加代理人则验证代理人信息
		if (isDbrFlag) {
			String proxyPeraonName   = iCertValidateVO.getProxyPersonName(),
				   proxyPersonIdcard = iCertValidateVO.getProxyPersonIdCard(),
				   cardType2		 = iCertValidateVO.getCardType2();

			if (CertType.ID_CARD.getCode().equals(cardType2)) {
				try {
					this.IdCardValidate(environment, proxyPeraonName, proxyPersonIdcard);
				} catch (Exception e) {
					isSuccess = false;
					log.error(e.getMessage(), e.getCause());
					
		            String msg  = SampleExceptionUtils.addAfterIfTipMsg("认证代办人身份信息失败", e.getMessage());
		            rstErrorMsg = rstErrorMsg + msg;
				}
			} else {
				CertType certType = CertType.getByCode(cardType2);
				String name = (certType == null ? "" : certType.getMessage());
				log.info(String.format("代理人[%s],身份证类型为[%s]:不需用进行国政通认证", proxyPeraonName, name));
			}
		}

		if (!isSuccess) {
			throw new ServiceException(rstErrorMsg);
		}
		
		return isSuccess;
	}

	@Override
	public boolean IdCardValidate(OperationEnvironment environment, String realName, String idCard) throws ServiceException {
		log.info("实名认证用户身份信息, realName : {}, idCard : {} ", realName, idCard);
		
		Assert.notNull(realName);
		Assert.notNull(idCard);
		
		CertValidateRequest cvr = new CertValidateRequest();
		cvr.setCertType(CertValidateType.ID_CARD);
		cvr.setIdentityCard(idCard);
		cvr.setRealName(realName);
		
		try {
			iCertValidateService.validate(environment, cvr);
			return true;
		} catch (BizException e) {
			throw new ServiceException(e);
		}
	}
}
