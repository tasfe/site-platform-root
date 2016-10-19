package com.netfinworks.site.domain.enums;

import org.apache.commons.lang.StringUtils;

public enum CertificationType {
	
	SOFT_CERTIFICATION("soft", "软证书"),
	
	HARD_CERTIFICATION("hard", "硬证书"),
	
	GATEWAY_CERTIFICATION("gateway", "网关证书");
	
	private String code;
	
    private String message;
    
    private CertificationType(String code, String message) {
        this.code = code;
        this.message = message;
    }

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
    
	public static CertificationType getByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (CertificationType item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
}
