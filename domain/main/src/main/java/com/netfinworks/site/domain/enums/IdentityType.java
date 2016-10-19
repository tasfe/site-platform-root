/**
 *
 */
package com.netfinworks.site.domain.enums;

import org.apache.commons.lang.StringUtils;

/**
 * <p>会员标识类型</p>
 * @author fjl
 * @version $Id: IdentityType.java, v 0.1 2013-11-13 上午10:56:54 fjl Exp $
 */
public enum IdentityType {
    UID("UID", "1", 0),
    MEMBER_ID("MEMBER_ID", null, null),
    MOBILE("MOBILE", "1", 2),
    LOGIN_NAME("LOGIN_NAME","3",3),
    COMPANY_ID("COMPANY_ID","4",0),
    EMAIL("EMAIL","1",1);

    private String  code;
    private String  platformType; //内部平台类型
    private Integer insCode;     //内部类型

    private IdentityType(String code, String platformType, Integer insCode) {
        this.code = code;
        this.platformType = platformType;
        this.insCode = insCode;
    }

    public String getCode() {
        return code;
    }

    public String getPlatformType() {
        return platformType;
    }

    public static IdentityType getByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (IdentityType item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }


    public static IdentityType getByPlatformType(String platformType) {
        if (StringUtils.isBlank(platformType)) {
            return null;
        }
        for (IdentityType item : values()) {
            if (item.getPlatformType().equals(platformType)) {
                return item;
            }
        }
        return null;
    }

    public Integer getInsCode() {
        return insCode;
    }

}
