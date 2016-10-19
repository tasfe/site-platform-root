package com.netfinworks.site.domain.enums;

/**
 * <p>邮箱服务器登陆地址</p>
 * 暂录20个邮箱登陆地址
 * @author liangzhizhuang.m
 * @version $Id: LoginEmailAddress.java, v 0.1 2014年5月27日 下午12:29:44 liangzhizhuang.m Exp $
 */
public enum LoginEmailAddress {

    /** 网易163邮箱 */
    email_163("163.com", "http://mail.163.com"),

    /** 网易126邮箱 */
    email_126("126.com", "http://www.126.com/"),

    /** 网易yeah邮箱 */
    email_yeah("yeah.net", "http://www.yeah.net/"),

    /** qq邮箱 */
    email_qq("qq.com", "http://mail.qq.com"),

    /** qq企业邮箱 */
    email_netfinworks("netfinworks.com", "http://exmail.qq.com/login"),

    /** qq邮箱 */
    email_foxmail("foxmail.com", "http://mail.qq.com"),
    
    email_139("139.com", "http://mail.10086.cn/"),
    
    email_gmail("gmail.com", "http://accounts.google.com/"),
    
    email_sogou("sogou.com", "http://mail.sogou.com/"),
    
    email_sohu("sohu.com", "http://mail.sohu.com/"),
    
    email_sina_com("sina.com", "http://mail.sina.com.cn/"),
    
    email_sina_cn("sina.cn", "http://mail.sina.com.cn/"),
    
    email_2980("2980.com", "http://www.2980.com/"),
    
    email_wo("wo.cn", "http://mail.wo.com.cn"),
    
    email_outlook("outlook.com", "http://login.live.com/"),
    
    email_hotmail("hotmail.com", "http://login.live.com/"),
    
    email_189("189.cn", "http://webmail2.189.cn"),
    
    email_aliyun("aliyun.com", "http://mail.aliyun.com"),
    
    email_188("188.com", "http://www.188.com/"),
    
	email_tom("tom.com", "http://web.mail.tom.com"),
    
	email_21cn("21cn.com", "http://mail.21cn.com/");

    /** 邮箱名称 */
    private final String emailName;

    /** 邮箱登陆地址 */
    private final String url;

    LoginEmailAddress(String emailName, String url) {
        this.emailName = emailName;
        this.url = url;
    }

    public String getEmailName() {
        return emailName;
    }

    public String getUrl() {
        return url;
    }

    public static String getEmailLoginUrl(String emailName) {
        emailName = emailName.substring(emailName.lastIndexOf("@") + 1);
        for (LoginEmailAddress item : LoginEmailAddress.values()) {
			if (item.getEmailName().equalsIgnoreCase(emailName)) {
                return item.getUrl();
            }
        }
        return "";
    }

}
