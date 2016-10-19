package com.netfinworks.site.domain.enums;


/**
 * <p>解密返回数据标志枚举</p>
 * @author chenfei
 * @version $Id: DeciphedQueryFlag.java, v 0.1 2013-1-29 下午3:32:32 cf Exp $
 */
public enum DeciphedQueryFlag {
    ALL(0, "明文与ticket"),
    PRIMITIVE(1, "明文"),
    TICKET(2, "加密后ticket"),
    ;

    /** 代码 */
    private final Integer   code;
    /** 信息 */
    private final String message;

    DeciphedQueryFlag(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 通过代码获取枚举项
     * @param code
     * @return
     */
    public static DeciphedQueryFlag getByCode(Integer code) {
        if (code == null) {
            return null;
        }

        for (DeciphedQueryFlag ce : DeciphedQueryFlag.values()) {
            if (ce.getCode().equals(code)) {
                return ce;
            }
        }

        return null;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
