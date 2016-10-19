package com.netfinworks.site.domain.enums;

public enum PlatformTypeEnum {

    UID(1,"用户ID"),
    MOBILE(2,"手机号"),
    LOGIN_NAME(3,"普通文字账户"),
    COMPANY_ID(4,"普通文字账户");

    private Integer   code;
    private String msg;
    
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    private PlatformTypeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static PlatformTypeEnum getByCode(Integer code) {
        if (code==null) {
            return null;
        }
        for (PlatformTypeEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
    
}
