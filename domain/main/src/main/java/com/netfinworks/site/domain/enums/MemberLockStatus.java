package com.netfinworks.site.domain.enums;

/**
 * <p>会员锁定状态</p>
 * @author eric
 * @version $Id: ResourceType.java, v 0.1 2013-7-16 下午2:15:47  Exp $
 */
public enum MemberLockStatus {

    DEFAULT(0, "正常状态"),

    LOCKED(1, "锁定");

    private final int    code;

    private final String message;

    /**
     * 构造器
     * @param code
     * @param message
     */
    MemberLockStatus(int code, String message) {
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
    public static MemberLockStatus getByCode(int code) {
        for (MemberLockStatus item : MemberLockStatus.values()) {
            if (item.getCode() == code) {
                return item;
            }
        }
        return null;
    }
    
    public static MemberLockStatus getByCode(Long code) {
        if(code == null){
            return null;
        }
        return getByCode(code.intValue());
    }
}
