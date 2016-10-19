package com.netfinworks.site.domain.enums;

/**
 * 
 * <p>操作员锁定状态枚举类</p>
 * @author luoxun
 * @version $Id: OperatorLockStatusEnum.java, v 0.1 2014-5-21 上午10:41:09 luoxun Exp $
 */
public enum OperatorLockStatusEnum {
    
    UNLOCK(0,"启用"),
    LOCK(1,"禁用");
    
    private Integer   code;
    private String msg;
    
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    private OperatorLockStatusEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static OperatorLockStatusEnum getByCode(Integer code) {
        if (code==null) {
            return null;
        }
        for (OperatorLockStatusEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

}
