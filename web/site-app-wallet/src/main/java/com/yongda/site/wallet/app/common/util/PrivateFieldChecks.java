package com.yongda.site.wallet.app.common.util;

import org.apache.commons.validator.Field;
import org.apache.commons.validator.GenericValidator;
import org.apache.commons.validator.ValidatorAction;
import org.springframework.validation.Errors;
import org.springmodules.validation.commons.FieldChecks;

import com.netfinworks.common.lang.StringUtil;

/**
 * <p>私有字段校验</p>
 * @author eric
 * @version $Id: PrivateFieldChecks.java, v 0.1 2013-7-22 下午2:14:13 Exp $
 */
public class PrivateFieldChecks extends FieldChecks {
    private static final long serialVersionUID = 9211409574565575925L;

    /**
     * 是否包含中文 GENERAL_PUNCTUATION 判断中文的CJK_SYMBOLS_AND_PUNCTUATION 判断中文的
     * HALFWIDTH_AND_FULLWIDTH_FORMS 判断中文的，
     *
     * @param c
     * @return
     */
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
            || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
            || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
            || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
            || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
            || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {

            return true;
        }
        return false;
    }

    /**
     * 判断字符串中文算两个字
     *
     * @param value
     * @return
     */
    public static int valueLength(String value) {
        if (StringUtil.isBlank(value)) {
            return 0;
        }

        char[] ch = value.toCharArray();
        int count = 0;
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
                count = count + 2;
            } else {
                count++;
            }
        }

        return count;
    }

    /**
     * 判断长度是否超过
     *
     * @param bean
     * @param va
     * @param field
     * @param errors
     * @return
     */
    public static boolean validateMaxLength(Object bean, ValidatorAction va, Field field,
                                            Errors errors) {

        String value = FieldChecks.extractValue(bean, field);

        if (StringUtil.isNotBlank(value)) {
            try {
                int max = Integer.parseInt(field.getVarValue("maxlength"));

                if (valueLength(value) > max) {
                    FieldChecks.rejectValue(errors, field, va);

                    return false;
                }
            } catch (Exception e) {
                FieldChecks.rejectValue(errors, field, va);
                return false;
            }
        }

        return true;
    }

    /**
     * 判断长度是否满足
     *
     * @param bean
     * @param va
     * @param field
     * @param errors
     * @return
     */
    public static boolean validateMinLength(Object bean, ValidatorAction va, Field field,
                                            Errors errors) {

        String value = FieldChecks.extractValue(bean, field);

        if (!GenericValidator.isBlankOrNull(value)) {
            try {
                int min = Integer.parseInt(field.getVarValue("minlength"));

                if (valueLength(value) < min) {
                    FieldChecks.rejectValue(errors, field, va);

                    return false;
                }
            } catch (Exception e) {
                FieldChecks.rejectValue(errors, field, va);
                return false;
            }
        }

        return true;
    }
}
