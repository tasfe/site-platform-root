package com.netfinworks.site.web.common.constant;

/**
 * <p>支付渠道</p>
 * @author eric
 * @version $Id: ResourceType.java, v 0.1 2013-7-16 下午2:15:47  Exp $
 */
public enum PayChannel {
    BALANCE("01", "余额", ""),
    NET_BANK("02", "网银", ""),
    POS("03", "POS", ""),
    CASH("04", "现金", ""),
    QUICK("05", "快捷", ""),
    FUNDOUT("14", "出款", ""),
    
    // 永达互联网金融支付4.0新增渠道
    NET_BANK_DC("20", "纯借", ""),
    NET_BANK_DC2("29", "纯借+银联前置", "_F"),
    NET_BANK_CC("21", "纯贷", ""),
    NET_BANK_DC_LARGE("22", "纯借（大）", "_L"),
    NET_BANK_CC_LARGE("23", "纯贷（大）", "_L"),
    QUICK_DC("51", "纯借(快捷）", "_Q"),
    QUICK_CC("52", "纯贷(快捷）", "_Q"),
    COMP("30", "综合", ""),
    B2B("40", "B2B", ""),
    VERIFY("60", "认证支付", "_V"),
    ;

    private final String code;

    private final String message;
    
    private final String sufCode;

    /**
     * @param code
     * @param message
     */
    PayChannel(String code, String message, String sufCode) {
        this.code = code;
        this.message = message;
        this.sufCode = sufCode;
    }

    /**
     * 根据代码获取ENUM
     *
     * @param code
     * @return
     */
    public static PayChannel getByCode(String code) {
        for (PayChannel payChannel : PayChannel.values()) {
            if (code.equals(payChannel.getCode())) {
                return payChannel;
            }
        }

        return null;
    }
    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

	public String getSufCode() {
		return sufCode;
	}

}
