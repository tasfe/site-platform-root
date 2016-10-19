package com.netfinworks.site.domain.enums;


/** 
* @ClassName: BankCardPayAttrType 
* @Description: 卡绑定属性
* @author slong
* @date 2016年5月19日 上午9:44:36 
*  
*/
public enum BankCardPayAttrType {
	QUICKPAY("qpay", "快捷"), NORMAL("normal", "一般");

    /** 代码 */
    private final String   code;
    /** 信息 */
    private final String message;

    BankCardPayAttrType(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 通过代码获取枚举项
     * @param code
     * @return
     */
    public static BankCardPayAttrType getByCode(String code) {
        if (code == null) {
            return null;
        }

        for (BankCardPayAttrType le : BankCardPayAttrType.values()) {
            if (le.getCode().equals(code)) {
                return le;
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
