package com.netfinworks.site.domainservice.pki;

import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.domain.domain.member.BaseMember;

/**
 *
 * <p>证书服务</p>
 * @author qinde
 * @version $Id: PkiService.java, v 0.1 2013-12-16 下午1:33:13 qinde Exp $
 */
public interface DefaultPkiService {
    /**
     * 获取证书
     *
     * @return
     */
    public String getCertification() throws ServiceException;

    /**
     * 获取盐值
     * @return
     * @throws ServiceException
     */
    public String getSalt() throws ServiceException;

    /**
     * 控件RSA解密
     *
     * @param base64EnvelopedData
     * @param encryptedData
     * @param certSerialNo
     * @param encryptType
     * @return
     */
    public String decryptRSA(String base64EnvelopedData, String encryptedData, String certSerialNo,
                             String encryptType) throws ServiceException;

    /**
     * 非控件RSA解密
     *
     * @param encryptedData
     * @return
     */
    public String decryptRSA(String encryptedData) throws ServiceException;

    /**
     * 非控件AES解密
     *
     * @param key
     * @param value
     * @return
     */
    public String decryptAES(String key, String value) throws ServiceException;

    /**
     * 验证支付密码
     *
     * @param user
     * @param paypwdKey
     * @param paypwdValue
     * @return
     */
    public String validate(BaseMember user, String paypwdKey, String paypwdValue, String salt) throws ServiceException;
    
    /**
     * 验证登录密码
     *
     * @param user
     * @param paypwdKey
     * @param paypwdValue
     * @return
     */
    public String validateLoginPasswd( String loginpwdKey, String loginpwdValue, String salt) throws ServiceException;

}
