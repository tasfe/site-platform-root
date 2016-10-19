package com.netfinworks.site.domain.enums;

public enum SendType {
	AUTHCODE("AUTHCODE","以验证码形式"),
	AUTHURL("AUTHURL","以链接形式");
	
    private final String code; // 代码
    private final String  message; // 信息
    
    public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	SendType(String code, String message) {
        this.code = code;
        this.message = message;
    }
	
	public static SendType getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (SendType e : SendType.values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    } 
}
