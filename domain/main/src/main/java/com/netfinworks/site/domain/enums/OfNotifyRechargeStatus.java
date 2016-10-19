/**
 * @company 永达互联网金融信息服务有限公司
 * @project recharge-domain-common
 * @version 1.0.0
 * @date 2014年11月20日 江海龙/2014年11月20日/首次创建
 */
package com.netfinworks.site.domain.enums;

/**
 * <p>
 * 欧飞返回的充值状态结果
 * </p>
 * 
 * @author 陈辉
 * @since jdk6
 * @date 2015年8月18日
 */
public enum OfNotifyRechargeStatus {
    SUCCESS("1", "成功"), FAILURE("9", "失败"), PROGRESS("0", "处理中");

    private final String code;

    private final String message;

    private OfNotifyRechargeStatus(String code, String message) {
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
