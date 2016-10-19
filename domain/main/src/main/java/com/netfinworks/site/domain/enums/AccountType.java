package com.netfinworks.site.domain.enums;


/**
 * <p>账户类型枚举</p>
 * @author chenfei
 * @version $Id: AccountType.java, v 0.1 2013-1-29 下午3:18:24 cf Exp $
 */
public enum AccountType {
	PERSONAL_BASE(101L, "对私基本户"), ENTERPRISE_BASE(201L, "对公基本户"), MERCHANT_BASE(301L, "对公基本户"), ;

    /** 代码 */
    private final Long   code;
    /** 信息 */
    private final String message;

    AccountType(Long code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 通过代码获取枚举项
     * @param code
     * @return
     */
    public static AccountType getByCode(Long code) {
        if (code == null) {
            return null;
        }

        for (AccountType le : AccountType.values()) {
            if (le.getCode().equals(code)) {
                return le;
            }
        }

        return null;
    }

    public Long getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
