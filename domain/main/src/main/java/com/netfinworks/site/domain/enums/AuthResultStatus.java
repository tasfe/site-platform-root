package com.netfinworks.site.domain.enums;

import org.apache.commons.lang.StringUtils;

/**
 *
 * <p>认证结果</p>
 * @author qinde
 * @version $Id: AuthResultStatus.java, v 0.1 2013-12-2 下午7:36:48 qinde Exp $
 */
public enum AuthResultStatus {
    INIT("init", "实名认证中"),
    AUDIT_PASS("auditPass", "审核通过"),
    AUDIT_REJECT("auditReject", "实名认证被拒绝"),
    /*checkPass 复审通过即实名认证已通过 一般情况checkPass=PASS*/
    CHECK_PASS("checkPass", "实名认证已通过"),
    CHECK_REJECT("checkReject", "实名认证被拒绝"),
    //以上为实名的中间结果
    NOT_FOUND("NOT_FOUND", "未实名认证"),
    //MA 实名状态已经通过
    PASS("PASS","实名认证已通过");

    private String code;
    private String message;

    public String getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    private AuthResultStatus(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static AuthResultStatus getByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (AuthResultStatus item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
}
