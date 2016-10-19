/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.web.validator;

import javax.servlet.http.HttpServletRequest;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.netfinworks.site.web.form.PayPasswordForm;

/**
 * 通用说明：
 *
 * @author <a href="mailto:qinde@netfinworks.com">qinde</a>
 *
 * @version 1.0.0  2013-11-14 下午4:13:19
 *
 */
public class SetPayPasswordValidator implements Validator {
    private HttpServletRequest request;

    public SetPayPasswordValidator(Object... objs) {
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
        return PayPasswordForm.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PayPasswordForm form = (PayPasswordForm) target;
        validatePaypwd(form, errors);
    }

    private void validatePaypwd(PayPasswordForm form, Errors errors) {
        //ValidationUtils.rejectIfEmpty(errors, "mobile", "mobile_not_exsit","手机号码不能为空");
        ValidationUtils.rejectIfEmpty(errors, "password", "password_not_exsit","请输入支付密码");
        ValidationUtils.rejectIfEmpty(errors, "repassword", "repassword_not_exsit","请输入支付密码确认");
        ValidationUtils.rejectIfEmpty(errors, "paypwdCaptcha","paypwdCaptcha_not_exsit", "请输入验证码");
        //ValidationUtils.rejectIfEmpty(errors, "username","username_not_exsit", "请输入用户名");
       // ValidationUtils.rejectIfEmpty(errors, "email","email_not_exsit", "请输入email");
        //ValidationUtils.rejectIfEmpty(errors, "agree","agree_not_exsit", "请选择是否接受协议");

    }

}
