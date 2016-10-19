/**
 * @company 永达互联网金融信息服务有限公司
 * @project recharge-domain-common
 * @version 1.0.0
 * @date 2014年11月20日 江海龙/2014年11月20日/首次创建
 */
package com.netfinworks.site.domain.enums;

/**
 * <p>
 * 话费充值订单充值状态
 * </p>
 * 
 * @author 江海龙
 * @since jdk6
 * @date 2014年11月20日
 */
public enum RechargeOrderRechargeStatus {

    SUCCESS("F001", "成功"), FAILURE("F002", "失败"), PROGRESS("F003", "处理中"), EXCEPTION(
            "F004", "异常"),RECHARGEING("F005", "待充值");

    private final String code;

    private final String message;

    private RechargeOrderRechargeStatus(String code, String message) {
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
