package com.netfinworks.site.domain.enums;


/**
 * <p>交易类型</p>
 * @author zhangyun.m
 * @version $Id: DealType.java, v 0.1 2013-12-6 上午10:27:56 HP Exp $
 */
public enum DealType {

    ALLDEALTYPE(1, "所有"),
    PAYOUT(2, "支出"), 
    INCOME(3, "收入");

    private final Integer    code;

    private final String message;

    private DealType(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static DealType getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DealType item : values()) {
            if (item.getCode() == code) {
                return item;
            }
        }
        return null;
    }

}
