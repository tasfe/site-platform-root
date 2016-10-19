package com.netfinworks.site.domain.enums;

import org.apache.commons.lang.StringUtils;

/**
 *
 * <p>认证类型</p>
 * @author qinde
 * @version $Id: AuthType.java, v 0.1 2013-12-2 下午7:31:24 qinde Exp $
 */
public enum AuthType {
	REAL_NAME("realName", "实名认证"), RESET_PAYPWD("resetPaypwd", "重置支付密码"), BANK_CARD("bankCard", "银行卡认证"), IDENTITY(
			"identity", "身份认证");

    private String code;
    private String message;

    private AuthType(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    public static AuthType getByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (AuthType item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
}
