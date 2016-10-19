/**
 * 
 */
package com.netfinworks.site.domain.enums;

import com.netfinworks.common.lang.StringUtil;

/**
 * <p>交易服务的交易类型</p>
 * @author fjl
 * @version $Id: AcqTradeType.java, v 0.1 2013-12-7 下午10:04:04 fjl Exp $
 */
public enum AcqTradeType {

    INSTANT_TRASFER("01","普通转账交易"),
    INSTANT_ACQUIRING("11","即时到账收单交易"),
    ENSURE_ACQUIRING("12","担保收单交易"),
    PREPAY_ACQUIRING("13","下订收单交易"),
    REFUND_ACQUIRING("14","收单退款交易"),
    MERGE("15","合并支付"),
    MOBILE_RECHARGE("16", "话费充值"),
    TRUST_COLLECT("17","银行卡代扣交易"),
    FUND_SHARE("18","基金份额交易");

    /**
     * 代码
     */
    private final String code;
    /**
     * 信息
     */
    private final String message;

    AcqTradeType(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据代码获取ENUM
     *
     * @param code
     * @return
     */
    public static AcqTradeType getByCode(String code) {
        if (StringUtil.isBlank(code)) {
            return null;
        }

        for (AcqTradeType type : AcqTradeType.values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
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
