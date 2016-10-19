package com.netfinworks.site.domain.enums;

/**
 * <p>会员激活状态</p>
 * @author eric
 * @version $Id: ResourceType.java, v 0.1 2013-7-16 下午2:15:47  Exp $
 */
public enum MemberStatus {

    NOT_ACTIVE(0, "未激活"),

    DEFAULT(1, "正常状态");

    private final int    code;

    private final String message;

    /**
     * 构造器
     * @param code
     * @param message
     */
    MemberStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    
    /**
     * 通过代码获取枚举项
     * @param code
     * @return
     */
    public static MemberStatus getByCode(int code) {
        for (MemberStatus item : MemberStatus.values()) {
            if (item.getCode() == code) {
                return item;
            }
        }
        return null;
    }
    
    public static MemberStatus getByCode(Long code) {
        if(code == null){
            return null;
        }
        return getByCode(code.intValue());
    }
}
