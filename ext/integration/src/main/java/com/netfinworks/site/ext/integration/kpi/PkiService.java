package com.netfinworks.site.ext.integration.kpi;

import com.netfinworks.service.exception.ServiceException;

/**
 *
 * <p>证书服务</p>
 * @author qinde
 * @version $Id: PkiService.java, v 0.1 2013-12-16 下午1:33:13 qinde Exp $
 */
public interface PkiService {
    /**
     * 获取证书
     *
     * @return
     */
    public String getCertification() throws ServiceException;

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

}
