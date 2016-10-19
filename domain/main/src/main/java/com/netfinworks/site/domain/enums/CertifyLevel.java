package com.netfinworks.site.domain.enums;

import org.apache.commons.lang.StringUtils;

/**
 * 实名认证等级
 * 
 * @author liuchen
 * 
 */
public enum CertifyLevel {
	NOT_CERTIFY("0", "未认证"), CERTIFY_V0("1", "实名校验"), CERTIFY_V1("2", "实名认证V1"), CERTIFY_V2("3", "实名认证V2");

    private String code;
    private String message;

    private CertifyLevel(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    public static CertifyLevel getByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (CertifyLevel item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
}
