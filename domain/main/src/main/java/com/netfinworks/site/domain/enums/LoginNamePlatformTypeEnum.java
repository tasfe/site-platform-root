package com.netfinworks.site.domain.enums;

public enum LoginNamePlatformTypeEnum {

    KJT(1,"永达互联网金融");

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

    private LoginNamePlatformTypeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static LoginNamePlatformTypeEnum getByCode(Integer code) {
        if (code==null) {
            return null;
        }
        for (LoginNamePlatformTypeEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
    
}
