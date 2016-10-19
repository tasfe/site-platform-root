/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.web.validator;

import javax.servlet.http.HttpServletRequest;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.netfinworks.site.web.form.NameCertificationOneForm;

/**
 * 通用说明：
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-14 下午4:13:19
 *
 */
public class CertificationValidator implements Validator {
    private HttpServletRequest request;

    public CertificationValidator(Object... objs) {
        if (objs != null) {
            for (Object obj : objs) {
                if (obj instanceof HttpServletRequest) {
                    this.request = (HttpServletRequest) obj;
                }
            }
        }
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return NameCertificationOneForm.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        NameCertificationOneForm form = (NameCertificationOneForm) target;
        validateForm(form, errors);
    }

    private void validateForm(NameCertificationOneForm form, Errors errors) {
        ValidationUtils.rejectIfEmpty(errors, "realName", "realName_not_exsit","真实名称不能为空");
        ValidationUtils.rejectIfEmpty(errors, "certification", "certification_not_exsit","身份证号码不能为空");
    }

}
