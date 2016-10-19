
package com.netfinworks.site.domain.enums;

/**
 *
 * <p>用户开户处理</p>
 * @author qinde
 * @version $Id: MemberSetStatus.java, v 0.1 2013-12-19 下午3:01:57 qinde Exp $
 */
public enum MemberSetStatus {

    ACTIVE("ACTIVE", "激活"),

	SET_PASSWD("SET_PASSWD", "设置支付密码"),

	BIND_PHONE("BIND_PHONE", "绑定手机号码"),
	
	CERTIFI_V0("CERTIFI_V0", "实名校验"),
	
	CERTIFI_V1("CERTIFI_V1", "实名认证V1"),
	
	CERTIFI_V2("CERTIFI_V2", "实名认证V2");

    private final String    code;

    private final String message;

    /**
     * 构造器
     * @param code
     * @param message
     */
    MemberSetStatus(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }


}
