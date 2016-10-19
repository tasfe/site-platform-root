package com.netfinworks.site.domain.enums;

/**
 * <p>会员支付密码状态</p>
 * @author eric
 * @version $Id: ResourceType.java, v 0.1 2013-7-16 下午2:15:47  Exp $
 */
public enum MemberPaypasswdStatus {

    DEFAULT(0, "正常状态"),

    NOT_SET_PAYPASSWD(1, "未设置支付密码"),

    PAYPASSWD_LOCKED(2, "支付密码被锁定");

    private final int code;

    private final String message;

    /**
     * 构造器
     * @param code
     * @param message
     */
    MemberPaypasswdStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
