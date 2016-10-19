package com.netfinworks.site.domain.enums;

import org.apache.commons.lang.StringUtils;

/**
 *
 * <p>对公对私类型</p>
 * @author qinde
 * @version $Id: AuthType.java, v 0.1 2013-12-2 下午7:31:24 qinde Exp $
 */
public enum CropOrPersonal {
    COMPANY("B", "对公"), PERSONAL("C", "对私");

    private String code;
    private String message;

    private CropOrPersonal(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    public static CropOrPersonal getByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (CropOrPersonal item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
}
