package com.netfinworks.site.domain.enums;

/**
 * <p>支付渠道</p>
 * @author eric
 * @version $Id: ResourceType.java, v 0.1 2013-7-16 下午2:15:47  Exp $
 */
public enum Dbcr {
    DC("DC", "储蓄卡"),
    CC("CC", "信用卡"),
    GC("GC", "综合卡"),
    ;

    private final String code;

    private final String message;

    /**
     * @param code
     * @param message
     */
    Dbcr(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据代码获取ENUM
     *
     * @param code
     * @return
     */
    public static Dbcr getByCode(String code) {
        for (Dbcr payChannel : Dbcr.values()) {
            if (code.equals(payChannel.getCode())) {
                return payChannel;
            }
        }

        return null;
    }
    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
