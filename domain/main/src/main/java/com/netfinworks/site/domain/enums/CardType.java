/**
 *
 */
package com.netfinworks.site.domain.enums;

import org.apache.commons.lang.StringUtils;

/**
 *
 * <p>卡类型</p>
 * @author qinde
 * @version $Id: BankType.java, v 0.1 2013-11-28 下午12:28:51 qinde Exp $
 */
public enum CardType {
    JJK("1", "借记卡"),

    XYK("2", "信用卡"),

    CZ("3", "存折"),

    OTHER("4", "其它") ;

    private String code;
    private String message;

    private CardType(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static CardType getByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (CardType item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

}
