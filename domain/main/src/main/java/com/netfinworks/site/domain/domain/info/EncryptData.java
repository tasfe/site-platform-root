/**
 *
 */
package com.netfinworks.site.domain.domain.info;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * <p>加密数据</p>
 * @author fjl
 * @version $Id: EncryptData.java, v 0.1 2013-12-2 下午1:56:10 fjl Exp $
 */
public class EncryptData implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -2199211534166805850L;

    /**
     * 密文即ticket
     */
    private String            ciphertext;

    /**
     * 明文
     */
    private String            plaintext;

    /**
     * 掩码
     */
    private String            mask;
    /**摘要*/
    private String            summary;

    public String getCiphertext() {
        return ciphertext;
    }

    public void setCiphertext(String ciphertext) {
        this.ciphertext = ciphertext;
    }

    public String getPlaintext() {
        return plaintext;
    }

    public void setPlaintext(String plaintext) {
        this.plaintext = plaintext;
    }

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
