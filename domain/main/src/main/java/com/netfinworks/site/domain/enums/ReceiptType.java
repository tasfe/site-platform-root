package com.netfinworks.site.domain.enums;

/**
 * <p>电子回单类型</p>
 * @author zhangyun.m
 * @version $Id: ReceiptType.java, v 0.1 2014年7月4日 下午3:02:18 zhangyun.m Exp $
 */
public enum ReceiptType {

    WITHDRAW(10210, "提现"), 
    PAYTOBANK(10220, "付款到卡");

    /** 代码 */
    private final Integer code;
    /** 信息 */
    private final String  message;

    /**构造器
     * @param code
     * @param message
     */
    ReceiptType(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 通过代码获取枚举项
     * @param code
     * @return
     */
    public static ReceiptType getByCode(Integer code) {
        if (code == null) {
            return null;
        }

        for (ReceiptType rt : ReceiptType.values()) {
            if (rt.getCode().equals(code)) {
                return rt;
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

    public boolean equalsByCode(Integer code) {
        return this.code.equals(code);
    }

}
