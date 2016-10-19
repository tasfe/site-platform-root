package com.netfinworks.site.domain.enums;

/**
 *
 * <p>短信网关发送类型</p>
 * @author qinde
 * @version $Id: AuthTypeKind.java, v 0.1 2013-12-4 下午4:08:47 qinde Exp $
 */
public enum AuthTypeKind {
    /**单次，重复*/
    SINGLE_USE, REPEATEDLY;

    private String code;
    private String msg;

    public static AuthTypeKind getByCode(String code) {
        for (AuthTypeKind value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean equals(String code) {
        return getCode().equals(code);
    }

}
