/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.web.form;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.netfinworks.site.core.common.constants.ValidationConstants;


/**
 * 通用说明：实名认证form
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-14 下午3:00:04
 *
 */
public class NameCertificationOneForm implements Serializable {
    private static final long serialVersionUID = 5325161679649149687L;

    private String            memberId;                               //账户
    private String            mobile;                                 //电话
    private String            loginName;                              //登录名称

    @NotNull(message = "realname_not_exist")
    @Pattern(regexp = ValidationConstants.NAME_PATTERN, message = "realname_pattern_not_right")
    @Size(min = 2, max = 12, message = "realname_pattern_not_right")
    private String            realName;                               //真实名称

    @NotNull(message = "certification_not_exsit")
    @Pattern(regexp = ValidationConstants.ID_CARD_PATTERN, message = "cert_pattern_not_right")
    private String            certification;                          //身份证号码

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getCertification() {
        return certification;
    }

    public void setCertification(String certification) {
        this.certification = certification;
    }

}
