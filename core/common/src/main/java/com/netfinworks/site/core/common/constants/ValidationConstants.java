package com.netfinworks.site.core.common.constants;


public class ValidationConstants {

	public final static String PAY_PWD_PATTERN = "^([a-zA-z0-9~!@#$%^&*\\;',./_+|{}\\[\\]:\"<>?]{8,20})?$";
	public final static String NAME_PATTERN = "[\u4E00-\u9FA5]{2,6}";
	public final static String BANK_CARD_NO_PATTERN = "[0-9a-zA-Z\\-]{8,32}";
	public final static String ID_CARD_PATTERN = "\\d{15}$|^\\d{17}[0-9a-zA-Z]";
	public final static String BANK_CARD_ID_PATTERN = "^[\\d]*$";
	public final static String MOBILE_PATTERN = "^1\\d{10}$";
	public final static String EMAIL_MOBILE_APTTERN = "^(\\w)+(\\.\\w+)*@(\\w)+((\\.\\w+)+)$|^1\\d{10}$";
	public final static String CAPTCHA_PATTERN = "^[0-9a-zA-Z]{4}$";
	public final static String VCODE_PATTERN = "[0-9a-zA-Z]{6}";
	public final static String MONEY_PATTERN = "(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){1,2})?";
	public final static String CHANNEL_CODE = "^[a-zA-Z2]+$";
	public final static String DBCR_TYPE_PATTERN = "^(?i)(CC|DC|GC)$";

	public final static String EXPRESS_BANK_CARD_NO_PATTERN = "^[0-9\\-]{8,32}$";
	public final static String EXPRESS_MONTH_PATTERN = "^[1-9]$|^1[0-2]$";
	public final static String EXPRESS_YEAR_PATTERN = "^201[3-9]$|^202[0-9]$|^2030$";
	public final static String EXPRESS_SECURITY_CODE_PATTERN = "^[0-9]{3,4}$";
	public final static String EXPRESS_BANK_NAME_PATTERN = "^[A-Z]+$";

}
