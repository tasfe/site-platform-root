package com.netfinworks.site.domain.enums;

/***
 * 
 * <p>操作员类型枚举类</p>
 * @author luoxun
 * @version $Id: OperatorStatusEnum.java, v 0.1 2014-5-21 上午10:25:57 luoxun Exp $
 */
public enum OperatorTypeEnum {
    PERSONAL(1, "个人"), ENTERPRISE(2, "企业商户"), INSTITUTION(3, "非企业机构"), PERSON_MERCHANT(4, "个人商户");

    private Integer code;
    private String  msg;

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

    private OperatorTypeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static OperatorTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OperatorTypeEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
}
