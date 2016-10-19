package com.netfinworks.site.domain.enums;

public enum LoginNameTypeEnum {
    EMAIL(1,"邮箱"),
    MOBILE(2,"手机号"),
    NORMAL(3,"普通文字账户");

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

    private LoginNameTypeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static LoginNameTypeEnum getByCode(Integer code) {
        if (code==null) {
            return null;
        }
        for (LoginNameTypeEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
}
