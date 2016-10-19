package com.netfinworks.site.domain.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public enum CertType {
	ID_CARD("idCard", "大陆居民身份证"), BANK_CARD_NO("bankCardNo", "银行卡号"),
	HK_CARD("hkCard", "港澳台公民身份证"), 
//	PASSPORT("passport", "护照"),TW_PASS("twPass", "台湾居民往来大陆通行证"),
//	GA_PASS("gaPass", "港澳居民往来大陆通行证"),GA_CARD("gaCard", "香港/澳门居民身份证"),
	OTHER_CARD("otherCard", "其他");
	
    private String code;
    private String message;

    private CertType(String code, String message) {
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
    
    public static CertType getByCode(String code){
        if(StringUtils.isBlank(code)){
            return null;
        }
        for(CertType item : values()){
            if(item.getCode().equals(code)){
                return item;
            }
        }
        return null;
    }
    /**
     * 过滤
     * @param filterList
     * @return
     */
    public static List<CertType> toFilterList(List<CertType> filterList) {
    	CertType[] certArray = CertType.values();
 
    	if (filterList == null || filterList.size() == 0)
    		return Arrays.asList(certArray);
    	
    	List<CertType> list = new ArrayList<CertType>();
    	p1: for (CertType ct : certArray) {
	    		for (CertType ft : filterList) {
	    			if (ct.equals(ft)) {
	    				continue p1;
	    			}
	    		}
	    		list.add(ct);
    		}
    	
    	return list;
    }


}
