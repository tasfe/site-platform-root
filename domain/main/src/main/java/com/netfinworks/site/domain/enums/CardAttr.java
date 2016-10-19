/**
 * 
 */
package com.netfinworks.site.domain.enums;

/**
 * <p>银行账号属性</p>
 * @author fangjilue
 * @version $Id: BankAccountAttrEnum.java 11471 2012-08-20 02:58:43Z fangjilue $
 */
public enum CardAttr {
    PERSONAL(1, "对私"), COMPANY(0, "对公");
    
    /** 代码 */
    private final Integer   code;
    /** 信息 */
    private final String message;

    CardAttr(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 通过代码获取枚举项
     * @param code
     * @return
     */
    public static CardAttr getByCode(Integer code) {
        if (code == null) {
            return null;
        }

        for (CardAttr at : CardAttr.values()) {
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
}
