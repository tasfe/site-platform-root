package com.netfinworks.site.domain.enums;

import org.apache.commons.lang.StringUtils;

/**
 * 证书状态
 * @author chengwen
 *
 */
public enum CertificationStatus {
	INIT("INIT","初始化"),
	
	ACTIVATED("ACTIVATED","已激活"),
	
	UNBIND("UNBIND","解除绑定"),
	
	INVALID("INVALID","无效的"),
	
	REVOKED("REVOKED","已注销");
		
	private String code;
		
	private String message;
	    
	private CertificationStatus(String code, String message) {
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
	
	public static CertificationStatus getByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (CertificationStatus item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
}
