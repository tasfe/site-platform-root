/**
 * @company 永达互联网金融信息服务有限公司
 * @project site-web-enterprise
 * @date 2014年10月17日
 */
package com.netfinworks.site.core.common.constants;

/**
 * <p>
 * 正则表达式验证规则
 * </p>
 * 
 * @author 徐威
 * @date 2014年10月17日
 */
public class RegexRule {
    /** 整数或者小数 */
    public static final String NUMBER                    = "^[0-9]+\\.{0,1}[0-9]+$";

    /** 数字 */
    public static final String INTEGER                   = "^[0-9]*$";

    /** 零和非零开头的数字 */
    public static final String INTEGER_VALID             = "^(0|[1-9][0-9]*)$";

    /** 浮点数 */
    public static final String FLOAT_VALID               = "^(-?\\d+)\\.?\\d+$";

    /** 有最多两位小数的正实数 */
    public static final String FLOAT_2_DECINALS          = "^[0-9]+(.[0-9]{0,2})?$";

    /** 有小数点前19位，小数点后2位的金额 */
    public static final String AMOUNT_2_DECINALS         = "^[0-9]{1,19}(.[0-9]{0,2})?$";
    
    /** 有小数点前19位，小数点后4位的金额 */
    public static final String AMOUNT_4_DECINALS         = "^[0-9]{1,19}(.[0-9]{0,4})?$";
    
    /** 有小数点前19位，小数点后6位的金额 */
    public static final String AMOUNT_6_DECINALS         = "^[0-9]{1,19}(.[0-9]{0,6})?$";
    
    /** 有小数点前19位，小数点后8位的金额 */
    public static final String AMOUNT_8_DECINALS         = "^[0-9]{1,19}(.[0-9]{0,8})?$";

    /** 非零的正整数 */
    public static final String INTEGER_NOT_ZERO          = "^\\+?[1-9][0-9]*$";

    /** 非零的负整数 */
    public static final String NEGATIVE_INTEGER_NOT_ZERO = "^\\-[1-9][0-9]*$";

    /** 26个英文字母组成的字符串 */
    public static final String STRING_LETTER             = "^[A-Za-z]+$";

    /** 26个大写英文字母组成的字符串 */
    public static final String STRING_BIG_LERRER         = "^[A-Z]+$";

    /** 26个小写英文字母组成的字符串 */
    public static final String STRING_SMALL_LERRER       = "^[a-z]+$";

    /** 数字和26个英文字母组成的字符串 */
    public static final String STRING_NUMBER_LETTER      = "^[A-Za-z0-9]+$";

    /** 密码验证：以字母开头，长度在6~18之间，只能包含字符、数字和下划线 */
    public static final String PASSWORD                  = "^[a-zA-Z]\\w{5,17}$";

    /** 验证是否含有^%&',;=?$\"等特殊字符 */
    public static final String SPECIAL                   = "^(.*)[\\#\\.\\^\\%\\&\\'\\,\\;\\=\\?\\$\\\\]+(.*)$";

    /** EMAIL邮箱 */
    public static final String EMAIL                     = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";

    /** HTTP请求UR */
    public static final String URL_HTTP                  = "^http://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$";

    /** HTTPS请求URL */
    public static final String URL_HTTPS                 = "^https://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$";

    /** FTP请求URL */
    public static final String URL_FTP                   = "^ftp://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$";

    /** 双字节字符 */
    public static final String CHAR_DOUBLE               = "[^\\x00-\\xff]";

    /** 中文字符 */
    public static final String CHAR_CHINESE              = "[\\u4e00-\\u9fa5]";

    /** 空行 */
    public static final String ROW_EMPTY                 = "\\n[\\s| ]*\\r";

    /** HTML标签 */
    public static final String HTML                      = "<(.*)>(.*)<\\/(.*)>|<(.*)2\\/>";

    /** 首尾空格的正则表达式 */
    public static final String STRING_LEFT_RIGHT_SPACE   = "(^\\s*\\w*\\s*$)";

    /** IP地址 */
    public static final String IP                        = "^\\d{1,3}+\\.\\d{1,3}+\\.\\d{1,3}+\\.\\d{1,3}$";

    /** QQ号码 */
    public static final String QQ                        = "^[1-9][0-9]{4,13}$";

    /** 电话号码 */
    public static final String TELPHONE                  = "^(\\d{3,4}-|\\d{3,4}-)?\\d{7,8}$";

    /** 手机号码 */
    public static final String MOBLIE                    = "^(\\+86|86)*0*(1){1}[3-9]{1}\\d{9}$";

    /** 身份证号（15位或18位数字） */
    public static final String ID_CARD                   = "^[0-9|X]{15}|[0-9|X]{18}$";
    /** 身份证号（15位全数字或18位最后一位为字母/数字） */
    public static final String ID_CARD_18X               = "([0-9]{17}([0-9]|X))|([0-9]{15})";

    /** 邮编 */
    public static final String POSTCODE                  = "^[1-9]{1}(\\d+){5}$";

    /** 中文字符串 */
    public static final String CHINESE                   = "^[\u4e00-\u9fa5]+$";

    /** 银行卡号 */
    public static final String BANKCARD_NO               = "^\\d{8,32}$";

    /** 银行开户名 */
    public static final String ACCOUNT_NAME              = "^.{1,20}$";
    
    public static final String MOBILE_VERCODE            = "\\d{6}";
    
    /**6-20字母与数字组合登录密码*******/
    public static final String PWD_VERIFY= "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,20}$";
    /**7-23字母与数字组合支付密码*******/
    public static final String PAY_PWD_VERIFY= "^(?![\\d]+$)(?![a-zA-Z]+$)(?![^\\da-zA-Z]+$).{7,23}$";
}
