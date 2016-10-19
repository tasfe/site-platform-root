package com.netfinworks.site.domain.enums;

/**
 * <p>会员类型枚举</p>
 * @author netfinworks
 * @version $Id: MemberTypeEnum.java 33158 2013-01-08 10:31:39Z fangjilue $
 */
public enum MemberType {
	PERSONAL("1", "个人", 101L,"基础账户"), ENTERPRISE("2", "企业", 201L,"基础账户"), MERCHANT("3", "商户", 301L,"基础账户"),
	//保证金户
    PERSON_ENSURE_ACCOUNT("102","保证金户",102L,"保理账户"),
    // 佣金户
    PERSON_BROKERAGE_ACCOUNT("103","佣金户",103L,"保理账户"),
    //保理户
    BAOLIHU_BASE_ACCOUNT("205","保理户",205L,"保理账户");

    /** 代码 */
    private final String code;
    /** 信息 */
    private final String message;
    /** 基本户类型 */
    private final Long   baseAccount;
    
    private final String memberType;

    MemberType(String code, String message, Long baseAccount,String memberType) {
        this.code = code;
        this.message = message;
        this.baseAccount = baseAccount;
        this.memberType = memberType;
    }
    /**
     * 通过基本户类型获枚举信息
     * @param baseAccount
     * @return
     */
    public static String getMessage(Long baseAccount) {
        if (baseAccount == null) {
            return null;
        }

        for (MemberType memberType : MemberType.values()) {
            if (memberType.getBaseAccount().equals(baseAccount)) {
                return memberType.getMemberType();
            }
        }

        return null;
    }
    /**
     * 通过代码获取枚举项
     * @param code
     * @return
     */
    public static MemberType getByCode(String code) {
        if (code == null) {
            return null;
        }

        for (MemberType memberType : MemberType.values()) {
            if (memberType.getCode().equals(code)) {
                return memberType;
            }
        }

        return null;
    }
    
    public static MemberType getByCode(Long code) {
        if (code == null) {
            return null;
        }
        return getByCode(code.toString());
    }

    
    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Long getBaseAccount() {
        return baseAccount;
    }
    
    public String getMemberType() {
        return memberType;
    }
}
