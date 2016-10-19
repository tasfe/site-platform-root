package com.netfinworks.site.web.common.constant;

/**
 * <p>支付渠道</p>
 * @author eric
 * @version $Id: ResourceType.java, v 0.1 2013-7-16 下午2:15:47  Exp $
 */
public enum PayChannel {
    BALANCE("01", "余额", "", "", 0),
    NET_BANK("02", "网银", "", "", 0),
    POS("03", "POS", "", "", 0),
    CASH("04", "现金", "", "", 0),
    QUICK("05", "快捷", "", "", 0),
    FUND("06", "基金", "", "", 0),
    FUNDOUT("14", "出款", "", "", 0),
    
    // 永达互联网金融支付4.0新增渠道
    NET_BANK_DC("20", "纯借", "", "", 8),
    NET_BANK_DC2("29", "纯借普", ",_F", "", 8),
    NET_BANK_CC2("28", "纯贷普", ",_F", "", 8),
    NET_BANK_DC_LARGE("22", "纯借（大）", "_L", "大额", 5),
    NET_BANK_CC_LARGE("23", "纯贷（大）", "_L", "大额", 5),
    QUICK_DC("51", "纯借(快捷）", "_Q", "快捷", 9),
    QUICK_CC("52", "纯贷(快捷）", "_Q", "快捷", 9),
    COMP("30", "综合", ",_F", "综合", 0),
    B2B("40", "B2B", "", "", 1),
    VERIFY("60", "认证支付", "_V", "", 0),
    TRUST_COLLECT_DC("61", "纯借（代扣）", "", "", 0),
    TRUST_COLLECT_CC("62", "纯贷（代扣）", "", "", 0),
    UNIONPAY("70", "银联间联", "_F", "", 0),
    ;

    private final String code;

    private final String message;
    
    private final String sufCode;
    
    private final String sufDesc;
    
    // 支付渠道显示优先级
    private final int level;

    /**
     * @param code
     * @param message
     */
    PayChannel(String code, String message, String sufCode, String sufDesc, int level) {
        this.code = code;
        this.message = message;
        this.sufCode = sufCode;
        this.sufDesc = sufDesc;
        this.level = level;
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
    
    /**
     * 根据尾码获取一个ENUM对象
     * @param sufCode 尾码
     * @return ENUM对象
     */
    public static PayChannel getBySufCode(String sufCode) {
        for (PayChannel payChannel : PayChannel.values()) {
            if (sufCode.equals(payChannel.getSufCode())) {
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

	public String getSufDesc() {
		return sufDesc;
	}

    public int getLevel() {
        return level;
    }

}
