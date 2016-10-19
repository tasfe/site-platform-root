package com.netfinworks.site.domain.enums;

/**
 * <p>
 * 速卡返回的充值状态结果
 * </p>
 * 
 * @author zwj
 * @since jdk6
 * @date 2016年6月8日
 */
public enum SkNotifyRechargeStatus {
	SUCCESS("true", "成功"), FAILURE("false", "失败");
	
	private final String code;

    private final String message;

    private SkNotifyRechargeStatus(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }
}
