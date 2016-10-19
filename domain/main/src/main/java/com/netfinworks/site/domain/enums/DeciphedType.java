package com.netfinworks.site.domain.enums;

/**
 * <p>解密类型枚举</p>
 * @author netfinworks
 * @version $Id: DeciphedTypeEnum.java 25877 2012-11-26 11:31:55Z chenfei $
 */
public enum DeciphedType {
	 	ID_CARD(1, "身份证", "idCard"),
	    CELL_PHONE(2, "手机号", "cellPhone"),
	    EMAIL(3, "邮箱", "email"),
	 	BANK_CARD(4, "银行卡号", "bankAccountNo"),
	 	TRUE_NAME(5,"真实姓名","trueName"),
	 	LEGAL_NAME(6,"法人姓名","legalName"),
	    LEGAL_MOBILE(7,"法人手机","legalMobile"),
	 	CONTACT_NAME(8,"联系人姓名","contactName"),
	    CONTACT_EMAIL(9,"联系人邮箱","contactEmail"),
	    CONTACT_MOBILE(10,"联系人手机","contactMobile"),
	    PASSPORT(11, "护照", "passPort"),
	    HKMT_PASS(12,"港澳台通行证","hkmtPass")
	 	;

	    /** 代码 */
	    private final Integer   code;
	    /** 信息 */
	    private final String message;
	    
	    private final String fieldName;

	    DeciphedType(Integer code, String message, String fieldName) {
	        this.code = code;
	        this.message = message;
	        this.fieldName = fieldName;
	    }

	    /**
	     * 通过代码获取枚举项
	     * @param code
	     * @return
	     */
	    public static DeciphedType getByCode(Integer code) {
	        if (code == null) {
	            return null;
	        }

	        for (DeciphedType lnt : DeciphedType.values()) {
	            if (lnt.getCode().equals(code)) {
	                return lnt;
	            }
	        }

	        return null;
	    }
	    
	    public static DeciphedType getByFieldName(String fieldName) {
	        if (fieldName == null) {
	            return null;
	        }

	        for (DeciphedType lnt : DeciphedType.values()) {
	            if (lnt.getFieldName().equals(fieldName)) {
	                return lnt;
	            }
	        }

	        return null;
	    }
	    

	    public Integer getCode() {
	        return code;
	    }

	    public String getMessage() {
	        return message;
	    }

	    public String getFieldName() {
	        return fieldName;
	    }
}
