/**
 * 
 */
package com.netfinworks.site.domain.enums;

/**
 * <p>支付密码是否设置</p>
 * @author fangjilue
 * @version $Id: PayPwdSet.java, v 0.1 2014-1-6 下午5:08:34 fangjilue Exp $
 */
public enum PayPwdSet {

    SET(1, "设置"), NOT_SET(0, "未设置");
    
    /** 代码 */
    private final Integer   code;
    /** 信息 */
    private final String message;

    PayPwdSet(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 通过代码获取枚举项
     * @param code
     * @return
     */
    public static PayPwdSet getByCode(Integer code) {
        if (code == null) {
            return null;
        }

        for (PayPwdSet at : PayPwdSet.values()) {
            if (at.getCode().equals(code)) {
                return at;
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
    
    public boolean equalsByCode(Integer code){
        return this.code.equals(code);
    }

}
