package com.netfinworks.site.domain.enums;


public enum PlatformTypUnionma {
	KJT("KJT","永达互联网金融"),
	HRY("HRY","海融易");
	
	private final String code; // 代码
    private final String  message; // 信息
    
	
	
    public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	PlatformTypUnionma(String code, String message) {
        this.code = code;
        this.message = message;
    }
	
	public static PlatformTypeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (PlatformTypeEnum e : PlatformTypeEnum.values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    } 
}
