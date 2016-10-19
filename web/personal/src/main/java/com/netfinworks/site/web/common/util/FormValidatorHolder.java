package com.netfinworks.site.web.common.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.validation.Validator;
import org.springmodules.validation.commons.ConfigurableBeanValidator;
import org.springmodules.validation.commons.ValidatorFactory;

/**
 * <p>表单校验器持器</p>
 * @author sean won
 * @version $Id: FormValidatorHolder.java, v 0.1 2009-11-17 上午11:17:39  Exp $
 */
public class FormValidatorHolder {
    /** 表单内容校验器工厂 */
    private static ValidatorFactory       validatorFactory;
    /** 校验器缓存 */
    private static Map<String, Validator> validatorCache = new HashMap<String, Validator>();

    /**
     * 构造注入
     * @param validatorFactory
     */
    public FormValidatorHolder(ValidatorFactory validatorFactory) {
        FormValidatorHolder.validatorFactory = validatorFactory;
    }

    /**
     * 获取校验器
     * @param formName
     * @return
     */
    public static Validator getValidator(String formName) {
        Validator validator = validatorCache.get(formName);
        if (validator == null) {
            ConfigurableBeanValidator newValidator = new ConfigurableBeanValidator();
            newValidator.setFormName(formName);
            newValidator.setValidatorFactory(validatorFactory);

            validatorCache.put(formName, newValidator);
            validator = newValidator;
        }

        return validator;
    }
}
