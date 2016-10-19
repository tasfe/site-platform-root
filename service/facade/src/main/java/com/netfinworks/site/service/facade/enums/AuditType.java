package com.netfinworks.site.service.facade.enums;

/**
 * <p>审核状态</p>
 * @author zhangyun.m
 * @version $Id: AuditType.java, v 0.1 2014年7月17日 下午1:06:25 zhangyun.m Exp $
 */
public enum AuditType {

    AUDIT_PENDING(1, "待审核"), AUDIT_PASS(2, "审核通过"), AUDIT_REFUSE(3, "审核拒绝"), AUDIT_CANCEL(4, "审核撤销");

    /** 代码 */
    private final Integer code;
    /** 信息 */
    private final String  message;

    AuditType(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 通过代码获取枚举项
     * @param code
     * @return
     */
    public static AuditType getByCode(Integer code) {
        if (code == null) {
            return null;
        }

        for (AuditType at : AuditType.values()) {
            if (at.getCode().equals(code)) {
                return at;
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

    public boolean equalsByCode(Integer code) {
        return this.code.equals(code);
    }

}
