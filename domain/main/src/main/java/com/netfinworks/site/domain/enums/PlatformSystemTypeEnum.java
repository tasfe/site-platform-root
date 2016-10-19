package com.netfinworks.site.domain.enums;

/**
 * 统一登陆区别平台系统类型
 * @author zhaozq
 * @date 2015年6月24日
 */
public enum PlatformSystemTypeEnum {
	KJT_PERSONAL("1","个人版",   PlatformTypUnionma.KJT),
	KJT_ENTERPRISE("2","企业版", PlatformTypUnionma.KJT);
	
	private String code;
	private String message;
	private PlatformTypUnionma platformTypUnionma;
	
	PlatformSystemTypeEnum(String code, String message, PlatformTypUnionma platformTypUnionma) {
		this.code = code;
		this.message = message;
		this.platformTypUnionma = platformTypUnionma;
	}
	PlatformSystemTypeEnum(String code, String message) {
		this.code = code;
		this.message = message;	
	}
	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}
	
	public PlatformTypUnionma getPlatformTypUnionma() {
		return this.platformTypUnionma;
	}
	
	public static PlatformSystemTypeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (PlatformSystemTypeEnum e : PlatformSystemTypeEnum.values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    } 
}
