package com.netfinworks.site.domain.enums;

/**
 * <p>认证状态类型类型枚举</p>
 * @author netfinworks
 * @version $Id: LoginNameTypeEnum.java, v 0.1 2010-11-29 下午02:17:45 netfinworks Exp $
 */
public enum VerifyStatus {
    NO(0, "未认证未绑定"),
    YES(1, "已认证已绑定");

    /** 代码 */
    private final Integer code;
    /** 信息 */
    private final String  message;

    VerifyStatus(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 通过代码获取枚举项
     * @param code
     * @return
     */
    public static VerifyStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }

        for (VerifyStatus lnt : VerifyStatus.values()) {
            if (lnt.getCode().equals(code)) {
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
}
