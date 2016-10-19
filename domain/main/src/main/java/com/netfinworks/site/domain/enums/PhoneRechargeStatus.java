package com.netfinworks.site.domain.enums;

import org.apache.commons.lang.StringUtils;

/**
 * <p>话费充值状态</p>
 * @author chenhui
 */
public enum PhoneRechargeStatus {
    RECHARGE_PAYING("1","待支付"),
    RECHARGE_SUCCESS("2","交易成功"),
    RECHARGE_FAILD("3","交易失败"),
    RECHARGE_CLOSE("4","交易关闭");
    private String code;
    private String message;
    private PhoneRechargeStatus(String code,String message){
        this.code = code;
        this.message = message;
    }
    public String getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }
    public static PhoneRechargeStatus getByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (PhoneRechargeStatus item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

}

