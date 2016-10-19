/**
 * @company 永达互联网金融信息服务有限公司
 * @project recharge-domain-common
 * @version 1.0.0
 * @date 2014年11月20日 江海龙/2014年11月20日/首次创建
 */
package com.netfinworks.site.domain.enums;

/**
 * <p>
 * 手拉手返回的充值状态结果
 * </p>
 * 
 * @author 江海龙
 * @since jdk6
 * @date 2014年11月20日
 */
public enum SlsNotifyRechargeStatus {
    SUCCESS("00", "成功"), FAILURE("01", "失败"), PROGRESS("02", "处理中"), EXCEPTION("03", "异常");

    private final String code;

    private final String message;

    private SlsNotifyRechargeStatus(String code, String message) {
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
