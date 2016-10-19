/**
 * 
 */
package com.netfinworks.site.domain.enums;

/**
 * <p>访问终端</p>
 * @author fjl
 * @version $Id: AccessChannel.java, v 0.1 2013-11-28 下午4:29:42 fjl Exp $
 */
public enum AccessChannel {

    WEB("WEB","电脑"),
    WAP("WAP","手机");
    

    /** 代码 */
    private final String code;
    /** 信息 */
    private final String message;

    AccessChannel(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 通过代码获取枚举项
     * @param code
     * @return
     */
    public static AccessChannel getByCode(String code) {
        if (code == null) {
            return null;
        }

        for (AccessChannel item : AccessChannel.values()) {
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
