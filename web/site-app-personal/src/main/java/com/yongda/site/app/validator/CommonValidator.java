/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年8月25日
 */
package com.yongda.site.app.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.ValidatorFactory;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 通用Bean验证
 * @author xuwei
 * @date 2014年8月25日
 */
@Component("commonValidator")
public class CommonValidator {
	public static final Logger logger = LoggerFactory.getLogger(CommonValidator.class);
	
	@Resource(name = "validator")
	private ValidatorFactory factory;
	
	/**
     * 验证一个对象的字段值
     * @param <T> 模板
     * @param o 模板对象（包含验证注解）
     */
    public <T> String validate(T o) {
        Set<ConstraintViolation<T>> violations = factory.getValidator().validate(o);
        if (violations != null && violations.size() > 0) {
            ConstraintViolation<T> violation = violations.iterator().next();
            return violation.getMessage();
        }
        
        return null;
    }

    /**
     * 校验对象所有字段
     * @param <T> 模板
     * @param o 模板对象（包含验证注解）
     * @return 错误信息列表
     */
    public <T> List<String> validateAll(T o) {
        List<String> errorList = new ArrayList<String>();
        Set<ConstraintViolation<T>> violations = factory.getValidator().validate(o);
        if (!CollectionUtils.isEmpty(violations)) {
            for (ConstraintViolation<T> violation : violations) {
                String message = violation.getMessage();
                errorList.add(message);
            }
        }

        return errorList;
    }
}
