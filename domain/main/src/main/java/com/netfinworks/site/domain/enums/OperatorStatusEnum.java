package com.netfinworks.site.domain.enums;

/***
 * 
 * <p>操作员状态枚举类</p>
 * @author luoxun
 * @version $Id: OperatorStatusEnum.java, v 0.1 2014-5-21 上午10:25:57 luoxun Exp $
 */
public enum OperatorStatusEnum {
    NOT_ACTIVE(0, "未激活"), NORMAL(1, "正常"), CLOSE(2, "已注销");

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

    private OperatorStatusEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static OperatorStatusEnum getByCode(Integer code) {
        if (code==null) {
            return null;
        }
        for (OperatorStatusEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
}
