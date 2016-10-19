package com.netfinworks.site.domain.enums;
/**
 * <p>
 * 话费充值订单支付状态
 * </p>
 * 
 * @author 赵贞强
 * @since jdk6
 * @date 2014年11月27日
 */
public enum RechargeOrderPayStatus {
	PAYING("F001", "待支付"),  SUCCESS("F002", "支付成功"), FAILURE("F003", "支付失败"), REFUNDED(
            "F004", "已退款");

    private final String code;

    private final String message;

    
    private RechargeOrderPayStatus(String code, String message) {
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
