/**
 *
 */
package com.netfinworks.site.domain.enums;

/**
 * <p>交易查询类型</p>
 * @author dexter.qin
 * @version $Id: TradeType.java, v 0.1 2013-11-28 下午1:30:06 dexter.qin Exp $
 */
public enum TradeTypeRequest {
    INSTANT_TRASFER("01","普通转账交易"),
    INSTANT_ACQUIRING("11","即时到账收单交易"),
    ENSURE_ACQUIRING("12","担保收单交易"),
    PREPAY_ACQUIRING("13","下订收单交易"),
    REFUND_ACQUIRING("14","收单退款交易"),
    MERGE("15","合并支付交易"),
    BANK_WITHHOLDING("17","银行卡代扣");

    /** 代码 */
    private final String code;
    /** 信息 */
    private final String message;

    TradeTypeRequest(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 通过代码获取枚举项
     * @param code
     * @return
     */
    public static TradeTypeRequest getByCode(String code) {
        if (code == null) {
            return null;
        }

        for (TradeTypeRequest item : TradeTypeRequest.values()) {
            if (item.getCode().equals(code)) {
                return item;
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
