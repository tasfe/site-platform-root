package com.netfinworks.site.domain.enums;

public enum NewPlatformTypeEnum {

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

    private NewPlatformTypeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static NewPlatformTypeEnum getByCode(Integer code) {
        if (code==null) {
            return null;
        }
        for (NewPlatformTypeEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
    
}
