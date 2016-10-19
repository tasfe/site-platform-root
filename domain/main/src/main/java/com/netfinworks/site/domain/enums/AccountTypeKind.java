package com.netfinworks.site.domain.enums;

/**
 *
 * <p>账户类型</p>
 * @author leelun
 * @version $Id: AccountTypeKind.java, v 0.1 2014-5-9 下午4:02:53 lilun Exp $
 */
public enum AccountTypeKind {

    //基本户
    PERSON_BASE_ACCOUNT(101L, "101"),
    //企业基本户
    COMPANY_BASE_ACCOUNT(201L, "201"),
    //特约商户基本户
    MERCHANT_BASE_ACCOUNT(301L, "301"),
    
    //保证金户
    PERSON_ENSURE_ACCOUNT(102L, "102"),
    // 佣金户
    PERSON_BROKERAGE_ACCOUNT(103L, "103"),
    //保理户
    BAOLIHU_BASE_ACCOUNT(205L,"205")
    ;

    private long code;
    private String msg;

    private AccountTypeKind(Long code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static AccountTypeKind getByCode(long code) {
        for (AccountTypeKind ls : AccountTypeKind.values()) {
            if (ls.code == code) {
                return ls;
            }
        }
        return null;
    }

    public static AccountTypeKind getByMsg(String msg) {
        for (AccountTypeKind ls : AccountTypeKind.values()) {
            if (ls.msg.equalsIgnoreCase(msg)) {
                return ls;
            }
        }
        return null;
    }

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean equals(long code) {
        return getCode() == code;
    }
}
