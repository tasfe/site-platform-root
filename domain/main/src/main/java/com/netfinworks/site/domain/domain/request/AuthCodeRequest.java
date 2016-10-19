/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.domain.domain.request;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.netfinworks.common.domain.OperationEnvironment;

/**
 * 通用说明：手机认证码请求
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-26 下午12:45:03
 *
 */
public class AuthCodeRequest extends OperationEnvironment implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -4009583131998792507L;

    private String            memberId;
    /**业务类型    */
    private String            bizType;
    /**业务代码    */
    private String            bizId;
    /**手机加密    */
    private String            mobileTicket;
    /*    */private Integer  verifiableCount;
    /**有效时间    */
    private Long              validity;
    /*    */private Integer  authCodeLength;
    /*    */private String   authCode;
    /*    */private String   mobile;

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public String getMobileTicket() {
        return mobileTicket;
    }

    public void setMobileTicket(String mobileTicket) {
        this.mobileTicket = mobileTicket;
    }

    public Integer getVerifiableCount() {
        return verifiableCount;
    }

    public void setVerifiableCount(Integer verifiableCount) {
        this.verifiableCount = verifiableCount;
    }

    public Long getValidity() {
        return validity;
    }

    public void setValidity(Long validity) {
        this.validity = validity;
    }

    public Integer getAuthCodeLength() {
        return authCodeLength;
    }

    public void setAuthCodeLength(Integer authCodeLength) {
        this.authCodeLength = authCodeLength;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
