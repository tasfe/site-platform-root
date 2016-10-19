package com.netfinworks.site.domainservice.pki.impl;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.core.common.util.RadomUtil;
import com.netfinworks.site.domain.domain.member.BaseMember;
import com.netfinworks.site.domainservice.pki.DefaultPkiService;
import com.netfinworks.site.ext.integration.kpi.PkiService;
import com.netfinworks.ues.util.UesUtils;

/**
 *
 * <p>PKI解密</p>
 * @author qinde
 * @version $Id: DefaultPfsBaseServiceImpl.java, v 0.1 2013-11-28 上午11:10:03 qinde Exp $
 */
@Service("defaultPkiService")
public class DefaultPKIServiceImpl implements DefaultPkiService {
    public static final int SALT_LENGTH = 32;

    @Resource(name = "pkiService")
    private PkiService pkiService;

    @Override
    public String getCertification() throws ServiceException {
        String base64Cert = pkiService.getCertification();
        if (base64Cert != null) {
            return "-----BEGIN CERTIFICATE-----\n" + base64Cert
                    + "\n-----END CERTIFICATE-----";
        }
        return null;
    }

    @Override
    public String decryptRSA(String base64EnvelopedData, String encryptedData, String certSerialNo,
                             String encryptType) throws ServiceException {
        return pkiService.decryptRSA(base64EnvelopedData, encryptedData, certSerialNo, encryptType);
    }

    @Override
    public String decryptRSA(String encryptedData) throws ServiceException {
        return pkiService.decryptRSA(encryptedData);
    }

    @Override
    public String decryptAES(String key, String value) throws ServiceException {
        return pkiService.decryptAES(key, value);
    }

    /**
     * 验证支付密码
     *
     * @param user
     * @param paypwdKey
     * @param paypwdValue
     * @return
     */
    @Override
    public String validate(BaseMember user, String paypwdKey, String paypwdValue, String salt) throws ServiceException {
        return validate(user, paypwdKey, paypwdValue, salt, null, null);
    }

    private String validate(BaseMember user, String paypwdKey, String paypwdValue,String salt, String certSerialNo,
            String encryptType) throws ServiceException{
        if (StringUtils.isEmpty(paypwdKey) || StringUtils.isEmpty(paypwdValue)) {
            return null;
        }
        String paypwd = null;
        String saltPaser = null;

        if (paypwdKey.length() == SALT_LENGTH) {
            // AES解密
            // salt = paypwdKey;
            // paypwd = pkiService.decryptAES(paypwdKey, paypwdValue);

            // RSA解密
            String saltpaypwd = pkiService.decryptRSA(paypwdValue);
            if (saltpaypwd.length() > SALT_LENGTH) {
                salt = saltpaypwd.substring(0, SALT_LENGTH);
                paypwd = saltpaypwd.substring(SALT_LENGTH);
            }
        } else {
            if (StringUtils.isEmpty(certSerialNo)) {
                String base64Cert = pkiService.getCertification();
                certSerialNo = UesUtils.getSerialNoFromBase64Cert(base64Cert);
            }
            String saltpaypwd = pkiService.decryptRSA(paypwdKey, paypwdValue, certSerialNo, encryptType);
            if (saltpaypwd.length() > SALT_LENGTH) {
                saltPaser = saltpaypwd.substring(0, SALT_LENGTH);
                paypwd = saltpaypwd.substring(SALT_LENGTH);
            }
        }
        if (!validateSalt(saltPaser, salt)) {

            return null;
        }
        if (StringUtils.isEmpty(paypwd)) {
            return null;
        }
        return paypwd;
    }
    
    private String validatelogin(String loginpwdKey, String loginpwdValue,String salt, String certSerialNo,
    		String encryptType) throws ServiceException{
    	if (StringUtils.isEmpty(loginpwdKey) || StringUtils.isEmpty(loginpwdValue)) {
    		return null;
    	}
    	String loginpwd = null;
    	String saltPaser = null;
    	
    	if (loginpwdKey.length() == SALT_LENGTH) {
    		// AES解密
    		// salt = paypwdKey;
    		// paypwd = pkiService.decryptAES(paypwdKey, paypwdValue);
    		
    		// RSA解密
    		String saltloginpwd = pkiService.decryptRSA(loginpwdValue);
    		if (saltloginpwd.length() > SALT_LENGTH) {
    			salt = saltloginpwd.substring(0, SALT_LENGTH);
    			loginpwd = saltloginpwd.substring(SALT_LENGTH);
    		}
    	} else {
    		if (StringUtils.isEmpty(certSerialNo)) {
    			String base64Cert = pkiService.getCertification();
    			certSerialNo = UesUtils.getSerialNoFromBase64Cert(base64Cert);
    		}
    		String saltloginpwd = pkiService.decryptRSA(loginpwdKey, loginpwdValue, certSerialNo, encryptType);
    		if (saltloginpwd.length() > SALT_LENGTH) {
    			saltPaser = saltloginpwd.substring(0, SALT_LENGTH);
    			loginpwd = saltloginpwd.substring(SALT_LENGTH);
    		}
    	}
    	if (!validateSalt(saltPaser, salt)) {
    		
    		return null;
    	}
    	if (StringUtils.isEmpty(loginpwd)) {
    		return null;
    	}
    	return loginpwd;
    }


    @Override
    public String getSalt() {
        long salt = RadomUtil.createRadom();
        String saltStr = String.valueOf(salt);
        return UesUtils.hashSignContent(saltStr).substring(0,SALT_LENGTH);
    }

    /**
     * 验证salt
     *
     * @param salt
     * @return
     */
    public boolean validateSalt(String saltPaser,String salt) {
        return salt.equals(saltPaser);
    }

	@Override
	public String validateLoginPasswd( String loginpwdKey,
			String loginpwdValue, String salt) throws ServiceException {
		return validatelogin(loginpwdKey, loginpwdValue, salt, null, null);
	}

}
