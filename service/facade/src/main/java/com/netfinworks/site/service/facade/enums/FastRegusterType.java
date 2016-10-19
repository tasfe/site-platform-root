package com.netfinworks.site.service.facade.enums;

public enum FastRegusterType {

    MOBILE("1", "手机"), E_MAIL("2", "邮箱");

    /** 注册类型*/
    private final String type;
    /** 信息 */
    private final String  message;

    FastRegusterType(String type, String message) {
        this.type = type;
        this.message = message;
    }

    /**
     * 通过代码获取枚举项
     * @param type
     * @return
     */
    public static FastRegusterType getByType(String type) {
        if (type == null) {
            return null;
        }

        for (FastRegusterType at : FastRegusterType.values()) {
            if (at.getType().equals(type)) {
                return at;
            }
        }

        return null;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public boolean equalsByCode(String type) {
        return this.type.equals(type);
    }


}
