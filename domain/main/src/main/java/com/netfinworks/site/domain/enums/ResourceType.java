package com.netfinworks.site.domain.enums;

/**
 * <p>资源类型</p>
 * @author eric
 * @version $Id: ResourceType.java, v 0.1 2013-7-16 下午2:15:47  Exp $
 */
public enum ResourceType {
    MENU("M", "菜单"),

    PAGE("P", "页面");

    private final String code;

    private final String message;

    /**
     * 构造器
     * @param code
     * @param message
     */
    ResourceType(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
