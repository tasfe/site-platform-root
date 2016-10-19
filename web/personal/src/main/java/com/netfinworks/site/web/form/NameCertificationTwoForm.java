/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.web.form;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * 通用说明：实名认证form
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-14 下午3:00:04
 *
 */
public class NameCertificationTwoForm implements Serializable {
    private static final long serialVersionUID = 5325161679649149687L;

    private String            memberId;                               //账户
    private String            mobile;                                 //电话
    private String            loginName;                              //登录名称
    private String            realName;                               //真实名称
    private String            certification;                          //身份证号码
    private String            certificationFront;                     //身份证号码正面
    private String            certificationBack;                      //身份证号码反面
    private String            startDate;                              //身份证号码到期时间
    private String            type;                                   //身份证到期类型 1:有期限;2长期

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

    public String getCertificationFront() {
        return certificationFront;
    }

    public void setCertificationFront(String certificationFront) {
        this.certificationFront = certificationFront;
    }

    public String getCertificationBack() {
        return certificationBack;
    }

    public void setCertificationBack(String certificationBack) {
        this.certificationBack = certificationBack;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static void main(String[] args) {
        NameCertificationTwoForm form = new NameCertificationTwoForm();
        form.setRealName("1");
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        Validator validator = vf.getValidator();
        Set<ConstraintViolation<NameCertificationTwoForm>> set = validator.validate(form);
        for (ConstraintViolation<NameCertificationTwoForm> constraintViolation : set) {
            System.out.println(constraintViolation.getMessage());
        }
    }

}
