package com.netfinworks.site.ext.integration.kpi.impl;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.annotation.Resource;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import com.netfinworks.service.exception.ServiceException;
import com.netfinworks.site.ext.integration.kpi.PkiService;
import com.netfinworks.ues.util.UesUtils;

/**
 *
 * <p>PKI 解密服务</p>
 * @author qinde
 * @version $Id: PkiServiceImpl.java, v 0.1 2013-12-16 下午1:34:25 qinde Exp $
 */
@Service("pkiService")
public class PkiServiceImpl implements PkiService, InitializingBean {

    @Resource(name = "payPasswdPfx")
    private RSAKeyHolder    keyHolder;

    private X509Certificate x509Cert;
    private PrivateKey      privateKey;

    @Override
    public void afterPropertiesSet() throws Exception {
        x509Cert = UesUtils.getX509CertFromPfxFile(keyHolder.getPfxFile(), keyHolder.getPassword());
        privateKey = UesUtils.getPrivateKeyByPfxFile(keyHolder.getPfxFile(), keyHolder.getPassword());
        Validate.notNull(x509Cert);
        Validate.notNull(privateKey);
    }

    @Override
    public String getCertification() throws ServiceException {
        String base64Cert = null;
        try {
            base64Cert = Base64.encodeBase64String(x509Cert.getEncoded());
        } catch (Exception e) {
            throw new ServiceException("PKI服务出错,获取证书失败！", e);
        }
        return base64Cert;

    }

    @Override
    public String decryptRSA(String base64EnvelopedData, String encryptedData, String certSerialNo,
                             String encryptType) throws ServiceException {

        byte[] result = UesUtils.decryptCMS(base64EnvelopedData, x509Cert, privateKey);
        String plainData = null;
        if (result != null) {
            try {
                String key = new String(result, "utf-16le");
                plainData = UesUtils.decryptDESede(encryptedData, key);
            } catch (Exception e) {
                throw new ServiceException("PKI服务出错，解密失败", e);
            }
        }

        return plainData;
    }

    @Override
    public String decryptRSA(String encryptedData) throws ServiceException {
        String plainData = null;
        try {
            byte[] en_data = org.apache.commons.codec.binary.Hex.decodeHex(encryptedData
                .toCharArray());
            Cipher cipher = Cipher.getInstance("RSA", new BouncyCastleProvider());
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] deBytes = cipher.doFinal(en_data);
            plainData = StringUtils.reverse(new String(deBytes));
        } catch (Exception e) {
            throw new ServiceException("PKI服务出错，解密失败",e);
        }
        return plainData;
    }

    @Override
    public String decryptAES(String key, String value) throws ServiceException {
        String plainData = null;
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] encryptedBytes = Hex.decode(value);
            byte[] original = cipher.doFinal(encryptedBytes);
            plainData = new String(original).trim();
        } catch (Exception e) {
            throw new ServiceException("AES解密失败",e);
        }
        return plainData;
    }

    /**
     * RSA密钥对信息
     *
     * @author WangHai
     *
     */
    static public class RSAKeyHolder {
        /**
         * Pfx 文件
         */
        private String pfxFile;

        /**
         * 密码
         */
        private String password;

        public String getPfxFile() {
            return pfxFile;
        }

        public void setPfxFile(String pfxFile) {
            this.pfxFile = pfxFile;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

    }

}
