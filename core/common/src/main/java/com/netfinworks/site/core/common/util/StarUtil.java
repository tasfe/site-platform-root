package com.netfinworks.site.core.common.util;

import org.apache.commons.lang.StringUtils;

public class StarUtil {
    /**
     * 获取马赛克数据
     * @param data 原文
     * @return 马赛克数据
     */
    public static String getMaskData(String data) {
        if (StringUtils.isEmpty(data) || data.length() <= 4) {
            return data;
        }
        return getMaskData(data, 0, data.length() - 4);
    }
    
    /**
     * 获取马赛克数据
     * @param data 原文
     * @param start *字符开始位
     * @param end *字符结束位
     * @return 马赛克数据
     */
    public static String getMaskData(String data, int start, int end) {
        if (start >= end) {
            return StringUtils.EMPTY;
        }
        
        if (StringUtils.isEmpty(data) || data.length() <= (end - start)) {
            return data;
        }
        
        int len = data.length();
        return new StringBuilder(data.replaceAll(".", "*"))
            .replace(0, start, data.substring(0, start))
            .replace(end, len, data.substring(end, len)).toString();
    }
    

	/**
	 * 通用星号处理
	 * 
	 * @param mobile
	 * @return
	 */
	public static String mockCommon(String str) {
		if (StringUtils.isBlank(str)) {
			return "";
		}
		if (str.length() <= 4) {
			return str;
		}
		return "******" + str.substring(str.length() - 4);
	}
	
	/**通用信号处理（升级版）
	 * @param str 要处理的字符串
	 * @param pre 需要显示该字符串的前几位
	 * @param after 需要显示该字符串的后几位
	 * @param count 隐藏部分用几个星号代替
	 * @return
	 */
	public static String hideStrBySym(String str,int pre,int after,int count) {
		StringBuffer symbol = new StringBuffer();
		if(count>0){
			for(int i=0;i<count;i++){
				symbol.append("*");
			}
		}
		if(null==str||"".equals(str)){
			return "";
		}
		String str1 = str.substring(pre,str.length()-after);
		str1 = str.replace(str1, symbol.toString());
		return str1;
	}
	

    /**
     * 手机星号处理
     * @param mobile
     * @return
     */
    public static String mockMobile(String mobile) {
        if (StringUtils.isBlank(mobile)) {
            return null;
        }
        if (mobile.length() < 10) {
            return mobile;
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(7);
    }

	/**
	 * 邮箱星号处理
	 * 
	 * @param mobile
	 * @return
	 */
	public static String mockEmail(String email) {
		if (StringUtils.isBlank(email)) {
			return null;
		}
		if (email.indexOf("@") != -1) {
			return email.substring(0, 1) + "******" + email.substring(email.indexOf("@") - 1);
		}
		return email;
	}
	
    /**
     * 银行卡星号处理
     * @param mobile
     * @return
     */
    public static String mockBankCard(String bankCard) {
        if (StringUtils.isBlank(bankCard)) {
            return null;
        }
        if (bankCard.length() < 16) {
            return bankCard;
        }
        if (bankCard.length() <= 16) {
            return bankCard.substring(0, 4) + "*******" + bankCard.substring(12);
        } else {
            return bankCard.substring(0, 4) + "*******" + bankCard.substring(15);
        }
    }
    /**
     * 银行卡星号和空格处理
     * @param mobile
     * @return
     */
    public static String mockBankCardByBlank(String bankCard) {
        if (StringUtils.isBlank(bankCard)) {
            return "";
        }
        if(bankCard.length() <=4) {
            return "**** **** **** " + bankCard;
        }
        return "**** **** **** " + bankCard.substring(bankCard.length()-4);
    }

    /**
     * 实名星号处理
     * @param mobile
     * @return
     */
    public static String mockRealName(String realName) {
        if (StringUtils.isBlank(realName)) {
            return null;
        }
        StringBuffer result = new StringBuffer();
        if(realName.length() > 1) {
            result.append("*");
            result.append(realName.substring(1));
            return result.toString();
        }
        return realName;
    }
    
    /**姓名处理，长度超过4位，后面的字符只显示..
     * @param realName
     * @return
     */
    public static String hideLongRealName(String realName){
    	if(realName!=null){
    		int len = realName.length();
        	if(len>4){
        		realName = realName.substring(0, 4)+"..";
        	}
    	}
    	return realName;
    }

    /**
     * 银行卡截取
     * @param bankCard
     * @return
     */
    public static String maskBankCard(String bankCard) {
        if (StringUtils.isBlank(bankCard)) {
            return null;
        }
        return "**********" + bankCard.substring(bankCard.length() - 4);

    }

    public static String maskIdCard(String idCard) {
        if (StringUtils.isBlank(idCard)) {
            return null;
        }
        int length = idCard.length();
        if (length < 3) {
            return idCard;
        }
        return idCard.substring(0, 1) + "****************" + idCard.substring(idCard.length() - 1);
    }
}
