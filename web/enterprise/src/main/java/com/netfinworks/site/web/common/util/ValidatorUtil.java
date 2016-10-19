package com.netfinworks.site.web.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.apache.commons.collections.CollectionUtils;

/**
 * 
 * <p>校验工具类</p>
 * @author Fengxueyong
 * @version $Id: ValidatorUtil.java, v 0.1 2014年8月11日 下午7:22:13 fengxueyong Exp $
 */
public class ValidatorUtil {
    private static ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    /**
     * 验证一个对象的字段值
     * @param <T>
     * @param o
     * @throws IllegalArgumentException
     */
    public static <T> void validate(T o) throws IllegalArgumentException {
        Set<ConstraintViolation<T>> violations = factory.getValidator().validate(o);
        if (violations != null && violations.size() > 0) {
            ConstraintViolation<T> violation = violations.iterator().next();
            throw new IllegalArgumentException(violation.getMessage());
        }
    }

    /**
     * 校验对象所有字段
     * @param <T>
     * @param o
     * @return 错误信息列表
     */
    public static <T> List<String> validateAll(T o) {
        List<String> errorList = new ArrayList<String>();
        Set<ConstraintViolation<T>> violations = factory.getValidator().validate(o);
        if (!CollectionUtils.isEmpty(violations)) {
            for (ConstraintViolation<T> violation : violations) {
                errorList.add(violation.getMessage());
            }
        }

        return errorList;
    }
}
