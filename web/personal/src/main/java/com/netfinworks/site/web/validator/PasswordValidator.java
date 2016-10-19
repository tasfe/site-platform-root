/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.web.validator;

import javax.servlet.http.HttpServletRequest;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.netfinworks.site.web.form.ResetPayPasswordForm;

/**
 * <p>密码验证</p>
 * @author zhangyun.m
 * @version $Id: PasswordValidator.java, v 0.1 2014年5月22日 下午5:37:57 zhangyun.m Exp $
 */
public class PasswordValidator implements Validator {
    private HttpServletRequest request;

    public PasswordValidator(Object... objs) {
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
        return ResetPayPasswordForm.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ResetPayPasswordForm form = (ResetPayPasswordForm) target;
        validatePaypwd(form, errors);
    }

    private void validatePaypwd(ResetPayPasswordForm form, Errors errors) {
        ValidationUtils.rejectIfEmpty(errors, "newPasswd", "password_not_exsit","请输入支付密码");
        ValidationUtils.rejectIfEmpty(errors, "renewPasswd", "repassword_not_exsit","请输入支付密码确认");
    }

}
