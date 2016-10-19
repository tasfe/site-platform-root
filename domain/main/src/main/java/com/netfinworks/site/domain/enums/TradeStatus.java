/**
 *
 */
package com.netfinworks.site.domain.enums;

import org.apache.commons.lang.StringUtils;

/**
 * <p>交易状态</p>
 * @author fjl
 * @version $Id: ErrorCode.java, v 0.1 2013-11-12 下午4:11:40 fjl Exp $
 */
public enum TradeStatus {
    TRADE_PAYING("1", "待支付"),

    TRADE_FINISHED("2", "已完成"),

    TRADE_CANCEL("3", "已取消");

    private String code;
    private String message;

    private TradeStatus(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static TradeStatus getByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (TradeStatus item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

}
